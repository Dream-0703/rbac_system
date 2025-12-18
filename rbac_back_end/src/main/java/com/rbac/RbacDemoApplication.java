package com.rbac;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot启动类（Web底座核心入口）
 * 作用：启动Spring容器，扫描所有组件（Controller/Service/Proxy/DAO等）
 */
@SpringBootApplication // 核心注解：包含自动配置、组件扫描、配置类注解
public class RbacDemoApplication {
    // 程序入口方法：仅负责启动Spring Boot应用，移除所有测试逻辑
    public static void main(String[] args) {
        // 启动Spring Boot应用（加载Web服务器+所有组件）
        SpringApplication.run(RbacDemoApplication.class, args);

        // 可选：仅保留启动成功日志，便于排查
        System.out.println("===== RBAC权限系统启动成功 =====");
        System.out.println("访问地址：http://localhost:8080/swagger-ui/index.html");
    }
}