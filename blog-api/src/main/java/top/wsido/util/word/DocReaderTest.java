package top.wsido.util.word;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 文档读取测试类
 */
public class DocReaderTest {

    public static void main(String[] args) {
        // 目标文档路径
        String docPath = "../../6_毕业设计（论文）-计算机科学与技术专业2025年-熊恒.docx";
        
        // 确保文件存在
        File docFile = new File(docPath);
        if (!docFile.exists()) {
            System.out.println("文件不存在: " + docFile.getAbsolutePath());
            return;
        }
        
        System.out.println("开始分析文档: " + docFile.getName());
        String analysis = TempDocReader.analyzeDocument(docFile.getAbsolutePath());
        
        // 将分析结果保存到文件
        try {
            String outputPath = "文档分析结果.txt";
            try (FileWriter writer = new FileWriter(outputPath)) {
                writer.write(analysis);
            }
            System.out.println("分析结果已保存到: " + new File(outputPath).getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("保存分析结果失败: " + e.getMessage());
        }
        
        // 尝试提取全文内容
        try {
            String content = TempDocReader.extractTextFromDocx(docFile.getAbsolutePath());
            String outputPath = "文档提取内容.txt";
            try (FileWriter writer = new FileWriter(outputPath)) {
                writer.write(content);
            }
            System.out.println("文档内容已保存到: " + new File(outputPath).getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("保存文档内容失败: " + e.getMessage());
        }
    }
} 