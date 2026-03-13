package com.sevenshop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.sevenshop.mapper")
@EnableAsync
public class SevenShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(SevenShopApplication.class, args);
    }
}
