package top.wsido.util.word;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Word文档表格解析工具类
 * 处理.docx格式中的表格
 * 
 * @author wsido
 * @date 2023/10/26
 */
@Slf4j
public class WordTableUtils {

    /**
     * 从Word文档中提取所有表格数据
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
            throw new IOException("仅支持.docx格式文件提取表格");
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
                    List<XWPFTableCell> cells = row.getTableCells();
                    
                    for (int j = 0; j < cells.size(); j++) {
                        XWPFTableCell cell = cells.get(j);
                        tableData[i][j] = cell.getText().trim();
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
    
    /**
     * 将表格数据转换为HTML表格字符串
     *
     * @param tableData 表格数据(二维数组)
     * @return HTML表格字符串
     */
    public static String tableToHtml(String[][] tableData) {
        if (tableData == null || tableData.length == 0) {
            return "";
        }
        
        StringBuilder html = new StringBuilder();
        html.append("<table border='1' cellspacing='0' cellpadding='5'>\n");
        
        for (String[] row : tableData) {
            html.append("  <tr>\n");
            for (String cell : row) {
                html.append("    <td>").append(cell == null ? "" : cell).append("</td>\n");
            }
            html.append("  </tr>\n");
        }
        
        html.append("</table>");
        return html.toString();
    }
} 