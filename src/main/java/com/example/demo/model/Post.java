package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
public class Post extends BaseModel {

    private String message;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonBackReference
    private User user;

//    @Override
//    public String toString() {
//        return "Post{" +
//                "message='" + message + '\'' +
//                '}';
//    }
}