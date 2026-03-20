package com.example.nguyenthanhdat.service;

import com.example.nguyenthanhdat.model.Product;
import org.springframework.stereotype.Service;

@Service
public class PricingService {

    public PricingResult calculate(Product product, int quantity) {
        if (product == null || quantity <= 0) {
            return new PricingResult(0, 0.0, 0.0, 0.0, 0.0);
        }

        double basePrice = product.getPrice();
        double promotionPrice = product.getPromotionPrice() != null ? product.getPromotionPrice() : basePrice;
        int availablePromotionQuantity = product.getPromotionQuantity() != null ? Math.max(0, product.getPromotionQuantity()) : 0;

        boolean promotionActive = Boolean.TRUE.equals(product.getPromoted())
                && promotionPrice > 0
                && promotionPrice < basePrice
                && availablePromotionQuantity > 0;

        int promotionUnitsUsed = promotionActive ? Math.min(quantity, availablePromotionQuantity) : 0;
        int baseUnitsUsed = quantity - promotionUnitsUsed;

        double totalPromotionAmount = promotionUnitsUsed * promotionPrice;
        double totalBaseAmount = baseUnitsUsed * basePrice;
        double subtotal = totalPromotionAmount + totalBaseAmount;

        return new PricingResult(
                promotionUnitsUsed,
                totalPromotionAmount,
                totalBaseAmount,
                subtotal,
                quantity > 0 ? subtotal / quantity : 0.0
        );
    }

    public record PricingResult(
            int promotionUnitsUsed,
            double totalPromotionAmount,
            double totalBaseAmount,
            double subtotal,
            double averageUnitPrice
    ) {
    }
}
