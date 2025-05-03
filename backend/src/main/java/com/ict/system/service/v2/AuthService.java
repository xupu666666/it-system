package com.ict.system.service.v2;

import com.ict.system.model.v2.User;
import com.ict.system.payload.request.LoginRequest;
import com.ict.system.payload.request.SignupRequest;
import com.ict.system.payload.response.JwtResponse;

public interface AuthService {

    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @return JWT响应
     */
    JwtResponse login(LoginRequest loginRequest);

    /**
     * 用户注册
     * @param signupRequest 注册请求
     * @return 注册的用户
     */
    User register(SignupRequest signupRequest);

    /**
     * 刷新令牌
     * @param username 用户名
     * @return 新的JWT令牌
     */
    String refreshToken(String username);

    /**
     * 获取当前登录用户
     * @return 当前用户
     */
    User getCurrentUser();

    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);
}
