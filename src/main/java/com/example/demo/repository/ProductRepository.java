package com.example.demo.repository;

import com.example.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 15+ seconds timeout query
    @Query(value = "select benchmark(9000000, md5('when will it end?'));", nativeQuery = true)
    void getTimeEaterZero();
}
