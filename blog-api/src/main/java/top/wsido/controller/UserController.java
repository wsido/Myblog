package top.wsido.controller;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import top.wsido.entity.User;
import top.wsido.model.dto.PasswordDTO;
import top.wsido.model.dto.UserDTO;
import top.wsido.model.vo.Result;
import top.wsido.service.UserService;
import top.wsido.util.upload.UploadUtils;

/**
 * @Description: 用户控制器
 * @Author: wsido
 * @Date: 2023-05-15
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    
    @Value("${upload.file.path}")
    private String uploadPath;
    
    /**
     * 用户注册
     *
     * @param userDTO 用户数据
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result register(@RequestBody UserDTO userDTO) {
        return userService.register(userDTO.getUsername(), userDTO.getPassword(), 
                                  userDTO.getNickname(), userDTO.getEmail());
    }
    
    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result getUserInfo() {
        return userService.getUserInfo();
    }
    
    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 更新结果
     */
    @PutMapping("/info")
    public Result updateUserInfo(@RequestBody User user) {
        return userService.updateUserInfo(user);
    }
    
    /**
     * 修改密码
     *
     * @param passwordDTO 密码信息
     * @return 修改结果
     */
    @PutMapping("/password")
    public Result updatePassword(@RequestBody PasswordDTO passwordDTO) {
        return userService.updatePassword(passwordDTO);
    }
    
    /**
     * 修改账号和密码
     *
     * @param user 用户信息（包含用户名和密码）
     * @param jwt JWT令牌
     * @return 修改结果
     */
    @PostMapping("/account")
    public Result account(@RequestBody User user, @RequestHeader(value = "Authorization", defaultValue = "") String jwt) {
        System.out.println("UserController.account(): 收到请求");
        System.out.println("UserController.account(): user = " + user);
        System.out.println("UserController.account(): jwt = " + jwt);
        
        boolean res = userService.changeAccount(user, jwt);
        System.out.println("UserController.account(): 修改结果 = " + res);
        
        return res ? Result.ok("修改成功") : Result.error("修改失败");
    }
    
    /**
     * 用户头像上传
     *
     * @param file 图片文件
     * @return 上传结果
     */
    @PostMapping("/avatar")
    public Result uploadAvatar(@RequestParam("file") MultipartFile file) {
        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/jpeg") && !contentType.startsWith("image/png"))) {
            return Result.error("只支持JPEG/PNG格式的图片");
        }
        
        // 检查文件大小（限制为2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            return Result.error("图片大小不能超过2MB");
        }
        
        try {
            File avatarDir = new File(uploadPath + "avatar");
            if (!avatarDir.exists()) {
                avatarDir.mkdirs();
            }
            
            byte[] fileBytes = file.getBytes();
            String fileType;
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null && originalFilename.contains(".")) {
                fileType = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            } else if (contentType.contains("/")) {
                fileType = contentType.substring(contentType.lastIndexOf("/") + 1);
            } else {
                fileType = "jpg"; 
            }
            
            UploadUtils.ImageResource imageResource = new UploadUtils.ImageResource(fileBytes, fileType);
            String avatarUrl = UploadUtils.upload(imageResource);
            
            // 更新用户头像信息 - 通过userService来做，它会处理当前用户
            User userToUpdateAvatar = new User();
            userToUpdateAvatar.setAvatar(avatarUrl);
            Result updateResult = userService.updateUserInfo(userToUpdateAvatar);
            
            if (updateResult.getCode() == 200) {
                return Result.ok("上传成功", avatarUrl);
            } else {
                return Result.error("头像信息更新失败: " + updateResult.getMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试接口 - 无需验证
     *
     * @return 简单测试结果
     */
    @GetMapping("/test")
    public Result test() {
        System.out.println("UserController.test(): 测试接口被调用");
        return Result.ok("测试成功，接口正常");
    }
} 