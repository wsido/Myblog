# Word文档处理功能使用说明

## 功能概述

本项目实现了基于Apache POI的Word文档解析功能，可以：

1. 提取Word文档（.doc和.docx）中的文本内容
2. 提取文档中的段落
3. 解析文档中的表格并转换为HTML
4. 提取文档中的图片（仅支持.docx）

## 解决Maven依赖问题

当前项目依赖Apache POI库，如果遇到Maven依赖下载问题，请按以下步骤操作：

### 方法1：运行提供的脚本（推荐）

1. 在项目根目录找到`clear-cache-and-run.bat`脚本
2. 双击运行，它会：
   - 创建新的Maven配置
   - 清除POI相关缓存
   - 提供后续操作指南

### 方法2：手动清除缓存

1. 手动删除Maven本地仓库中的POI相关缓存：
   - 找到`%USERPROFILE%\.m2\repository\org\apache\poi`目录
   - 删除此目录
2. 修改Maven设置文件`%USERPROFILE%\.m2\settings.xml`，添加以下内容：
   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
       <mirrors>
           <!-- 中央仓库 -->
           <mirror>
               <id>central</id>
               <name>Maven Central</name>
               <url>https://repo1.maven.org/maven2/</url>
               <mirrorOf>central</mirrorOf>
           </mirror>
       </mirrors>
   </settings>
   ```
3. 在IDE中右键点击项目，选择"Maven -> Update Project..."或等效操作

### 方法3：降低POI版本

如果上述方法仍无法解决问题，可以尝试降低POI版本：

1. 修改`pom.xml`文件中的POI依赖版本：
   ```xml
   <dependency>
       <groupId>org.apache.poi</groupId>
       <artifactId>poi</artifactId>
       <version>3.17</version>
   </dependency>
   <dependency>
       <groupId>org.apache.poi</groupId>
       <artifactId>poi-ooxml</artifactId>
       <version>3.17</version>
   </dependency>
   <dependency>
       <groupId>org.apache.poi</groupId>
       <artifactId>poi-scratchpad</artifactId>
       <version>3.17</version>
   </dependency>
   ```

## 使用方法

### API访问

可以通过`/admin/word/parse`接口上传Word文档并解析：

```
POST /admin/word/parse
参数：
- file: Word文档文件
- extractImages (可选): 是否提取图片，默认false
```

### 返回数据示例

```json
{
  "code": 200,
  "msg": "解析成功",
  "data": {
    "text": "文档全文内容...",
    "paragraphs": ["段落1", "段落2", ...],
    "paragraphCount": 10,
    "tables": [
      {
        "index": 1,
        "rows": 3,
        "columns": 4,
        "data": [["表头1", "表头2"], ["数据1", "数据2"]],
        "html": "<table>...</table>"
      }
    ],
    "tableCount": 1,
    "images": [
      {
        "index": 1,
        "filename": "image.png",
        "path": "/upload/word-images/image.png",
        "url": "/upload/word-images/image.png",
        "html": "<img src='/upload/word-images/image.png' alt='Extracted Image' />"
      }
    ],
    "imageCount": 1
  }
}
```

## 直接使用工具类

也可以在代码中直接使用工具类：

```java
// 提取文本
String text = WordUtils.extractText("path/to/file.docx");

// 提取段落
List<String> paragraphs = WordUtils.extractParagraphs("path/to/file.docx");

// 提取表格
List<String[][]> tables = WordTableUtils.extractTables("path/to/file.docx");

// 提取图片
List<String> imagePaths = WordImageUtils.extractImages("path/to/file.docx", "output/dir");
```

## 仅支持.docx的简化版工具

如果遇到依赖问题，还可以使用`SimpleWordUtils`类，它仅支持.docx文件但不依赖scratchpad库：

```java
// 提取文本
String text = SimpleWordUtils.extractText("path/to/file.docx");

// 提取段落
List<String> paragraphs = SimpleWordUtils.extractParagraphs("path/to/file.docx");

// 提取表格
List<String[][]> tables = SimpleWordUtils.extractTables("path/to/file.docx");
``` 