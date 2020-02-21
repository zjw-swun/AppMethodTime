package com.zjw.appmethodtime;

public class BaseModel {
    private int age;
    private String name = "test";

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "BaseModel{" +
                "age=" + age +
                ", name='" + name + '\'' +
                '}';
    }
}
