package com.ict.system.controller.v2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController("testControllerV2")
@RequestMapping("/api/test")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TestController {

    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    /**
     * 公共测试端点
     * @return 测试响应
     */
    @GetMapping("/public")
    public ResponseEntity<?> publicEndpoint() {
        log.info("访问公共测试端点");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "这是一个公共端点");
        response.put("timestamp", System.currentTimeMillis());
        response.put("version", "v2");

        return ResponseEntity.ok(response);
    }

    /**
     * 受保护的测试端点
     * @return 测试响应
     */
    @GetMapping("/protected")
    public ResponseEntity<?> protectedEndpoint() {
        log.info("访问受保护的测试端点");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "这是一个受保护的端点");
        response.put("timestamp", System.currentTimeMillis());
        response.put("version", "v2");

        return ResponseEntity.ok(response);
    }

    /**
     * 回显测试端点
     * @param request 请求体
     * @return 回显响应
     */
    @PostMapping("/echo")
    public ResponseEntity<?> echo(@RequestBody(required = false) Map<String, Object> request) {
        log.info("访问回显测试端点");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "回显服务");
        response.put("timestamp", System.currentTimeMillis());
        response.put("version", "v2");
        response.put("received", request != null ? request : "无请求体");

        return ResponseEntity.ok(response);
    }
}
