package com.ict.system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 模拟认证控制器，用于在MongoDB不可用时提供基本的登录功能
 */
@RestController
@RequestMapping("/mock-auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MockAuthController {

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        System.out.println("模拟登录请求: 用户名=" + username + ", 密码=" + password);

        // 简单验证，仅接受admin/123456
        if ("admin".equals(username) && "123456".equals(password)) {
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> user = new HashMap<>();

            // 模拟用户信息
            user.put("id", "mock-user-id-001");
            user.put("username", username);
            user.put("name", "系统管理员");
            user.put("email", "admin@example.com");
            user.put("department", "系统部");
            user.put("roles", new String[]{"admin"});
            user.put("permissions", new String[]{
                "maintenance.view", "maintenance.add", "maintenance.edit", "maintenance.delete",
                "maintenance.export", "maintenance.update-status",
                "inventory.view", "inventory.add", "inventory.edit", "inventory.delete",
                "inventory.import", "inventory.export", "inventory.check",
                "network.view", "network.add", "network.edit", "network.delete",
                "network.topology", "network.terminal",
                "supplies.view", "supplies.apply", "supplies.approve", "supplies.return",
                "system.user", "system.role", "system.department", "system.log"
            });

            // 模拟令牌
            String mockToken = "mock-jwt-token-" + System.currentTimeMillis();

            response.put("token", mockToken);
            response.put("user", user);
            response.put("message", "登录成功");

            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "用户名或密码错误");
            errorResponse.put("status", "error");

            return ResponseEntity.status(401).body(errorResponse);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> registerRequest) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "注册功能暂不可用，请联系管理员");
        response.put("status", "error");

        return ResponseEntity.status(503).body(response);
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String token) {
        // 简单验证token
        if (token != null && token.startsWith("Bearer mock-jwt-token-")) {
            Map<String, Object> user = new HashMap<>();

            // 模拟用户信息
            user.put("id", "mock-user-id-001");
            user.put("username", "admin");
            user.put("name", "系统管理员");
            user.put("email", "admin@example.com");
            user.put("department", "系统部");
            user.put("roles", new String[]{"admin"});
            user.put("permissions", new String[]{
                "maintenance.view", "maintenance.add", "maintenance.edit", "maintenance.delete",
                "maintenance.export", "maintenance.update-status",
                "inventory.view", "inventory.add", "inventory.edit", "inventory.delete",
                "inventory.import", "inventory.export", "inventory.check",
                "network.view", "network.add", "network.edit", "network.delete",
                "network.topology", "network.terminal",
                "supplies.view", "supplies.apply", "supplies.approve", "supplies.return",
                "system.user", "system.role", "system.department", "system.log"
            });

            return ResponseEntity.ok(user);
        } else {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "无效的令牌");
            errorResponse.put("status", "error");

            return ResponseEntity.status(401).body(errorResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "登出成功");
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }
}
