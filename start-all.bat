@echo off
title NBlog Project Starter

echo ==========================================================
echo.
echo  NBlog 一键启动脚本
echo.
echo  !! 重要前提 !!
echo  - 本项目后端服务依赖于 Redis。
echo  - 在运行此脚本前，请确保您的计算机上已经安装并启动了 Redis 服务。
echo  - Redis 默认应运行在 localhost:6379。
echo  - 如果您没有安装 Redis，推荐使用 Docker 启动一个实例:
echo    docker run -d --name nblog-redis -p 6379:6379 redis
echo.
echo ==========================================================
echo.
echo  说明:
echo  1. 本脚本将分别启动三个组件:
echo     - 后端 API (blog-api)
echo     - 后台管理系统 (blog-cms)
echo     - 博客前台页面 (blog-view)
echo.
echo  2. 每个组件将在一个独立的命令行窗口中运行。
echo     请不要关闭这些新打开的窗口，否则会中止服务。
echo.
echo  3. 启动过程需要一些时间，请耐心等待。
echo     特别是第一次运行时，Maven 和 npm 会下载依赖。
echo.
echo  4. 请确保您已正确安装 Java, Maven 和 Node.js 环境。
echo.
echo ==========================================================
echo.

echo 正在启动后端服务 (blog-api)...
start "NBlog API" cmd /c "cd blog-api && mvn spring-boot:run"

echo.
echo 正在启动后台管理系统 (blog-cms)...
start "NBlog CMS" cmd /c "cd blog-cms && npm install && npm run serve"

echo.
echo 正在启动博客前台 (blog-view)...
start "NBlog View" cmd /c "cd blog-view && npm install && npm run serve"

echo.
echo 所有启动命令已发出。请查看新打开的窗口中的日志输出。
echo.

pause 