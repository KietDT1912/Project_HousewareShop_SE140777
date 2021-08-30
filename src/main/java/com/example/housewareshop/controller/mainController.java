package com.example.housewareshop.controller;

import com.example.housewareshop.entity.*;
import com.example.housewareshop.repository.*;
import com.example.housewareshop.util.MathFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.SimpleFormatter;

@Controller
public class mainController {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ShippingRepository shippingRepository;

    @Autowired
    OrderDetailRepository orderDetailRepository;

    @Autowired
    OrderRepository orderRepository;

    @GetMapping({"/"})
    public String index(){
        return "home";
    }

    @GetMapping({"/hello"})
    public String hello(){
        return "hello";
    }

    @GetMapping("/products")
    public String getListProducts(Model model,
                                  @RequestParam(name = "page", defaultValue = "1")Integer page,
                                  @RequestParam(name = "categoryId", defaultValue = "-1")Integer categoryId,
                                  @RequestParam(name = "subCategoryId", defaultValue = "-1")Integer subCategoryId){
        final int PAGE_SIZE = 20;
        Page<Product> products;
        if(subCategoryId != -1){
            products = productRepository.findBySubCategoryId(subCategoryId, PageRequest.of(page -1, PAGE_SIZE));
            if(products.getContent().size() == 0){
                products = productRepository.findBySubCategoryId(subCategoryId, PageRequest.of(0, PAGE_SIZE));
                page = 1 ;
            }
        }else if (categoryId != -1 ){
            products = productRepository.findByCategoryId(categoryId, PageRequest.of(page -1, PAGE_SIZE));
            if(products.getContent().size() == 0){
                products = productRepository.findByCategoryId(categoryId, PageRequest.of(0, PAGE_SIZE));
                page = 1 ;
            }
        }else{
            products = productRepository.findAll(PageRequest.of(page -1, PAGE_SIZE));
            if(products.getContent().size() == 0){
                products = productRepository.findAll(PageRequest.of(0, PAGE_SIZE));
                page = 1 ;
            }
        }
        List<Category> categories = categoryRepository.findAll();

        model.addAttribute( "products",products);
        model.addAttribute("categories", categories);
        model.addAttribute("page", page);
        model.addAttribute("totalPage", products.getTotalPages());
        return "listProduct";
    }

    @GetMapping("/detail")
    public String detail(Model model,
                         @RequestParam(name = "productId")Long productId){

        Product products = productRepository.findById(productId).get();
        List<Image> images = products.getImages();
        Image image = new Image();
        image.setImageUrl(products.getImageUrl());
        images.add(0, image);
        products.setImages(images);

        model.addAttribute("products",products);
        return "detail";
    }

    @GetMapping("/add-to-cart")
    public String addToCart(Model model,
                            HttpSession session,
                            @RequestParam(name = "productId")Long productId){

        Product product = productRepository.findById(productId).get();

        Cart cart = new Cart();

        cart.setProductId(product.getId());
        cart.setProductCode(product.getCode());
        cart.setProductName(product.getName());
        cart.setProductQuantity(product.getQuantity());
        cart.setProductPrice(product.getPrice());                       // vì chỉ mới có 1 sản phẩm nên getPrice: cart.setProductPrice(product.getPrice() * 1);
        cart.setProductDescription(product.getDescription());
        cart.setProductImageUrl(product.getImageUrl());
        cart.setQuantity(1);

        List<Cart> listCart = (List<Cart>) session.getAttribute("listCart");
        if(listCart == null){
            listCart = new ArrayList<>();
            listCart.add(cart);
        }else{
            // Nghĩa là đã tồn tại giỏ hàng trên session
            // Có 2 khả năng
            boolean isExist = false;
            // TH1: SP đã có trong giỏ hàng

            for (Cart ca: listCart) {
                if (productId.equals(ca.getProductId())){
                    isExist = true;
                    ca.setQuantity(ca.getQuantity() + 1);    // Mỗi lần chỉ được add thêm 1 sp cùng loại cho 1 lần click, nếu muốn thì lấy biến số lượng sp rồi thay vào 1
                }
            }
            // TH2: SP chưa có trong giỏ hàng
            if (!isExist){
                listCart.add(cart);
            }
        }

        session.setAttribute("listCart",listCart);
        return "redirect:/products";
    }

