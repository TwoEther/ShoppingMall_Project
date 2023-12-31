package org.project.shop.repository;

import org.project.shop.domain.Order;
import org.project.shop.domain.OrderItem;
import org.project.shop.domain.OrderSearch;

import java.util.List;

public interface OrderRepository {
    public void save(Order order);

    public Order findByOrderId(Long orderId);
    public List<Order> findAllOrder();

    public Order findByMemberIdBeforePayment(Long memberId);

    public List<Order> findByMemberIdAfterPayment(Long memberId);

    public List<Order> findOrderByMemberId(Long memberId);

    public List<Order> findAllOrder(OrderSearch orderSearch);
}
