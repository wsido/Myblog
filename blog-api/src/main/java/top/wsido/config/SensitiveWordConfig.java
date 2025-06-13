package top.wsido.config;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.api.IWordAllow;
import com.github.houbb.sensitive.word.api.IWordDeny;
import com.github.houbb.sensitive.word.support.allow.WordAllows;
import com.github.houbb.sensitive.word.support.deny.WordDenys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 敏感词过滤配置
 */
@Configuration
public class SensitiveWordConfig {

    private static final String MY_SENSITIVE_WORDS_PATH = "my_sensitive_words.txt";
    private static final String MY_ALLOW_WORDS_PATH = "my_allow_words.txt";

    /**
     * 自定义敏感词加载（可从文件、数据库等加载）
     */
    public static class MyWordDeny implements IWordDeny {
        @Override
        public List<String> deny() {
            try {
                ClassPathResource resource = new ClassPathResource(MY_SENSITIVE_WORDS_PATH);
                if (resource.exists()) {
                    try (InputStream inputStream = resource.getInputStream()) {
                        String content = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
                        return Arrays.stream(content.split("\\r?\\n"))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty() && !s.startsWith("#"))
                                .collect(Collectors.toList());
                    }
                }
            } catch (IOException e) {
                // log.error("加载自定义敏感词文件失败: {}", MY_SENSITIVE_WORDS_PATH, e); // 需要引入SLF4J的log
                System.err.println("加载自定义敏感词文件失败: " + MY_SENSITIVE_WORDS_PATH + ", " + e.getMessage());
            }
            return Collections.emptyList();
        }
    }

    /**
     * 自定义允许词加载（白名单）
     */
    public static class MyWordAllow implements IWordAllow {
        @Override
        public List<String> allow() {
            try {
                ClassPathResource resource = new ClassPathResource(MY_ALLOW_WORDS_PATH);
                if (resource.exists()) {
                    try (InputStream inputStream = resource.getInputStream()) {
                        String content = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
                        return Arrays.stream(content.split("\\r?\\n"))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty() && !s.startsWith("#"))
                                .collect(Collectors.toList());
                    }
                }
            } catch (IOException e) {
                 // log.error("加载自定义允许词文件失败: {}", MY_ALLOW_WORDS_PATH, e); // 需要引入SLF4J的log
                System.err.println("加载自定义允许词文件失败: " + MY_ALLOW_WORDS_PATH + ", " + e.getMessage());
            }
            return Collections.emptyList();
        }
    }

    @Bean
    public SensitiveWordBs sensitiveWordBs() {
        return SensitiveWordBs.newInstance()
                .wordDeny(WordDenys.chains(WordDenys.system(), new MyWordDeny()))       // 自定义敏感词
                .wordAllow(WordAllows.chains(WordAllows.system(), new MyWordAllow()))       // 自定义允许通过的词
                .ignoreCase(true)               // 忽略大小写
                .ignoreWidth(true)              // 忽略半角圆角
                .ignoreNumStyle(true)           // 忽略数字的写法
                .ignoreChineseStyle(true)       // 忽略中文的书写格式
                .ignoreEnglishStyle(true)       // 忽略英文的书写格式
                // .ignoreRepeat(false)         // 忽略重复词，默认false，可根据需要开启
                .enableNumCheck(true)           // 是否启用数字检测，默认true
                .enableEmailCheck(true)         // 是否启用邮箱检测，默认true
                .enableUrlCheck(true)           // 是否启用链接检测，默认true
                // .numCheckLen(8)              // 数字检测自定义指定长度，默认8
                // .replaceChar('*')            // 指定替换字符，默认'*'
                .init(); // 初始化，构建 DFA 数据结构
    }
} 