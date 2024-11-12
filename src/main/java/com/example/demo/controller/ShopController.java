package com.example.demo.controller;

import com.example.demo.model.Shop;
import com.example.demo.service.ShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/demo")
@Slf4j
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @PostMapping("/add-shop")
    public void addShop(@RequestBody Shop shop) {
        shopService.addShop(shop);
    }

    @GetMapping("/get-shop/{id}")
    public Shop getShop(@PathVariable int id) {
        return shopService.getShop(id);
    }
}
