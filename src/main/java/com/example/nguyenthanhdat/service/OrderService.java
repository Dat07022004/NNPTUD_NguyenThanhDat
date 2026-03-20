package com.example.nguyenthanhdat.service;

import com.example.nguyenthanhdat.model.CartItem;
import com.example.nguyenthanhdat.model.Order;
import com.example.nguyenthanhdat.model.OrderDetail;
import com.example.nguyenthanhdat.model.Product;
import com.example.nguyenthanhdat.repository.OrderDetailRepository;
import com.example.nguyenthanhdat.repository.OrderRepository;
import com.example.nguyenthanhdat.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {
    public static final double FREE_SHIPPING_SUBTOTAL_THRESHOLD = 1_000_000.0;
    public static final int FREE_SHIPPING_MIN_QUANTITY = 2;
    public static final double DEFAULT_SHIPPING_FEE = 30_000.0;
    public static final double REWARD_POINTS_DIVISOR = 7_500.0;

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private CartService cartService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private PricingService pricingService;
    @Autowired
    private LoyaltyService loyaltyService;

    public Order createOrder(String customerName, String customerPhone, String customerAddress, List<CartItem> cartItems) {
        Order order = new Order();
        order.setCustomerName(customerName);
        order.setCustomerPhone(customerPhone);
        order.setCustomerAddress(customerAddress);
        
        // Calculate subtotal before shipping
        double subtotalAmount = 0.0;
        int totalQuantity = 0;
        for (CartItem item : cartItems) {
            Product currentProduct = productRepository.findById(item.getProduct().getId())
                    .orElse(item.getProduct());
            PricingService.PricingResult pricing = pricingService.calculate(currentProduct, item.getQuantity());
            subtotalAmount += pricing.subtotal();
            totalQuantity += item.getQuantity();
        }
        double shippingFee = calculateShippingFee(subtotalAmount, totalQuantity);
        double totalAmount = subtotalAmount + shippingFee;
        int rewardPoints = calculateRewardPoints(subtotalAmount);
        double rewardValue = calculateRewardValue(subtotalAmount);

        order.setSubtotalAmount(subtotalAmount);
        order.setShippingFee(shippingFee);
        order.setRewardPoints(rewardPoints);
        order.setRewardValue(rewardValue);
        order.setTotalAmount(totalAmount);
        
        order = orderRepository.save(order);

        for (CartItem item : cartItems) {
            Product currentProduct = productRepository.findById(item.getProduct().getId())
                    .orElse(item.getProduct());
            PricingService.PricingResult pricing = pricingService.calculate(currentProduct, item.getQuantity());

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(currentProduct);
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(pricing.averageUnitPrice());
            detail.setSubtotal(pricing.subtotal());
            orderDetailRepository.save(detail);

            if (pricing.promotionUnitsUsed() > 0) {
                int currentPromotionQuantity = currentProduct.getPromotionQuantity() != null ? currentProduct.getPromotionQuantity() : 0;
                int remainingPromotionQuantity = Math.max(0, currentPromotionQuantity - pricing.promotionUnitsUsed());
                currentProduct.setPromotionQuantity(remainingPromotionQuantity);

                if (remainingPromotionQuantity == 0) {
                    currentProduct.setPromoted(false);
                    currentProduct.setPromotionPrice(null);
                }

                productRepository.save(currentProduct);
            }
        }

        loyaltyService.addPoints(customerPhone, rewardPoints, rewardValue);
        
        // Clear the cart after order placement
        cartService.clearCart();

        return order;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findByOrderByOrderDateDesc();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> searchOrders(String customerName) {
        return orderRepository.findByCustomerNameContainingIgnoreCase(customerName);
    }

    public void updateOrderStatus(Long orderId, String status) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(status);
            orderRepository.save(order);
        }
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    public double calculateCartTotal(List<CartItem> cartItems) {
        return cartItems.stream()
                .mapToDouble(item -> pricingService.calculate(item.getProduct(), item.getQuantity()).subtotal())
                .sum();
    }

    public int calculateCartTotalQuantity(List<CartItem> cartItems) {
        return cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public double calculateShippingFee(double subtotalAmount, int totalQuantity) {
        if (subtotalAmount >= FREE_SHIPPING_SUBTOTAL_THRESHOLD && totalQuantity >= FREE_SHIPPING_MIN_QUANTITY) {
            return 0.0;
        }
        return DEFAULT_SHIPPING_FEE;
    }

    public double calculateCartGrandTotal(List<CartItem> cartItems) {
        double subtotalAmount = calculateCartTotal(cartItems);
        int totalQuantity = calculateCartTotalQuantity(cartItems);
        return subtotalAmount + calculateShippingFee(subtotalAmount, totalQuantity);
    }

    public int calculateRewardPoints(double subtotalAmount) {
        if (subtotalAmount <= 0) {
            return 0;
        }

        return BigDecimal.valueOf(subtotalAmount)
                .divide(BigDecimal.valueOf(REWARD_POINTS_DIVISOR), 6, RoundingMode.HALF_UP)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }

    public double calculateRewardValue(double subtotalAmount) {
        return BigDecimal.valueOf(calculateRewardPoints(subtotalAmount))
                .multiply(BigDecimal.valueOf(REWARD_POINTS_DIVISOR))
                .doubleValue();
    }
}
