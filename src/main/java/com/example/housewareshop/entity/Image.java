package com.example.housewareshop.entity;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Data
//@Builder
@ToString
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String imageUrl;
    //    private int status; // 0-1
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
