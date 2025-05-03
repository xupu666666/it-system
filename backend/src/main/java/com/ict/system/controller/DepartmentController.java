package com.ict.system.controller;

import com.ict.system.model.User;
import com.ict.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashMap;

@RestController
@RequestMapping("/api/system")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DepartmentController {

    @Autowired
    private UserRepository userRepository;

    /**
     * 获取所有部门列表（去重）
     */
    @GetMapping("/departments")
    @PreAuthorize("hasAuthority('system.department')")
    public ResponseEntity<?> getDepartments() {
        List<User> users = userRepository.findAll();
        Set<String> departments = users.stream()
                .map(User::getDepartment)
                .filter(dept -> dept != null && !dept.trim().isEmpty())
                .collect(Collectors.toSet());
        // 转换为前端期望的对象数组
        List<Map<String, String>> result = departments.stream()
                .map(dept -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("id", dept);
                    map.put("name", dept);
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
} 