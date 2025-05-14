package top.wsido.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import top.wsido.model.vo.Result;
import top.wsido.service.UserFavoriteService;

@RestController
@RequestMapping("/user/favorites")
public class UserFavoriteController {

    @Autowired
    private UserFavoriteService userFavoriteService;

    /**
     * 获取当前登录用户的收藏列表（分页）
     * @param pageNum 页码，默认为1
     * @param pageSize 每页数量，默认为10
     * @return 分页的收藏博客列表
     */
    @GetMapping
    public Result getFavorites(@RequestParam(defaultValue = "1") Integer pageNum,
                               @RequestParam(defaultValue = "10") Integer pageSize) {
        return userFavoriteService.getFavorites(pageNum, pageSize);
    }

    /**
     * 添加博客到收藏
     * @param params 请求体，应包含 "blogId"
     * @return 操作结果
     */
    @PostMapping
    public Result addFavorite(@RequestBody Map<String, Long> params) {
        Long blogId = params.get("blogId");
        if (blogId == null) {
            return Result.error("博客ID不能为空");
        }
        return userFavoriteService.addFavorite(blogId);
    }

    /**
     * 从收藏中移除博客
     * @param blogId 要移除的博客ID (路径变量)
     * @return 操作结果
     */
    @DeleteMapping("/{blogId}")
    public Result removeFavorite(@PathVariable Long blogId) {
        return userFavoriteService.removeFavorite(blogId);
    }

    /**
     * 检查博客是否已被当前用户收藏
     * @param blogId 博客ID (路径变量)
     * @return 操作结果，包含isFavorite状态
     */
    @GetMapping("/check/{blogId}")
    public Result checkFavoriteStatus(@PathVariable Long blogId) {
        return userFavoriteService.checkFavoriteStatus(blogId);
    }
} 