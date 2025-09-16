package com.example.demo.repository;

import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {

    // 14+ seconds timeout query
//    @QueryHints(value = @QueryHint(name = "jakarta.persistence.query.timeout", value = "3"))
//    @Query(value = "select benchmark(90000000, md5('when will it end?'));", nativeQuery = true)
    public void getTimeEaterZero() {};
}
