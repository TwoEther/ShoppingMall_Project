package org.project.shop.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.shop.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Service
@SpringBootTest
public class ReviewServiceTest {
    @PersistenceContext
    EntityManager em;

    @Autowired
    private MemberServiceImpl memberServiceImpl;
    @Autowired
    private ItemServiceImpl itemServiceImpl;
    @Autowired
    private ReviewServiceImpl reviewServiceImpl;
    @Autowired
    private OrderServiceImpl orderServiceImpl;
    @Autowired
    private OrderItemServiceImpl orderItemServiceImpl;

    @BeforeEach
    @Transactional
    public void setUp() {
        Review review1 = new Review(4, "읽기 쉬워요");
        Review review2 = new Review(5, "정말 자세한 내용 입니다");
        Review review3 = new Review(2, "너무 어렵습니다");

        reviewServiceImpl.save(review1);
        reviewServiceImpl.save(review2);
        reviewServiceImpl.save(review3);

        Member member1 = new Member("testId1", "password1","nickname1", "name1","0100101010","test1@test.com");
        Member member2 = new Member("testId2", "password2","nickname2", "name2","0100101011","test2@test.com");

        memberServiceImpl.join(member1);
        memberServiceImpl.join(member2);

        Item item1 = new Item("item1", 20000, 30);
        Item item2 = new Item("item2", 34000, 15);
        Item item3 = new Item("item3", 11000, 11);
        Item item4 = new Item("item4", 59000, 9);

        itemServiceImpl.saveItemNoImage(item1);
        itemServiceImpl.saveItemNoImage(item2);
        itemServiceImpl.saveItemNoImage(item3);
        itemServiceImpl.saveItemNoImage(item4);

        Order order1 = Order.createOrder(member1);
        order1.setStatus(OrderStatus.SUCCESS);
        orderServiceImpl.save(order1);

        OrderItem orderItem1 = OrderItem.createOrderItem(item1, item1.getPrice(), 3);
        OrderItem orderItem2 = OrderItem.createOrderItem(item2, item2.getPrice(), 3);
        OrderItem orderItem3 = OrderItem.createOrderItem(item3, item3.getPrice(), 3);
        orderItem1.setOrder(order1);
        orderItem2.setOrder(order1);
        orderItem3.setOrder(order1);

        orderItemServiceImpl.save(orderItem1);
        orderItemServiceImpl.save(orderItem2);
        orderItemServiceImpl.save(orderItem3);

        review1.setMember(member1);
        review2.setMember(member1);
        review3.setMember(member2);

        review1.setItem(item1);
        review2.setItem(item1);
        review3.setItem(item2);

    }


    @DisplayName("리뷰 생성 테스트")
    @Test
    public void createReviewTest() {
        List<Review> allReview = reviewServiceImpl.findAllReview();
        assertThat(allReview.size()).isEqualTo(3);
    }

    @DisplayName("특정 아이템에 리뷰가 적용되는지 테스트")
    @Test
    public void reviewItemTest() {
        List<Member> allMember = memberServiceImpl.findAllMember();
        List<Item> allItems = itemServiceImpl.findItems();
        List<Review> allReview = reviewServiceImpl.findAllReview();

        Member member1 = allMember.get(0);
        Member member2 = allMember.get(1);

        Item item1 = allItems.get(0);
        Item item2 = allItems.get(1);

        for(int i=0; i<allReview.size()-1; i++){
            Review review = allReview.get(i);
            review.setMember(member1);
            review.setItem(item1);
        }
        allReview.get(allReview.size()-1).setMember(member2);
        allReview.get(allReview.size()-1).setItem(item2);


        // Item 에 해당 하는 리뷰 개수 테스트
        List<Review> allReviewByItemId1 = reviewServiceImpl.findAllReviewByItemId(item1.getId());
        List<Review> allReviewByItemId2 = reviewServiceImpl.findAllReviewByItemId(item2.getId());

        for (Review review : allReviewByItemId1) {
            System.out.println("review.toString() = " + review.toString());
        }

        assertThat(allReviewByItemId1.size()).isEqualTo(2);
        assertThat(allReviewByItemId2.size()).isEqualTo(1);

        // Member 에 해당 하는 리뷰 개수 테스트
        List<Review> allReviewByMemberId1 = reviewServiceImpl.findAllReviewByMemberId(allMember.get(0).getId());
        List<Review> allReviewByMemberId2 = reviewServiceImpl.findAllReviewByMemberId(allMember.get(1).getId());

        assertThat(allReviewByMemberId1.size()).isEqualTo(2);
        assertThat(allReviewByMemberId2.size()).isEqualTo(1);
    }

    @DisplayName("구매 상품에 리뷰가 적용되는지 테스트")
    @Transactional
    @Test
    public void paymentItemReviewTest() {
        List<Member> allMember = memberServiceImpl.findAllMember();
        List<Item> items = itemServiceImpl.findItems();
        List<Review> allReview = reviewServiceImpl.findAllReview();

        Item item1 = items.get(0);
        Item item2 = items.get(1);
        Item item3 = items.get(2);
        
        Member member1 = allMember.get(0);
        Review review1 = allReview.get(0);
        Review review2 = allReview.get(1);


        // 특정 Member가 구매한 전체 상품 목록
        List<Order> byMemberIdAfterPayment = orderServiceImpl.findByMemberIdAfterPayment(member1.getId());
        List<Item> paymentItemList = new ArrayList<>();
        for (Order order : byMemberIdAfterPayment) {
            List<Item> findItems = order.orderItemList();
            paymentItemList.addAll(findItems);
        }


        List<Review> findAllReview = reviewServiceImpl.findAllReview();
        assertThat(findAllReview.size()).isEqualTo(3);

        List<Review> allReviewByItemId = reviewServiceImpl.findAllReviewByItemId(item1.getId());
        assertThat(allReviewByItemId.size()).isEqualTo(2);
    }

}
