-- 数据库结构修改：为支持普通用户注册和博客管理
-- 为user表添加用户类型字段
ALTER TABLE user ADD COLUMN `type` varchar(10) NOT NULL DEFAULT 'user' COMMENT '用户类型：admin/user';

-- 更新现有管理员用户为admin类型
UPDATE user SET `type` = 'admin' WHERE `role` = 'ROLE_admin';

-- 为blog表确保user_id字段正确设置
ALTER TABLE blog MODIFY COLUMN `user_id` bigint(0) NOT NULL COMMENT '文章作者';

-- 添加新的角色
INSERT INTO user (id, username, password, nickname, avatar, email, create_time, update_time, role, type)
VALUES (2, 'test_user', '$2a$10$4wnwMW8Z4Bn6wR4K1YlbquQunlHM/4rvudVBX8oyfx16xeVtI6i7C', '测试用户', '/img/avatar.jpg', 'user@example.com', '2023-05-14 16:47:18', '2023-05-14 16:47:22', 'ROLE_user', 'user'); 