package com.example.demo.service;

import com.example.demo.model.Shop;
import com.example.demo.repository.ShopRepository;
import org.springframework.stereotype.Service;

@Service
public class ShopService {

    private final ShopRepository shopRepository;

    public ShopService(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    public void addShop(Shop shop) {
        shopRepository.save(shop);
    }

    public Shop getShop(int id) {
        return shopRepository.findById(id).orElseThrow();
    }
}
