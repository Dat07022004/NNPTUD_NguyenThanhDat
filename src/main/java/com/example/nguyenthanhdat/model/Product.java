package com.example.nguyenthanhdat.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double price;
    private String description;
    private String image;

    @Column(columnDefinition = "boolean default false")
    private Boolean promoted = false; // Khuyến mãi — mặc định không

    @Column(name = "promotion_price")
    private Double promotionPrice;

    @Column(name = "promotion_quantity")
    private Integer promotionQuantity = 0;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({"parentCategory", "subCategories"})
    private Category category;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Boolean getPromoted() {
        return promoted;
    }

    public void setPromoted(Boolean promoted) {
        this.promoted = promoted;
    }

    public Double getPromotionPrice() {
        return promotionPrice;
    }

    public void setPromotionPrice(Double promotionPrice) {
        this.promotionPrice = promotionPrice;
    }

    public Integer getPromotionQuantity() {
        return promotionQuantity;
    }

    public void setPromotionQuantity(Integer promotionQuantity) {
        this.promotionQuantity = promotionQuantity;
    }
}
