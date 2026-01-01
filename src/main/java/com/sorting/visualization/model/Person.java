package com.sorting.visualization.model;

import lombok.Data;

@Data
public class Person {
    private Integer id;
    private String name;
    private Integer age;    // 0-120
    private Double score;   // 0-150
    private String email;

    public Person() {
    }

    public Person(Integer id, String name, Integer age, Double score, String email) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.score = score;
        this.email = email;
    }
}