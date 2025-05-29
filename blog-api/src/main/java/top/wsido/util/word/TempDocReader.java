package top.wsido.util.word;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 临时简易文档内容提取工具
 * 不使用Apache POI，仅提取基本文本内容
 */
public class TempDocReader {
    
    /**
     * 从文档中提取纯文本内容（简易方式）
     * 注意：这只是一个临时方案，只能提取到部分文本内容
     * 
     * @param filePath 文件路径
     * @return 提取的文本内容
     */
    public static String extractTextFromDocx(String filePath) {
        StringBuilder content = new StringBuilder();
        try {
            // 使用ProcessBuilder执行powershell命令来读取文档内容
            ProcessBuilder pb = new ProcessBuilder(
                "powershell", "-Command", 
                "if (Test-Path '" + filePath + "') { " +
                "  $word = New-Object -ComObject Word.Application; " +
                "  $word.Visible = $false; " +
                "  $doc = $word.Documents.Open('" + filePath + "'); " +
                "  $text = $doc.Content.Text; " +
                "  $doc.Close(); " +
                "  $word.Quit(); " +
                "  [System.Runtime.InteropServices.Marshal]::ReleaseComObject($doc) | Out-Null; " +
                "  [System.Runtime.InteropServices.Marshal]::ReleaseComObject($word) | Out-Null; " +
                "  [System.GC]::Collect(); " +
                "  [System.GC]::WaitForPendingFinalizers(); " +
                "  Write-Output $text; " +
                "}"
            );
            
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
            
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            content.append("文档读取失败: ").append(e.getMessage());
        }
        
        return content.toString();
    }
    
    /**
     * 从文档中提取段落（简易方式）
     * 
     * @param content 文档内容
     * @return 段落列表
     */
    public static List<String> extractParagraphs(String content) {
        List<String> paragraphs = new ArrayList<>();
        
        // 将内容按空行分割成段落
        String[] parts = content.split("\\n\\s*\\n");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                paragraphs.add(trimmed);
            }
        }
        
        return paragraphs;
    }
    
    /**
     * 从文本中提取标题
     * 
     * @param content 文档内容
     * @return 标题列表
     */
    public static List<String> extractHeadings(String content) {
        List<String> headings = new ArrayList<>();
        
        // 提取数字编号的标题（如"1. 引言"、"2.1 系统设计"等）
        Pattern pattern = Pattern.compile("^\\s*(\\d+(\\.\\d+)*\\s+.+)$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            headings.add(matcher.group(1).trim());
        }
        
        return headings;
    }
    
    /**
     * 从文本中提取特定部分
     * 
     * @param content 文档内容
     * @param sectionName 部分名称（如"摘要"、"引言"等）
     * @return 提取的内容
     */
    public static String extractSection(String content, String sectionName) {
        int startIndex = content.indexOf(sectionName);
        if (startIndex == -1) {
            return "未找到部分: " + sectionName;
        }
        
        // 尝试找到下一个部分
        Pattern pattern = Pattern.compile("\\d+(\\.\\d+)*\\s+\\S+");
        Matcher matcher = pattern.matcher(content.substring(startIndex + sectionName.length()));
        
        if (matcher.find()) {
            int endIndex = startIndex + sectionName.length() + matcher.start();
            return content.substring(startIndex, endIndex).trim();
        } else {
            // 如果找不到下一个部分，则返回从开始位置到结尾的内容
            return content.substring(startIndex).trim();
        }
    }
    
    /**
     * 分析文档基本结构
     * 
     * @param filePath 文件路径
     * @return 文档分析结果
     */
    public static String analyzeDocument(String filePath) {
        StringBuilder result = new StringBuilder();
        
        try {
            String content = extractTextFromDocx(filePath);
            
            // 提取标题
            List<String> headings = extractHeadings(content);
            
            result.append("== 文档分析结果 ==\n\n");
            result.append("文件名: ").append(new File(filePath).getName()).append("\n");
            result.append("字符数: ").append(content.length()).append("\n");
            result.append("估计段落数: ").append(extractParagraphs(content).size()).append("\n\n");
            
            // 输出文档结构（标题）
            result.append("== 文档结构 ==\n\n");
            for (String heading : headings) {
                result.append(heading).append("\n");
            }
            
            // 尝试提取摘要
            result.append("\n== 摘要 ==\n\n");
            String abstract_ = extractSection(content, "摘要");
            result.append(abstract_).append("\n");
            
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "文档分析失败: " + e.getMessage();
        }
    }
} 