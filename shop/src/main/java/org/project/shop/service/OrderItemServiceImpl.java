package org.project.shop.service;

import org.project.shop.domain.OrderItem;
import org.project.shop.repository.OrderItemRepository;
import org.project.shop.repository.OrderItemRepositoryImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderItemServiceImpl implements OrderItemService{
    private final OrderItemRepositoryImpl orderItemRepositoryImpl;

    public OrderItemServiceImpl(OrderItemRepositoryImpl orderItemRepositoryImpl) {
        this.orderItemRepositoryImpl = orderItemRepositoryImpl;
    }

    @Override
    public void save(OrderItem orderItem) {
        orderItemRepositoryImpl.save(orderItem);
    }

    @Override
    public OrderItem findOrderItemById(Long id) {
        return orderItemRepositoryImpl.findOrderItemById(id);
    }

    @Override
    public List<OrderItem> findOrderItemByOrderId(Long orderId) {
        return orderItemRepositoryImpl.findOrderItemByOrderId(orderId);
    }

    @Override
    public List<OrderItem> findOrderItemByItemId(Long itemId) {
        return orderItemRepositoryImpl.findOrderItemByItemId(itemId);
    }

    @Override
    public List<OrderItem> findOrderItemByOrderAndItem(Long orderId, Long itemId) {
        return orderItemRepositoryImpl.findOrderItemByOrderAndItem(orderId, itemId);
    }

    @Override
    public List<OrderItem> findAllOrderItem() {
        return orderItemRepositoryImpl.findAllOrderItem();
    }
}