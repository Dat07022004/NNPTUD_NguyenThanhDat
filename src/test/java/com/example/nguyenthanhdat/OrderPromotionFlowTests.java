package com.example.nguyenthanhdat;

import com.example.nguyenthanhdat.model.CartItem;
import com.example.nguyenthanhdat.model.Category;
import com.example.nguyenthanhdat.model.LoyaltyAccount;
import com.example.nguyenthanhdat.model.Order;
import com.example.nguyenthanhdat.model.OrderDetail;
import com.example.nguyenthanhdat.model.Product;
import com.example.nguyenthanhdat.repository.CategoryRepository;
import com.example.nguyenthanhdat.repository.LoyaltyAccountRepository;
import com.example.nguyenthanhdat.repository.OrderDetailRepository;
import com.example.nguyenthanhdat.repository.ProductRepository;
import com.example.nguyenthanhdat.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@Transactional
class OrderPromotionFlowTests {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private LoyaltyAccountRepository loyaltyAccountRepository;

    @Test
    void checkoutShouldUsePromotionUpToPromotionQuantityThenFallbackToBasePrice() {
        Category category = new Category();
        category.setName("Test Promo Category");
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setName("Phone Promo Test");
        product.setCategory(category);
        product.setPrice(100.0);
        product.setPromoted(true);
        product.setPromotionPrice(80.0);
        product.setPromotionQuantity(2);
        product = productRepository.save(product);

        List<CartItem> cartItems = List.of(new CartItem(product, 4));

        Order order = orderService.createOrder("Tester", "0900000000", "HCM", cartItems);

        assertNotNull(order.getId());
        assertEquals(360.0, order.getSubtotalAmount(), 0.001);
        assertEquals(30_000.0, order.getShippingFee(), 0.001);
        assertEquals(30_360.0, order.getTotalAmount(), 0.001);
        assertEquals(0, order.getRewardPoints());
        assertEquals(0.0, order.getRewardValue(), 0.001);

        List<OrderDetail> details = orderDetailRepository.findByOrderId(order.getId());
        OrderDetail detail = details.getFirst();
        assertEquals(4, detail.getQuantity());
        assertEquals(360.0, detail.getSubtotal(), 0.001);

        Product reloaded = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(0, reloaded.getPromotionQuantity());
        assertFalse(Boolean.TRUE.equals(reloaded.getPromoted()));
        assertNull(reloaded.getPromotionPrice());
    }

    @Test
    void checkoutShouldFreeShipWhenSubtotalAtLeastOneMillionAndQuantityAtLeastTwo() {
        Category category = new Category();
        category.setName("Test Free Ship Category");
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setName("Phone Free Ship Test");
        product.setCategory(category);
        product.setPrice(600_000.0);
        product.setPromoted(false);
        product = productRepository.save(product);

        List<CartItem> cartItems = List.of(new CartItem(product, 2));

        Order order = orderService.createOrder("Tester", "0900000000", "HCM", cartItems);

        assertNotNull(order.getId());
        assertEquals(1_200_000.0, order.getSubtotalAmount(), 0.001);
        assertEquals(0.0, order.getShippingFee(), 0.001);
        assertEquals(1_200_000.0, order.getTotalAmount(), 0.001);
        assertEquals(160, order.getRewardPoints());
        assertEquals(1_200_000.0, order.getRewardValue(), 0.001);

        LoyaltyAccount account = loyaltyAccountRepository.findByCustomerPhone("0900000000").orElseThrow();
        assertEquals(160, account.getTotalPoints());
        assertEquals(1_200_000.0, account.getTotalRewardValue(), 0.001);
    }

    @Test
    void loyaltyPointsShouldPersistAndAccumulateByCustomerPhone() {
        Category category = new Category();
        category.setName("Loyalty Category");
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setName("Loyalty Product");
        product.setCategory(category);
        product.setPrice(75_000.0);
        product.setPromoted(false);
        product = productRepository.save(product);

        List<CartItem> firstCart = List.of(new CartItem(product, 1));
        List<CartItem> secondCart = List.of(new CartItem(product, 2));

        orderService.createOrder("Tester", "0911111111", "HCM", firstCart);
        orderService.createOrder("Tester", "0911111111", "HCM", secondCart);

        LoyaltyAccount account = loyaltyAccountRepository.findByCustomerPhone("0911111111").orElseThrow();
        assertEquals(30, account.getTotalPoints());
        assertEquals(225_000.0, account.getTotalRewardValue(), 0.001);
    }
}
