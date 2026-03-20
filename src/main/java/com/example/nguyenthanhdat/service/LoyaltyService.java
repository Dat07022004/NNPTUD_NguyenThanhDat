package com.example.nguyenthanhdat.service;

import com.example.nguyenthanhdat.model.LoyaltyAccount;
import com.example.nguyenthanhdat.model.VoucherRedemption;
import com.example.nguyenthanhdat.repository.LoyaltyAccountRepository;
import com.example.nguyenthanhdat.repository.VoucherRedemptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class LoyaltyService {
    @Autowired
    private LoyaltyAccountRepository loyaltyAccountRepository;
    @Autowired
    private VoucherRedemptionRepository voucherRedemptionRepository;

    public record LoyaltySummary(String customerPhone, int totalPoints, double totalRewardValue) {}
    public record RedeemOption(int pointsRequired, double discountValue) {}

    private static final List<RedeemOption> REDEEM_OPTIONS = List.of(
            new RedeemOption(100, 10000.0),
            new RedeemOption(300, 35000.0),
            new RedeemOption(500, 60000.0)
    );

    public LoyaltySummary addPoints(String customerPhone, int pointsToAdd, double rewardValueToAdd) {
        String normalizedPhone = normalizePhone(customerPhone);
        if (normalizedPhone == null) {
            return new LoyaltySummary("", 0, 0.0);
        }

        LoyaltyAccount account = loyaltyAccountRepository.findByCustomerPhone(normalizedPhone)
                .orElseGet(() -> createNewAccount(normalizedPhone));

        int safePointsToAdd = Math.max(0, pointsToAdd);
        double safeRewardValueToAdd = Math.max(0.0, rewardValueToAdd);

        account.setTotalPoints(account.getTotalPoints() + safePointsToAdd);
        account.setTotalRewardValue(account.getTotalRewardValue() + safeRewardValueToAdd);
        LoyaltyAccount saved = loyaltyAccountRepository.save(account);

        return toSummary(saved);
    }

    @Transactional(readOnly = true)
    public LoyaltySummary getSummaryOrDefault(String customerPhone) {
        String normalizedPhone = normalizePhone(customerPhone);
        if (normalizedPhone == null) {
            return new LoyaltySummary("", 0, 0.0);
        }

        Optional<LoyaltyAccount> accountOpt = loyaltyAccountRepository.findByCustomerPhone(normalizedPhone);
        return accountOpt.map(this::toSummary)
                .orElseGet(() -> new LoyaltySummary(normalizedPhone, 0, 0.0));
    }

    @Transactional(readOnly = true)
    public List<RedeemOption> getRedeemOptions() {
        return REDEEM_OPTIONS;
    }

    @Transactional(readOnly = true)
    public List<VoucherRedemption> getVouchersByPhone(String customerPhone) {
        String normalizedPhone = normalizePhone(customerPhone);
        if (normalizedPhone == null) {
            return List.of();
        }
        return voucherRedemptionRepository.findByCustomerPhoneOrderByCreatedAtDesc(normalizedPhone);
    }

    public VoucherRedemption redeemVoucher(String customerPhone, int pointsToRedeem) {
        String normalizedPhone = normalizePhone(customerPhone);
        if (normalizedPhone == null) {
            throw new IllegalArgumentException("So dien thoai khong hop le");
        }

        RedeemOption option = REDEEM_OPTIONS.stream()
                .filter(o -> o.pointsRequired() == pointsToRedeem)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Goi doi diem khong hop le"));

        LoyaltyAccount account = loyaltyAccountRepository.findByCustomerPhone(normalizedPhone)
                .orElseThrow(() -> new IllegalArgumentException("Ban chua co diem de doi"));

        int currentPoints = account.getTotalPoints() == null ? 0 : account.getTotalPoints();
        if (currentPoints < option.pointsRequired()) {
            throw new IllegalArgumentException("Diem tich luy khong du");
        }

        account.setTotalPoints(currentPoints - option.pointsRequired());
        loyaltyAccountRepository.save(account);

        VoucherRedemption voucher = new VoucherRedemption();
        voucher.setCustomerPhone(normalizedPhone);
        voucher.setPointsSpent(option.pointsRequired());
        voucher.setDiscountValue(option.discountValue());
        voucher.setVoucherCode(generateVoucherCode());
        voucher.setStatus("ACTIVE");
        return voucherRedemptionRepository.save(voucher);
    }

    private String generateVoucherCode() {
        return "VC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private LoyaltyAccount createNewAccount(String customerPhone) {
        LoyaltyAccount account = new LoyaltyAccount();
        account.setCustomerPhone(customerPhone);
        account.setTotalPoints(0);
        account.setTotalRewardValue(0.0);
        return account;
    }

    private LoyaltySummary toSummary(LoyaltyAccount account) {
        return new LoyaltySummary(
                account.getCustomerPhone(),
                account.getTotalPoints() != null ? account.getTotalPoints() : 0,
                account.getTotalRewardValue() != null ? account.getTotalRewardValue() : 0.0
        );
    }

    private String normalizePhone(String customerPhone) {
        if (customerPhone == null) {
            return null;
        }
        String trimmed = customerPhone.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}