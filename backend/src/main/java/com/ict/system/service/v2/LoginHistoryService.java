package com.ict.system.service.v2;

import com.ict.system.model.v2.LoginHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

public interface LoginHistoryService {

    /**
     * 记录登录成功
     * @param username 用户名
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 登录历史记录
     */
    LoginHistory recordLoginSuccess(String username, String ipAddress, String userAgent);

    /**
     * 记录登录失败
     * @param username 用户名
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @param reason 失败原因
     * @return 登录历史记录
     */
    LoginHistory recordLoginFailure(String username, String ipAddress, String userAgent, String reason);

    /**
     * 获取用户的登录历史
     * @param username 用户名
     * @return 登录历史列表
     */
    List<LoginHistory> getUserLoginHistory(String username);

    /**
     * 分页获取用户的登录历史
     * @param username 用户名
     * @param pageable 分页参数
     * @return 登录历史分页结果
     */
    Page<LoginHistory> getUserLoginHistory(String username, Pageable pageable);

    /**
     * 获取特定时间段内的登录历史
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 登录历史列表
     */
    List<LoginHistory> getLoginHistoryBetween(Date startTime, Date endTime);

    /**
     * 获取特定时间段内特定用户的登录历史
     * @param username 用户名
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 登录历史列表
     */
    List<LoginHistory> getUserLoginHistoryBetween(String username, Date startTime, Date endTime);

    /**
     * 获取特定状态的登录历史
     * @param status 状态
     * @return 登录历史列表
     */
    List<LoginHistory> getLoginHistoryByStatus(String status);

    /**
     * 获取特定IP地址的登录历史
     * @param ipAddress IP地址
     * @return 登录历史列表
     */
    List<LoginHistory> getLoginHistoryByIpAddress(String ipAddress);

    /**
     * 获取用户最近的登录历史
     * @param username 用户名
     * @return 登录历史列表
     */
    List<LoginHistory> getRecentLoginHistory(String username);

    /**
     * 获取用户的登录失败记录
     * @param username 用户名
     * @return 登录历史列表
     */
    List<LoginHistory> getLoginFailures(String username);

    /**
     * 统计特定时间段内的登录失败次数
     * @param username 用户名
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 失败次数
     */
    long countLoginFailures(String username, Date startTime, Date endTime);

    /**
     * 清理旧的登录历史
     * @param days 保留天数
     * @return 清理的记录数
     */
    int cleanupOldLoginHistory(int days);
}
