package com.im.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * IM 即时通讯系统后端主启动类
 */
@SpringBootApplication
@EnableScheduling
public class ImBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImBackendApplication.class, args);
        System.out.println("========================================");
        System.out.println("  IM Backend 服务已启动!");
        System.out.println("  WebSocket: ws://localhost:8080/ws");
        System.out.println("  HTTP API: http://localhost:8080/api");
        System.out.println("========================================");
    }
}
