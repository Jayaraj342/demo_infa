package com.example.demo.repository;

import com.example.demo.model.Post;
import com.example.demo.model.User;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
@Slf4j
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    @Test
    void testFetchTypes() {
        Optional<User> optionalUser = userRepository.findById(1);
        User user = optionalUser.get();
        System.out.println(user);

        System.out.println("User fetched ---------------------");

        for (Post post : user.getPosts()) {
            System.out.println("post fetched -> " + post);
        }
    }
}