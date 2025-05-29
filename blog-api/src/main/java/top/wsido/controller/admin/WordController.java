package top.wsido.controller.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.wsido.model.vo.Result;
import top.wsido.util.word.WordImageUtils;
import top.wsido.util.word.WordTableUtils;
import top.wsido.util.word.WordUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Word文档解析控制器
 *
 * @author wsido
 * @date 2023/10/26
 */
@RestController
@RequestMapping("/admin/word")
@Slf4j
public class WordController {

    @Value("${spring.resources.static-locations:classpath:/static/}")
    private String staticResourcePath;

    @Value("${wsido.upload.word-images:/upload/word-images}")
    private String wordImagesPath;

    /**
     * 上传并解析Word文档
     *
     * @param file 上传的Word文档
     * @param extractImages 是否提取图片(true/false)
     * @return 解析结果
     */
    @PostMapping("/parse")
    public Result parseWord(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "extractImages", defaultValue = "false") boolean extractImages) {
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !(originalFilename.endsWith(".doc") || originalFilename.endsWith(".docx"))) {
            return Result.error("仅支持.doc或.docx格式文件");
        }
        
        try {
            // 保存上传的文件到临时目录
            String fileName = UUID.randomUUID().toString() + getFileExtension(originalFilename);
            File tempDir = new File(System.getProperty("java.io.tmpdir"));
            File tempFile = new File(tempDir, fileName);
            file.transferTo(tempFile);
            
            // 解析文档内容
            String text = WordUtils.extractText(tempFile.getAbsolutePath());
            List<String> paragraphs = WordUtils.extractParagraphs(tempFile.getAbsolutePath());
            
            // 解析表格(只支持.docx)
            List<Map<String, Object>> tables = new ArrayList<>();
            if (originalFilename.endsWith(".docx")) {
                try {
                    List<String[][]> tableDataList = WordTableUtils.extractTables(tempFile.getAbsolutePath());
                    for (int i = 0; i < tableDataList.size(); i++) {
                        String[][] tableData = tableDataList.get(i);
                        Map<String, Object> tableInfo = new HashMap<>();
                        tableInfo.put("index", i + 1);
                        tableInfo.put("rows", tableData.length);
                        tableInfo.put("columns", tableData.length > 0 ? tableData[0].length : 0);
                        tableInfo.put("data", tableData);
                        tableInfo.put("html", WordTableUtils.tableToHtml(tableData));
                        tables.add(tableInfo);
                    }
                } catch (Exception e) {
                    log.warn("提取表格失败", e);
                }
            }
            
            // 提取图片(如果需要且是.docx)
            List<Map<String, Object>> images = new ArrayList<>();
            if (extractImages && originalFilename.endsWith(".docx")) {
                try {
                    // 创建图片保存目录
                    String uploadPath = System.getProperty("user.dir") + File.separator + "upload";
                    String imagesDir = uploadPath + File.separator + "word-images"; 
                    
                    // 确保目录存在
                    File dir = new File(imagesDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    
                    // 提取图片
                    List<String> imagePaths = WordImageUtils.extractImages(tempFile.getAbsolutePath(), imagesDir);
                    String baseUrl = "/upload/word-images";
                    List<String> imageTags = WordImageUtils.getImageTags(imagePaths, baseUrl);
                    
                    // 构建图片信息
                    for (int i = 0; i < imagePaths.size(); i++) {
                        String imagePath = imagePaths.get(i);
                        File imageFile = new File(imagePath);
                        
                        Map<String, Object> imageInfo = new HashMap<>();
                        imageInfo.put("index", i + 1);
                        imageInfo.put("filename", imageFile.getName());
                        imageInfo.put("path", imagePath);
                        imageInfo.put("url", baseUrl + "/" + imageFile.getName());
                        imageInfo.put("html", imageTags.get(i));
                        
                        images.add(imageInfo);
                    }
                } catch (Exception e) {
                    log.warn("提取图片失败", e);
                }
            }
            
            // 删除临时文件
            tempFile.delete();
            
            // 返回解析结果
            Map<String, Object> data = new HashMap<>();
            data.put("text", text);
            data.put("paragraphs", paragraphs);
            data.put("paragraphCount", paragraphs.size());
            data.put("tables", tables);
            data.put("tableCount", tables.size());
            data.put("images", images);
            data.put("imageCount", images.size());
            return Result.ok("解析成功", data);
        } catch (IOException e) {
            log.error("解析Word文档失败", e);
            return Result.error("解析失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            return filename.substring(dotIndex);
        }
        return "";
    }
} 