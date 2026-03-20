package com.example.nguyenthanhdat.controller;

import com.example.nguyenthanhdat.model.CartItem;
import com.example.nguyenthanhdat.model.Order;
import com.example.nguyenthanhdat.service.CartService;
import com.example.nguyenthanhdat.service.MomoPaymentService;
import com.example.nguyenthanhdat.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private CartService cartService;

    @Autowired
    private MomoPaymentService momoPaymentService;

    // Show checkout form
    @GetMapping("/checkout")
    public String showCheckoutForm(Model model) {
        List<CartItem> cartItems = cartService.getCartItems();
        
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }
        
        // Calculate total
        double subtotal = orderService.calculateCartTotal(cartItems);
        int totalQuantity = orderService.calculateCartTotalQuantity(cartItems);
        double shippingFee = orderService.calculateShippingFee(subtotal, totalQuantity);
        double grandTotal = subtotal + shippingFee;
        int estimatedRewardPoints = orderService.calculateRewardPoints(subtotal);
        double estimatedRewardValue = orderService.calculateRewardValue(subtotal);
        
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("totalQuantity", totalQuantity);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("grandTotal", grandTotal);
        model.addAttribute("estimatedRewardPoints", estimatedRewardPoints);
        model.addAttribute("estimatedRewardValue", estimatedRewardValue);
        
        return "checkout";
    }

    // Process checkout
    @PostMapping("/checkout")
    public String processCheckout(
            @RequestParam String customerName,
            @RequestParam String customerPhone,
            @RequestParam String customerAddress,
            @RequestParam(defaultValue = "COD") String paymentMethod) {
        
        List<CartItem> cartItems = cartService.getCartItems();
        
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }
        
        // Create order
        Order order = orderService.createOrder(customerName, customerPhone, customerAddress, cartItems);

        if ("MOMO".equalsIgnoreCase(paymentMethod)) {
            try {
                String payUrl = momoPaymentService.createPaymentUrl(order);
                orderService.updateOrderStatus(order.getId(), "WAITING_PAYMENT");
                return "redirect:" + payUrl;
            } catch (Exception e) {
                orderService.updateOrderStatus(order.getId(), "PAYMENT_FAILED");
                String encodedMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
                return "redirect:/order/confirmation/" + order.getId() + "?paymentError=" + encodedMessage;
            }
        }
        
        return "redirect:/order/confirmation/" + order.getId();
    }

    @GetMapping("/momo-return")
    public String momoReturn(@RequestParam Map<String, String> params) {
        String momoOrderId = params.get("orderId");
        String resultCode = params.get("resultCode");

        if (momoOrderId != null) {
            try {
                String appOrderId = momoOrderId;
                if (momoOrderId.startsWith("ORD") && momoOrderId.contains("T")) {
                    appOrderId = momoOrderId.substring(3, momoOrderId.indexOf('T'));
                }
                Long id = Long.parseLong(appOrderId);
                if ("0".equals(resultCode)) {
                    orderService.updateOrderStatus(id, "CONFIRMED");
                } else {
                    orderService.updateOrderStatus(id, "PAYMENT_FAILED");
                }
                return "redirect:/order/confirmation/" + id;
            } catch (NumberFormatException ignored) {
                // Ignore invalid order id from gateway callback.
            }
        }

        return "redirect:/order/list";
    }

    @PostMapping("/momo-notify")
    @ResponseBody
    public String momoNotify(@RequestBody(required = false) Map<String, Object> payload) {
        return "OK";
    }

    // Show order confirmation
    @GetMapping("/confirmation/{id}")
    public String showOrderConfirmation(
            @PathVariable Long id,
            @RequestParam(required = false) String paymentError,
            Model model) {
        Order order = orderService.getOrderById(id)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        model.addAttribute("order", order);
        model.addAttribute("paymentError", paymentError);
        
        return "order-confirmation";
    }

    // List all orders (admin view)
    @GetMapping("/list")
    public String listOrders(Model model) {
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        
        return "order-list";
    }

    // View order details
    @GetMapping("/details/{id}")
    public String viewOrderDetails(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderById(id)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        model.addAttribute("order", order);
        
        return "order-details";
    }

    // Update order status
    @PostMapping("/update-status/{id}")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        orderService.updateOrderStatus(id, status);
        return "redirect:/order/list";
    }

    // Delete order
    @GetMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return "redirect:/order/list";
    }
}
