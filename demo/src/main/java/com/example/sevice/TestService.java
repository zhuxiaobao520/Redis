package com.example.sevice;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.example.bean.Door;
import com.example.bean.Person;

@Service
public class TestService {

    @Cacheable(value = "hello")
    public Person hello(Person person){
        System.err.println("2");
        return person;
    }

    @Cacheable(value = "hello2")
    public Person hello2(Person person){
        System.err.println("2");
        return person;
    }

    @CacheEvict(value = "hello")
    public Person bye(Person person){
        System.err.println("2");
        return person;
    }

    @CacheEvict(value = "hello2")
    public Person bye2(Person person){
        System.err.println("2");
        return person;
    }

    @Cacheable(value = "door")
    public Door door(Door door){
        System.err.println("2");
        return door;
    }
}