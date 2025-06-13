package top.wsido.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.wsido.model.vo.Result;
import top.wsido.util.CoverGeneratorUtil;

@RestController
@RequestMapping("/admin")
public class CoverController {

    @Autowired
    private CoverGeneratorUtil coverGeneratorUtil;

    @GetMapping("/covers/generate")
    public Result generateCover(@RequestParam String title) {
        if (title == null || title.trim().isEmpty()) {
            return Result.error("Title cannot be empty.");
        }
        try {
            String coverUrl = coverGeneratorUtil.generate(title);
            return Result.ok("Cover generated successfully.", coverUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("Failed to generate cover image: " + e.getMessage());
        }
    }
} 