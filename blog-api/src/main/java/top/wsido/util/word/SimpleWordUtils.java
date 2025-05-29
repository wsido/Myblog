package top.wsido.util.word;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 简化版Word文档解析工具类
 * 只支持.docx格式，不依赖scratchpad库
 * 
 * @author wsido
 * @date 2023/10/26
 */
@Slf4j
public class SimpleWordUtils {

    /**
     * 从Word文档(.docx)中提取纯文本内容
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
        
        if (!filePath.toLowerCase().endsWith(".docx")) {
            throw new IOException("此方法仅支持.docx格式");
        }
        
        try (InputStream is = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(is)) {
            
            StringBuilder text = new StringBuilder();
            
            // 提取段落
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                text.append(paragraph.getText()).append("\n");
            }
            
            // 提取表格内容
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    text.append(row.getTableCells().stream()
                            .map(cell -> cell.getText())
                            .collect(Collectors.joining(" | ")))
                            .append("\n");
                }
                text.append("\n");
            }
            
            return text.toString();
        } catch (Exception e) {
            log.error("解析Word文档出错", e);
            throw new IOException("解析Word文档失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 从.docx文档中提取段落列表
     *
     * @param filePath Word文档路径
     * @return 文档中的段落列表
     * @throws IOException 如果文件读取失败
     */
    public static List<String> extractParagraphs(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("文件不存在: " + filePath);
        }
        
        if (!filePath.toLowerCase().endsWith(".docx")) {
            throw new IOException("此方法仅支持.docx格式");
        }
        
        try (InputStream is = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(is)) {
            
            List<String> paragraphs = new ArrayList<>();
            
            // 提取段落
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText().trim();
                if (!text.isEmpty()) {
                    paragraphs.add(text);
                }
            }
            
            return paragraphs;
        } catch (Exception e) {
            log.error("提取段落出错", e);
            throw new IOException("提取段落失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 从Word文档中提取表格数据
     *
     * @param filePath Word文档路径(.docx格式)
     * @return 表格数据列表(每个表格是一个二维数组)
     * @throws IOException 如果文件读取失败
     */
    public static List<String[][]> extractTables(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("文件不存在: " + filePath);
        }
        
        if (!filePath.toLowerCase().endsWith(".docx")) {
            throw new IOException("此方法仅支持.docx格式");
        }
        
        List<String[][]> tables = new ArrayList<>();
        
        try (InputStream is = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(is)) {
            
            // 遍历所有表格
            for (XWPFTable table : document.getTables()) {
                List<XWPFTableRow> rows = table.getRows();
                if (rows.isEmpty()) {
                    continue;
                }
                
                int rowCount = rows.size();
                // 确定最大列数
                int maxCols = 0;
                for (XWPFTableRow row : rows) {
                    int cellCount = row.getTableCells().size();
                    if (cellCount > maxCols) {
                        maxCols = cellCount;
                    }
                }
                
                // 创建表格数据数组
                String[][] tableData = new String[rowCount][maxCols];
                
                // 填充表格数据
                for (int i = 0; i < rowCount; i++) {
                    XWPFTableRow row = rows.get(i);
                    for (int j = 0; j < row.getTableCells().size(); j++) {
                        tableData[i][j] = row.getTableCells().get(j).getText().trim();
                    }
                }
                
                tables.add(tableData);
            }
            
            return tables;
        } catch (Exception e) {
            log.error("解析Word表格出错", e);
            throw new IOException("解析Word表格失败: " + e.getMessage(), e);
        }
    }
} 