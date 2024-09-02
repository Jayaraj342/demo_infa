package com.example.demo.repository;

import com.example.demo.model.Product;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 23+ seconds timeout query
//    @QueryHints(value = @QueryHint(name = "jakarta.persistence.query.timeout", value = "3"))
    @Query(value = "select benchmark(9000000, md5('when will it end?'));", nativeQuery = true)
    void getTimeEaterZero();
}
