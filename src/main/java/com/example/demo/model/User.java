package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
public class User extends BaseModel {

    String name;
    String email;

    // LAZY won't work because, getPosts is called by jackson during object init
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference
    @Fetch(FetchMode.SUBSELECT)
    List<Post> posts = new ArrayList<>();

//    @Override
//    public String toString() {
//        return "User{" +
//                "name='" + name + '\'' +
//                ", email='" + email + '\'' +
//                ", posts=" + posts +
//                '}';
//    }
}

// Fetch type -> LAZY
// Only User will be fetched using select query1
// On getPosts, post will be fetched using select query2

// Fetch type -> EAGER
// User and posts will be fetched using JOIN query1

// ------------------------------------------------------

// Fetch modes -> SELECT, JOIN, SUBSELECT

/*
FetchType        FetchMode          Result

LAZY             SELECT             Asked For Products - Queries - 2 select
                                    Not Asked For Products - Queries - 1 select

EAGER            SELECT             Asked For Products - Queries - 2 select
                                    Not Asked For Products - Queries - 2 select


LAZY             JOIN               Asked For Products - Queries - 1 join query
                                    Not Asked For Products - Queries - 1 join query

EAGER            JOIN               Asked For Products - Queries - 1 join query
                                    Not Asked For Products - Queries - 1 join query


LAZY             SUBSELECT           Asked For Products - Queries - 2 select queries
                                    Not Asked For Products - Queries - 1 select query

EAGER            SUBSELECT          Asked For Products - Queries - 2 select queries
                                    Not Asked For Products - Queries - 2 select queries

RESULTS ->

1. FetchMode is honored in case of SELECT in case of fetchtype EAGER, as we
saw select instead of joins

2. FetchMode Join will be dominant over any fetchtype.
It will get both Product and Category always

3. In case of SUB-SELECT, it is behaving same as SELECT. Unfortunately we are not able
to see any Subquery :(
*/
