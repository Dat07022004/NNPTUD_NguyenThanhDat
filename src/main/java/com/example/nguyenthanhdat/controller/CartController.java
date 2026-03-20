package com.example.nguyenthanhdat.controller;

import com.example.nguyenthanhdat.service.CartService;
import com.example.nguyenthanhdat.service.LoyaltyService;
import com.example.nguyenthanhdat.service.OrderService;
import com.example.nguyenthanhdat.service.PricingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartService cartService;
    @Autowired
    private PricingService pricingService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private LoyaltyService loyaltyService;

    @GetMapping
    public String showCart(@RequestParam(required = false) String memberPhone, Model model) {
        model.addAttribute("cartItems", cartService.getCartItems());
        double total = cartService.getCartItems().stream()
                .mapToDouble(item -> pricingService.calculate(item.getProduct(), item.getQuantity()).subtotal())
                .sum();
        int totalQuantity = orderService.calculateCartTotalQuantity(cartService.getCartItems());
        double shippingFee = orderService.calculateShippingFee(total, totalQuantity);
        double grandTotal = total + shippingFee;
        int estimatedRewardPoints = orderService.calculateRewardPoints(total);
        double estimatedRewardValue = orderService.calculateRewardValue(total);
        LoyaltyService.LoyaltySummary loyaltySummary = loyaltyService.getSummaryOrDefault(memberPhone);
        model.addAttribute("total", total);
        model.addAttribute("totalQuantity", totalQuantity);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("grandTotal", grandTotal);
        model.addAttribute("estimatedRewardPoints", estimatedRewardPoints);
        model.addAttribute("estimatedRewardValue", estimatedRewardValue);
        model.addAttribute("memberPhone", loyaltySummary.customerPhone());
        model.addAttribute("dbRewardPoints", loyaltySummary.totalPoints());
        model.addAttribute("dbRewardValue", loyaltySummary.totalRewardValue());
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId, @RequestParam int quantity) {
        cartService.addToCart(productId, quantity);

        return "redirect:/cart";
    }

    @GetMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId) {
        cartService.removeFromCart(productId);
        return "redirect:/cart";
    }

    @GetMapping("/clear")
    public String clearCart() {
        cartService.clearCart();
        return "redirect:/cart";
    }

}
