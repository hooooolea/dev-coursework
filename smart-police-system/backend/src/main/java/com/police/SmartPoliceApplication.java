package com.police;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class SmartPoliceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartPoliceApplication.class, args);
        System.out.println("===========================================");
        System.out.println("  智能警务管理系统 启动成功");
        System.out.println("  访问地址: http://localhost:8080");
        System.out.println("===========================================");
    }
}
