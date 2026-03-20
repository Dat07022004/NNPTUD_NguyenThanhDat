package com.example.nguyenthanhdat.repository;

import com.example.nguyenthanhdat.model.VoucherRedemption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherRedemptionRepository extends JpaRepository<VoucherRedemption, Long> {
    List<VoucherRedemption> findByCustomerPhoneOrderByCreatedAtDesc(String customerPhone);
}
