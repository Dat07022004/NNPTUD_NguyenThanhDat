package com.example.nguyenthanhdat.config;

import com.example.nguyenthanhdat.model.Category;
import com.example.nguyenthanhdat.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

/**
 * Inject danh sách category vào tất cả các view (kể cả layout)
 * để hiển thị menu cấp 2 động từ cơ sở dữ liệu.
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private CategoryRepository categoryRepository;

    @ModelAttribute("allCategories")
    public List<Category> populateCategories() {
        return categoryRepository.findByParentCategoryIsNull();
    }
}
