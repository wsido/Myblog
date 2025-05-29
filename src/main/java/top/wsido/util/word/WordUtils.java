package top.wsido.util.word;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.hwpf.HWPFDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Word文档解析工具类
 * 支持.doc和.docx格式
 * 
 * @author wsido
 * @date 2023/10/26
 */
@Slf4j
public class WordUtils {

    /**
     * 从Word文档中提取纯文本内容
     *
     * @param filePath Word文档路径
     * @return 文档中的文本内容
     * @throws IOException 如果文件读取失败
     */
    public static String extractText(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("文件不存在: " + filePath);
        }
        
        try (InputStream is = new FileInputStream(file)) {
            String fileName = file.getName().toLowerCase();
            
            if (fileName.endsWith(".docx")) {
                return extractDocxText(is);
            } else if (fileName.endsWith(".doc")) {
                return extractDocText(is);
            } else {
                throw new IOException("不支持的文件格式,仅支持.doc和.docx格式");
            }
        } catch (Exception e) {
            log.error("解析Word文档出错", e);
            throw new IOException("解析Word文档失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 提取.docx格式文档的文本
     */
    private static String extractDocxText(InputStream is) throws IOException {
        try (XWPFDocument document = new XWPFDocument(is);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }
    
    /**
     * 提取.doc格式文档的文本
     */
    private static String extractDocText(InputStream is) throws IOException {
        try (HWPFDocument document = new HWPFDocument(is);
             WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        }
    }
    
    /**
     * 从Word文档中提取段落列表
     *
     * @param filePath Word文档路径
     * @return 文档中的段落列表
     * @throws IOException 如果文件读取失败
     */
    public static List<String> extractParagraphs(String filePath) throws IOException {
        String text = extractText(filePath);
        String[] paragraphs = text.split("\\r?\\n");
        
        List<String> result = new ArrayList<>();
        for (String paragraph : paragraphs) {
            if (!paragraph.trim().isEmpty()) {
                result.add(paragraph.trim());
            }
        }
        return result;
    }
} 