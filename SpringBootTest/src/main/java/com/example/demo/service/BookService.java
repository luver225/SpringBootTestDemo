package com.example.demo.service;

import com.example.demo.bean.Employee;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.awt.print.Book;

@Service
public class BookService {

    @RabbitListener(queues = "amqpadmin.queue1")
    public void receive01(Employee employee){
        System.out.println("receive01："+employee);
    }

    @RabbitListener(queues = "amqpadmin.queue2")
    public void receive02(Message message){
        System.out.println("receive02："+message);
        System.out.println(message.getBody());
        System.out.println(message.getMessageProperties());
    }
}
