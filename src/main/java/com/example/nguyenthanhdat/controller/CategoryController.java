package com.example.nguyenthanhdat.controller;

import com.example.nguyenthanhdat.model.Category;
import com.example.nguyenthanhdat.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("parentCategories", categoryRepository.findByParentCategoryIsNull());
        return "category-form";
    }

    @PostMapping("/add")
    public String addCategory(
            @Valid Category category,
            BindingResult result,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "imageUrl", required = false) String imageUrl,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("parentCategories", categoryRepository.findByParentCategoryIsNull());
            return "category-form";
        }

        // Xử lý upload file ảnh
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_"
                        + StringUtils.cleanPath(imageFile.getOriginalFilename());
                Path path = Paths.get("src/main/resources/static/images/" + fileName);
                Files.createDirectories(path.getParent());
                Files.write(path, imageFile.getBytes());
                category.setImage("/images/" + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (imageUrl != null && !imageUrl.isEmpty()) {
            // Dùng URL ảnh trực tiếp
            category.setImage(imageUrl);
        }

        categoryRepository.save(category);
        return "redirect:/products";
    }
}
