@echo off
echo 清除Maven缓存并重新构建项目...

REM 创建Maven仓库配置
echo 创建Maven设置文件...
@echo ^<?xml version="1.0" encoding="UTF-8"?^> > %USERPROFILE%\.m2\settings.xml
@echo ^<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" >> %USERPROFILE%\.m2\settings.xml
@echo           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" >> %USERPROFILE%\.m2\settings.xml
@echo           xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd"^> >> %USERPROFILE%\.m2\settings.xml
@echo. >> %USERPROFILE%\.m2\settings.xml
@echo     ^<mirrors^> >> %USERPROFILE%\.m2\settings.xml
@echo         ^<!-- 中央仓库 --^> >> %USERPROFILE%\.m2\settings.xml
@echo         ^<mirror^> >> %USERPROFILE%\.m2\settings.xml
@echo             ^<id^>central^</id^> >> %USERPROFILE%\.m2\settings.xml
@echo             ^<name^>Maven Central^</name^> >> %USERPROFILE%\.m2\settings.xml
@echo             ^<url^>https://repo1.maven.org/maven2/^</url^> >> %USERPROFILE%\.m2\settings.xml
@echo             ^<mirrorOf^>central^</mirrorOf^> >> %USERPROFILE%\.m2\settings.xml
@echo         ^</mirror^> >> %USERPROFILE%\.m2\settings.xml
@echo     ^</mirrors^> >> %USERPROFILE%\.m2\settings.xml
@echo ^</settings^> >> %USERPROFILE%\.m2\settings.xml

REM 备份POI依赖相关缓存
mkdir %USERPROFILE%\.m2\backup
if exist %USERPROFILE%\.m2\repository\org\apache\poi (
    echo 备份已有POI缓存...
    xcopy /E /I /Y %USERPROFILE%\.m2\repository\org\apache\poi %USERPROFILE%\.m2\backup\poi
    rmdir /S /Q %USERPROFILE%\.m2\repository\org\apache\poi
)

REM 提示用户
echo.
echo Maven缓存清理完成。
echo 现在，您可以使用IDE重新构建项目。
echo 在IDE中右键点击项目，选择"Maven -> Update Project..."或等效操作。
echo.
echo 或者您可以运行以下命令手动构建:
echo mvn clean install -U
echo.
pause 