package com.example.nguyenthanhdat.controller;

import com.example.nguyenthanhdat.model.User;
import com.example.nguyenthanhdat.repository.IUserRepository;
import com.example.nguyenthanhdat.service.LoyaltyService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/loyalty")
public class LoyaltyController {

    @Autowired
    private LoyaltyService loyaltyService;

    @Autowired
    private IUserRepository userRepository;

    @GetMapping("/vouchers")
    public String vouchers(@NotNull Authentication authentication, Model model,
                           @RequestParam(value = "success", required = false) String success,
                           @RequestParam(value = "error", required = false) String error) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElse(null);

        if (user == null || user.getPhone() == null || user.getPhone().isBlank()) {
            model.addAttribute("errorMessage", "Tài khoản chưa có số điện thoại, chưa thể đổi điểm.");
            model.addAttribute("options", loyaltyService.getRedeemOptions());
            model.addAttribute("vouchers", java.util.List.of());
            model.addAttribute("points", 0);
            return "loyalty-voucher";
        }

        var summary = loyaltyService.getSummaryOrDefault(user.getPhone());
        model.addAttribute("points", summary.totalPoints());
        model.addAttribute("rewardValue", summary.totalRewardValue());
        model.addAttribute("options", loyaltyService.getRedeemOptions());
        model.addAttribute("vouchers", loyaltyService.getVouchersByPhone(user.getPhone()));

        if (success != null) {
            model.addAttribute("successMessage", success);
        }
        if (error != null) {
            model.addAttribute("errorMessage", error);
        }

        return "loyalty-voucher";
    }

    @PostMapping("/redeem")
    public String redeem(@NotNull Authentication authentication,
                         @RequestParam("points") int points) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElse(null);

        if (user == null || user.getPhone() == null || user.getPhone().isBlank()) {
            return "redirect:/loyalty/vouchers?error=Tai+khoan+chua+co+so+dien+thoai";
        }

        try {
            var voucher = loyaltyService.redeemVoucher(user.getPhone(), points);
            return "redirect:/loyalty/vouchers?success=Da+doi+thanh+cong+ma+" + voucher.getVoucherCode();
        } catch (IllegalArgumentException ex) {
            return "redirect:/loyalty/vouchers?error=" + ex.getMessage().replace(" ", "+");
        }
    }
}
