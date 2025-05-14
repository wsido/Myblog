package top.wsido.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import top.wsido.model.vo.Result;
import top.wsido.service.UserStatsService;

@RestController
@RequestMapping("/user/stats")
public class UserStatsController {

    @Autowired
    private UserStatsService userStatsService;

    /**
     * 获取当前登录用户的统计信息
     * @return 包含统计数据的Result对象
     */
    @GetMapping
    public Result getUserStats() {
        return userStatsService.getUserStats();
    }
} 