    @GetMapping("/carts")
    public String getListCart(Model model,HttpSession session) {
        List<Cart> listCart = (List<Cart>) session.getAttribute("listCart");
        if(listCart == null || listCart.size() == 0){
            return "emptyCart";
        }

        double totalMoney = 0;
        for(Cart c: listCart){
            totalMoney += c.getProductPrice()*c.getQuantity();
        }
        model.addAttribute("totalMoney", MathFunction.getMoney(totalMoney));
        model.addAttribute("listCart" ,listCart);
        return "listCart";
    }
    @GetMapping("/delete-cart")
    public String deleteCart(HttpSession session,
                             @RequestParam(name = "productId",required = false)Long productId){
        List<Cart> listCart = (List<Cart>) session.getAttribute("listCart");
        if(productId == null){
            //session.setAttribute("listCart", new ArrayList<>());
            listCart = null;
        }else{
            for (Cart c: listCart) {
                if(c.getProductId().equals(productId)){
                    listCart.remove(c);
                    break;
                }
            }
        }
        session.setAttribute("listCart", listCart);
        return "redirect:/carts";
    }

    @PostMapping ("/update-cart")
    public String updateCart(HttpServletRequest req, HttpSession session) {
        List<Cart> listCart = (List<Cart>) session.getAttribute("listCart");
        for (int i = 0; i < listCart.size(); i++) {
            listCart.get(i).setQuantity(Integer.parseInt(req.getParameter("quantity"+i)));
        }
        session.setAttribute("listCart",listCart);
        return "redirect:/carts";
    }

    @GetMapping("/checkout")
    public String checkout(Model model,
                           HttpSession session) {
        List<Cart> listCart = (List<Cart>) session.getAttribute("listCart");
        if(listCart == null || listCart.size() == 0){
            return "emptyCart";
        }

        double totalMoney = 0;
        for(Cart c: listCart){
            totalMoney += c.getProductPrice()*c.getQuantity();
        }
        model.addAttribute("totalMoney", MathFunction.getMoney(totalMoney));
        model.addAttribute("listCart" ,listCart);
        return "checkout";
    }

    @GetMapping("/prepare-shipping")
    public String prepareShipping(Model model,
                                  @RequestParam(name = "name")String name,
                                  @RequestParam(name = "phone")String phone,
                                  @RequestParam(name = "address")String address,
                                  @RequestParam(name = "note")String note,
                                  HttpSession session){

        List<Cart> listCart = (List<Cart>) session.getAttribute("listCart");
        if(listCart == null || listCart.size() == 0){
            return "emptyCart";
        }

        double totalMoney = 0;
        for(Cart c: listCart){
            totalMoney += c.getProductPrice()*c.getQuantity();
        }
        model.addAttribute("totalMoney", MathFunction.getMoney(totalMoney));
        model.addAttribute("listCart" ,listCart);
        model.addAttribute("name",name);
        model.addAttribute("phone",phone);
        model.addAttribute("address",address);
        model.addAttribute("note",note);
        return "prepareShipping";
    }

    @PostMapping("/prepare-shipping")
    public String postPrepareShipping(Model model,
                                  @RequestParam(name = "name")String name,
                                  @RequestParam(name = "phone")String phone,
                                  @RequestParam(name = "address")String address,
                                  @RequestParam(name = "note")String note,
                                  HttpSession session){

        List<Cart> listCart = (List<Cart>) session.getAttribute("listCart");
        if(listCart == null || listCart.size() == 0){
            return "emptyCart";
        }

        double totalMoney = 0;
        for(Cart c: listCart){
            totalMoney += c.getProductPrice()*c.getQuantity();
        }

        // Save to Database
        // Shipping save
        Shipping shipping = new Shipping();
        shipping.setName(name);
        shipping.setPhone(phone);
        shipping.setAddress(address);
        shipping = shippingRepository.save(shipping);

        // Order save
        Order order = new Order();
        order.setNote(note);
        order.setTotalPrice(totalMoney);

        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        order.setCreatedDate(java.sql.Date.valueOf(sdf.format(now)));

        order.setShipping(shipping);

        StatusOrder statusOrder = new StatusOrder();
        statusOrder.setId(1);
        order.setStatus(statusOrder);

        order = orderRepository.save(order);

        // Order detail save
        for (Cart cart: listCart) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);
            orderDetail.setQuantity(cart.getQuantity());
            orderDetail.setProductName(cart.getProductName());
            orderDetail.setProductPrice(cart.getProductPrice());
            orderDetail.setProductImage(cart.getProductImageUrl());

            orderDetailRepository.save(orderDetail);
        }

        return "prepareShipping";
    }

    @GetMapping({"/search"})
    public String search(Model model,
                         @RequestParam(name = "keyword", defaultValue = "")String keyword,
                         @RequestParam(name = "page", defaultValue = "1")Integer page){

        final int PAGE_SIZE = 20;
        Page<Product> products = productRepository.search("%" + keyword + "%", PageRequest.of(page -1, PAGE_SIZE));

        List<Category> categories = categoryRepository.findAll();

        model.addAttribute( "products",products);
        model.addAttribute("categories", categories);
        model.addAttribute("page", page);
        model.addAttribute("totalPage", products.getTotalPages());
        return "listProduct";
    }
}
