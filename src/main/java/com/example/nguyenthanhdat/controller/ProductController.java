package com.example.nguyenthanhdat.controller;

import com.example.nguyenthanhdat.model.Product;
import com.example.nguyenthanhdat.repository.CategoryRepository;
import com.example.nguyenthanhdat.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.util.StringUtils;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    public String listProducts(
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            Model model) {
        if (categoryId != null) {
            model.addAttribute("products", productRepository.findByCategoryIdOrderByPromotedDesc(categoryId));
            categoryRepository.findById(categoryId).ifPresent(cat -> model.addAttribute("selectedCategory", cat));
        } else {
            model.addAttribute("products", productRepository.findAllOrderByPromotedDesc());
        }
        // Danh sách riêng sản phẩm khuyến mãi để hiển thị section KM
        model.addAttribute("promotedProducts", productRepository.findByPromotedTrue());
        return "product-list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findAll());
        return "product-form";
    }

    @PostMapping("/add")
    public String addProduct(@Valid Product product, BindingResult result,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "imageUrl", required = false) String imageUrl,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            return "product-form";
        }
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_"
                        + StringUtils.cleanPath(imageFile.getOriginalFilename());
                Path path = Paths.get("src/main/resources/static/images/" + fileName);
                Files.createDirectories(path.getParent());
                Files.write(path, imageFile.getBytes());
                product.setImage("/images/" + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (imageUrl != null && !imageUrl.isEmpty()) {
            product.setImage(imageUrl);
        }
        normalizePromotionFields(product);
        productRepository.save(product);
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryRepository.findAll());
        return "product-form";
    }

    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable("id") Long id, @Valid Product product, BindingResult result,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "imageUrl", required = false) String imageUrl,
            Model model) {
        if (result.hasErrors()) {
            product.setId(id);
            model.addAttribute("categories", categoryRepository.findAll());
            return "product-form";
        }
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_"
                        + StringUtils.cleanPath(imageFile.getOriginalFilename());
                Path path = Paths.get("src/main/resources/static/images/" + fileName);
                Files.createDirectories(path.getParent());
                Files.write(path, imageFile.getBytes());
                product.setImage("/images/" + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (imageUrl != null && !imageUrl.isEmpty()) {
            product.setImage(imageUrl);
        } else {
            // Keep existing image if no new file or URL provided
            Product existingProduct = productRepository.findById(id).orElse(null);
            if (existingProduct != null && existingProduct.getImage() != null) {
                product.setImage(existingProduct.getImage());
            }
        }
        normalizePromotionFields(product);
        productRepository.save(product);
        return "redirect:/products";
    }

    private void normalizePromotionFields(Product product) {
        if (product.getPromotionQuantity() == null || product.getPromotionQuantity() < 0) {
            product.setPromotionQuantity(0);
        }

        if (!Boolean.TRUE.equals(product.getPromoted()) || product.getPromotionQuantity() == 0) {
            product.setPromoted(false);
            product.setPromotionQuantity(0);
            product.setPromotionPrice(null);
            return;
        }

        if (product.getPromotionPrice() == null || product.getPromotionPrice() <= 0 || product.getPromotionPrice() >= product.getPrice()) {
            product.setPromoted(false);
            product.setPromotionQuantity(0);
            product.setPromotionPrice(null);
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        productRepository.delete(product);
        return "redirect:/products";
    }
}
