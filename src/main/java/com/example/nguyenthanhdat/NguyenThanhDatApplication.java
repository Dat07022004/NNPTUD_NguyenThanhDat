package com.example.nguyenthanhdat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NguyenThanhDatApplication {

	public static void main(String[] args) {
		SpringApplication.run(NguyenThanhDatApplication.class, args);
	}

	@org.springframework.context.annotation.Bean
	public org.springframework.boot.CommandLineRunner seedCategories(com.example.nguyenthanhdat.repository.CategoryRepository repository) {
		return (args) -> {
			if (repository.count() == 0) {
				com.example.nguyenthanhdat.model.Category c1 = new com.example.nguyenthanhdat.model.Category();
				c1.setName("Điện thoại");
				repository.save(c1);

				com.example.nguyenthanhdat.model.Category c2 = new com.example.nguyenthanhdat.model.Category();
				c2.setName("Laptop");
				repository.save(c2);

				com.example.nguyenthanhdat.model.Category c3 = new com.example.nguyenthanhdat.model.Category();
				c3.setName("Đồng hồ");
				repository.save(c3);
			}
		};
	}

	@org.springframework.context.annotation.Bean
	public org.springframework.boot.CommandLineRunner seedDefaultRoles(com.example.nguyenthanhdat.repository.IRoleRepository roleRepository) {
		return (args) -> {
			if (roleRepository.findByName("USER").isEmpty()) {
				com.example.nguyenthanhdat.model.Role userRole = new com.example.nguyenthanhdat.model.Role();
				userRole.setName("USER");
				userRole.setDescription("Default role for registered users");
				roleRepository.save(userRole);
			}

			if (roleRepository.findByName("ADMIN").isEmpty()) {
				com.example.nguyenthanhdat.model.Role adminRole = new com.example.nguyenthanhdat.model.Role();
				adminRole.setName("ADMIN");
				adminRole.setDescription("Administrator role");
				roleRepository.save(adminRole);
			}

			if (roleRepository.findByName("MANAGER").isEmpty()) {
				com.example.nguyenthanhdat.model.Role managerRole = new com.example.nguyenthanhdat.model.Role();
				managerRole.setName("MANAGER");
				managerRole.setDescription("Manager role");
				roleRepository.save(managerRole);
			}
		};
	}

	@org.springframework.context.annotation.Bean
	public org.springframework.boot.CommandLineRunner seedProducts(
			com.example.nguyenthanhdat.repository.ProductRepository productRepository,
			com.example.nguyenthanhdat.repository.CategoryRepository categoryRepository) {
		return (args) -> {
			if (productRepository.count() > 0) {
				return;
			}

			java.util.List<com.example.nguyenthanhdat.model.Category> categories = categoryRepository.findAll();
			if (categories.isEmpty()) {
				return;
			}

			com.example.nguyenthanhdat.model.Category phoneCategory = categories.get(0);
			com.example.nguyenthanhdat.model.Category laptopCategory = categories.size() > 1 ? categories.get(1) : phoneCategory;
			com.example.nguyenthanhdat.model.Category watchCategory = categories.size() > 2 ? categories.get(2) : phoneCategory;

			com.example.nguyenthanhdat.model.Product p1 = new com.example.nguyenthanhdat.model.Product();
			p1.setName("iPhone 15 128GB");
			p1.setPrice(20990000);
			p1.setDescription("iPhone 15 chip A16, camera 48MP, pin tốt cho nhu cầu hằng ngày");
			p1.setImage("https://cdn.tgdd.vn/Products/Images/42/303891/iphone-15-black-thumb-600x600.jpg");
			p1.setPromoted(true);
			p1.setPromotionPrice(18990000.0);
			p1.setPromotionQuantity(20);
			p1.setCategory(phoneCategory);
			productRepository.save(p1);

			com.example.nguyenthanhdat.model.Product p2 = new com.example.nguyenthanhdat.model.Product();
			p2.setName("Samsung Galaxy S24 256GB");
			p2.setPrice(22990000);
			p2.setDescription("Galaxy S24 màn hình 120Hz, AI camera, hiệu năng mạnh mẽ");
			p2.setImage("https://cdn.tgdd.vn/Products/Images/42/307174/samsung-galaxy-s24-xam-thumbnew-600x600.jpg");
			p2.setPromoted(false);
			p2.setPromotionPrice(null);
			p2.setPromotionQuantity(0);
			p2.setCategory(phoneCategory);
			productRepository.save(p2);

			com.example.nguyenthanhdat.model.Product p3 = new com.example.nguyenthanhdat.model.Product();
			p3.setName("MacBook Air M2 13 inch");
			p3.setPrice(26990000);
			p3.setDescription("Laptop mỏng nhẹ, pin dài, phù hợp học tập và làm việc");
			p3.setImage("https://cdn.tgdd.vn/Products/Images/44/282827/macbook-air-m2-2022-midnight-thumb-600x600.jpg");
			p3.setPromoted(true);
			p3.setPromotionPrice(24990000.0);
			p3.setPromotionQuantity(10);
			p3.setCategory(laptopCategory);
			productRepository.save(p3);

			com.example.nguyenthanhdat.model.Product p4 = new com.example.nguyenthanhdat.model.Product();
			p4.setName("Apple Watch SE 44mm");
			p4.setPrice(6990000);
			p4.setDescription("Đồng hồ thông minh theo dõi sức khỏe và thông báo nhanh");
			p4.setImage("https://cdn.tgdd.vn/Products/Images/7077/325236/apple-watch-se-2-gps-44mm-vien-nhom-day-vai-den-thumb-1-600x600.jpg");
			p4.setPromoted(false);
			p4.setPromotionPrice(null);
			p4.setPromotionQuantity(0);
			p4.setCategory(watchCategory);
			productRepository.save(p4);
		};
	}
}


