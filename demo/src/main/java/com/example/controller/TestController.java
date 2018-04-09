package com.example.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bean.Door;
import com.example.bean.Head;
import com.example.bean.Person;
import com.example.sevice.TestService;

@RestController
public class TestController {

    @Autowired
    private TestService testService;

    @RequestMapping("/hello")
    public Person hello(int age){
        Person person = new Person("link",age,new Head(2,1));
        return testService.hello(person);
    }

    @RequestMapping("/hello2/{age}")
    public Person hello2(@PathVariable int age){
        Person person = new Person("link",age,new Head(2,1));
        return testService.hello2(person);
    }

    @RequestMapping("/bye/{age}")
    public Person bye(@PathVariable int age){
        Person person = new Person("link",age,new Head(2,1));
        return testService.bye(person);
    }

    @RequestMapping("/bye2/{age}")
    public Person bye2(@PathVariable int age){
        Person person = new Person("link",age,new Head(2,1));
        return testService.bye2(person);
    }

    @RequestMapping("/door/{id}")
    public Door door(@PathVariable int id){
        Door door = new Door(id);
        return testService.door(door);
    }
}