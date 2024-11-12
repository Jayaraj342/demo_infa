package com.example.demo.model;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class Item extends BaseModel {
    private String name;
    private double price;
    private int quantity;
}