package top.wsido.util;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class CoverGeneratorUtil {

    private static final int IMAGE_WIDTH = 1600;
    private static final int IMAGE_HEIGHT = 900;
    private static final String TEMPLATE_PATH = "/static/templates/template.jpg";
    private static final String FONT_PATH = "/static/templates/SourceHanSans.otf";
    private static final String OUTPUT_DIR = "blog-api/src/main/resources/static/covers/";
    private static Font customFont;

    static {
        try (InputStream is = CoverGeneratorUtil.class.getResourceAsStream(FONT_PATH)) {
            if (is == null) {
                throw new RuntimeException("Font file not found at: " + FONT_PATH);
            }
            customFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(80f);
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to a default font if custom font fails to load
            customFont = new Font("Serif", Font.BOLD, 80);
        }
    }

    public String generate(String title) throws Exception {
        // 加载背景模板
        InputStream is = CoverGeneratorUtil.class.getResourceAsStream(TEMPLATE_PATH);
        if (is == null) {
            throw new RuntimeException("Template image not found at: " + TEMPLATE_PATH);
        }
        BufferedImage image = ImageIO.read(is);
        Graphics2D g2d = image.createGraphics();

        // 设置高质量渲染
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // 设置字体和颜色
        g2d.setFont(customFont);
        g2d.setColor(Color.WHITE);

        FontMetrics metrics = g2d.getFontMetrics();
        int lineHeight = metrics.getHeight();
        
        // 处理文字换行
        List<String> lines = new ArrayList<>();
        String[] words = title.split(" ");
        String currentLine = "";
        for (String word : words) {
            if (metrics.stringWidth(currentLine + word) < IMAGE_WIDTH - 200) {
                currentLine += word + " ";
            } else {
                lines.add(currentLine.trim());
                currentLine = word + " ";
            }
        }
        lines.add(currentLine.trim());

        // 计算文字起始Y坐标，使其整体居中
        int totalTextHeight = lineHeight * lines.size();
        int startY = (IMAGE_HEIGHT - totalTextHeight) / 2 + metrics.getAscent();
        
        // 逐行绘制文字，水平居中
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int stringWidth = metrics.stringWidth(line);
            int startX = (IMAGE_WIDTH - stringWidth) / 2;
            g2d.drawString(line, startX, startY + i * lineHeight);
        }

        g2d.dispose();

        // 保存并返回文件路径
        String fileName = UUID.randomUUID().toString() + ".jpg";
        File outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        File outputFile = new File(outputDir, fileName);
        ImageIO.write(image, "jpg", outputFile);

        return "/covers/" + fileName;
    }
} 