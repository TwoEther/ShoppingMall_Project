package org.project.shop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Entity
// MySQL에서 order는 예약어 이므로 orders로 이름 변경
@Table(name = "orders")
@Getter
public class Order {
    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @Column(unique = true)
    private String tid;

    @Column(unique = true)
    private String orderNumber;

    // 지연 로딩 사용
    // N:1 (Order : Member)
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="orders")
    private Member member;

    // 1:N (Order : OrderItem)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    // 1:N (Order : Delivery)
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    // N:1 (Order : Cart)
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(insertable = false, updatable = false)
    private Cart cart;

    private LocalDate orderDate;

    @Embedded
    private Address address;

    // Enum type
    @Enumerated(EnumType.ORDINAL)
    private OrderStatus status;

    // 연관관계 메서드
    public void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }


    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }


    public void setTid(String tid) {
        this.tid = tid;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public static String createOrderNumber() {
        String data = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSSSS"));
        StringBuilder orderNumber = new StringBuilder();

        // 0 ~ 99 사이의 난수
        int random = (int)((Math.random() * 99));
        String salt = random >= 10 ? Integer.toString(random) : "0"+random;

        for (String s : data.split("-")) orderNumber.append(s);

        return orderNumber.append(salt).toString();
    }

    public static Order createOrder(Member member){
        Order order = new Order();
        order.setMember(member);
        order.setStatus(OrderStatus.READY);
//        order.setOrderNumber(createOrderNumber());
        order.setOrderDate(LocalDate.now());
        return order;
    }

    public String createOrderName() {
        return this.orderItems.get(0).getItem().getName() + "포함 "+ this.orderItems.size()+"건";
    }

    public int getOrderQuantity() {
        return this.getOrderItems().size();
    }


    // 주문명 생성 함수
    public String getOrderTitle() {
        List<OrderItem> orderItems = this.getOrderItems();
        int orderItemNum = orderItems.size();
        return orderItems.get(0).getItem().getName() + "포함 " + orderItemNum + "건";
    }


    public List<Item> findOrderItemList() {
        List<Item> paymentItems = new ArrayList<>();
        for (OrderItem orderItem : this.getOrderItems()) {
            paymentItems.add(orderItem.getItem());
        }
        
        return paymentItems;
    }
    // 주문취소
    public void cancel(){
        if (delivery.getStatus() == DeliveryStatus.GOING) {
            throw new IllegalStateException("이미 배송된 상품은 취소가 불가능합니다");
        }
        this.setStatus(OrderStatus.READY);
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }



    // 주문 가격 조회
    public int getTotalPrice(){
        int totalPrice = 0;
        for (OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", tid='" + tid + '\'' +
                ", member=" + member +
                ", orderItems=" + orderItems +
                ", delivery=" + delivery +
                ", cart=" + cart +
                ", orderDate=" + orderDate +
                ", status=" + status +
                '}';
    }
}
