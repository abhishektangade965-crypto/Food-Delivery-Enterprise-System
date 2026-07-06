package com.delivo.controller;

import com.delivo.model.Order;
import com.delivo.model.OrderItem;
import com.delivo.model.User;
import com.delivo.repository.OrderRepository;
import com.delivo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Order>> getMyOrders() {
        String email = getAuthenticatedUserEmail();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(orderRepository.findByCustomerId(user.getId()));
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody Map<String, Object> requestData) {
        String email = getAuthenticatedUserEmail();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        String restaurantName = (String) requestData.get("restaurantName");
        List<Map<String, Object>> itemsList = (List<Map<String, Object>>) requestData.get("items");

        if (restaurantName == null || itemsList == null || itemsList.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid order data");
        }

        Order order = new Order();
        order.setCustomer(user);
        order.setRestaurantName(restaurantName);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (Map<String, Object> itemData : itemsList) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setItemName((String) itemData.get("itemName"));
            item.setQuantity((Integer) itemData.get("quantity"));
            BigDecimal itemPrice = new BigDecimal(itemData.get("price").toString());
            item.setPrice(itemPrice);

            totalAmount = totalAmount.add(itemPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
            orderItems.add(item);
        }

        order.setTotalAmount(totalAmount);
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);
        return ResponseEntity.ok(savedOrder);
    }

    private String getAuthenticatedUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }
}
