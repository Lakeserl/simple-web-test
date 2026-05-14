package com.app.service;

import com.app.dto.OrderRequest;
import com.app.model.Order;
import com.app.model.OrderItem;
import com.app.model.OrderStatus;
import com.app.model.Product;
import com.app.repository.OrderRepository;
import com.app.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;

    public OrderService(OrderRepository orderRepo, ProductRepository productRepo) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
    }

    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + id));
    }

    @Transactional
    public Order create(OrderRequest request) {
        var order = new Order();
        order.setCustomerName(request.customerName());
        order.setCustomerPhone(request.customerPhone());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;

        for (var itemReq : request.items()) {
            Product product = productRepo.findById(itemReq.productId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Product not found: " + itemReq.productId()));

            var item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(itemReq.quantity());
            item.setPrice(product.getPrice());
            order.addItem(item);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemReq.quantity())));

            // Reduce stock
            product.setStock(product.getStock() - itemReq.quantity());
            productRepo.save(product);
        }

        order.setTotal(total);
        return orderRepo.save(order);
    }

    @Transactional
    public Order updateStatus(Long id, OrderStatus status) {
        var order = findById(id);
        order.setStatus(status);
        return orderRepo.save(order);
    }
}
