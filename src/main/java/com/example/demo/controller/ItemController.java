package com.example.demo.controller;

import com.example.demo.model.Item;
import com.example.demo.service.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/demo")
@Slf4j
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping("/add-item")
    public void addItem(@RequestBody Item item) {
        itemService.addItem(item);
    }
}
