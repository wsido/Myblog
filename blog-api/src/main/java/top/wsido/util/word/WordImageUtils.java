package top.wsido.util.word;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Word文档图片提取工具类
 * 支持从.docx文档中提取图片
 * 
 * @author wsido
 * @date 2023/10/26
 */
@Slf4j
public class WordImageUtils {

    /**
     * 从Word文档中提取所有图片并保存到指定目录
     *
     * @param filePath Word文档路径(.docx格式)
     * @param outputDir 图片保存目录
     * @return 保存的图片路径列表
     * @throws IOException 如果文件读取或写入失败
     */
    public static List<String> extractImages(String filePath, String outputDir) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("文件不存在: " + filePath);
        }
        
        if (!filePath.toLowerCase().endsWith(".docx")) {
            throw new IOException("仅支持.docx格式文件提取图片");
        }
        
        // 确保输出目录存在
        File dir = new File(outputDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("无法创建输出目录: " + outputDir);
        }
        
        List<String> savedImagePaths = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {
            
            List<XWPFPictureData> pictures = document.getAllPictures();
            
            for (XWPFPictureData picture : pictures) {
                String extension = picture.suggestFileExtension();
                
                // 生成唯一文件名
                String imageName = UUID.randomUUID().toString() + "." + extension;
                String imagePath = outputDir + File.separator + imageName;
                
                // 保存图片
                try (FileOutputStream fos = new FileOutputStream(imagePath)) {
                    fos.write(picture.getData());
                    savedImagePaths.add(imagePath);
                }
            }
            
            return savedImagePaths;
        } catch (Exception e) {
            log.error("提取Word图片出错", e);
            throw new IOException("提取Word图片失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取图片的HTML标签列表
     *
     * @param imagePaths 图片路径列表
     * @param baseUrl 图片的基础URL(用于网页访问)
     * @return HTML图片标签列表
     */
    public static List<String> getImageTags(List<String> imagePaths, String baseUrl) {
        List<String> imageTags = new ArrayList<>();
        
        for (String imagePath : imagePaths) {
            File imageFile = new File(imagePath);
            String fileName = imageFile.getName();
            String imageUrl = baseUrl + (baseUrl.endsWith("/") ? "" : "/") + fileName;
            
            String imageTag = "<img src='" + imageUrl + "' alt='Extracted Image' style='max-width:100%;'/>";
            imageTags.add(imageTag);
        }
        
        return imageTags;
    }
} 