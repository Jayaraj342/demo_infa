package com.example.demo.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Product extends BaseModel {
    private String name;
    private double price;
    private int quantity;
}
