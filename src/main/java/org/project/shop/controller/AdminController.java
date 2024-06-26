package org.project.shop.controller;

import com.querydsl.core.Tuple;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.project.shop.auth.PrincipalDetails;
import org.project.shop.config.ScriptUtils;
import org.project.shop.custom.CustomPageRequest;
import org.project.shop.domain.*;
import org.project.shop.service.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/admin")
@Secured("ROLE_ADMIN")

public class AdminController {
    private final MemberServiceImpl memberServiceImpl;
    private final ItemServiceImpl itemServiceImpl;
    private final ReviewServiceImpl reviewServiceImpl;
    private final OrderServiceImpl orderServiceImpl;
    private final InquiryServiceImpl inquiryServiceImpl;
    private final OrderItemServiceImpl orderItemServiceImpl;

    @GetMapping(value = "")
    public String adminPage(@AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {
        List<Member> memberList = memberServiceImpl.findAllGeneralMember();
        List<Order> allOrder = orderServiceImpl.findAllOrder();
        List<Inquiry> allInquiryByGeneralMember = inquiryServiceImpl.findAllInquiryByGeneralMember();
        List<Review> allReview = reviewServiceImpl.findAllReview();

        String username = principalDetails.getUsername();
        Member findMember = memberServiceImpl.findByUserId(username);

        // 관리자 권한이 없으면
        if (findMember.getRole().equals(Role.ROLE_USER.toString())) {
            return "home";
        }

        // 가져올 개수
        int memberSize = Math.min(memberList.size(), 2);
        int orderSize = Math.min(allOrder.size(), 2);
        int inquirySize = Math.min(allInquiryByGeneralMember.size(), 2);
        int reviewSize = Math.min(allReview.size(), 2);

        // 주문 개수별 랭킹 상위 10등
        List<Tuple> allMemberByOrderRank = memberServiceImpl.findAllMemberByOrderRank();
        int orderRank = Math.min(10, allMemberByOrderRank.size());

        model.addAttribute("rank", allMemberByOrderRank.subList(0, orderRank));
        model.addAttribute("type", "member");
        model.addAttribute("memberList", memberList.subList(0, memberSize));
        model.addAttribute("orderList", allOrder.subList(0, orderSize));
        model.addAttribute("inquiryList", allInquiryByGeneralMember.subList(0, inquirySize));
        model.addAttribute("reviewList", allReview.subList(0, reviewSize));
        return "admin/adminHome";
    }

    @GetMapping(value = "/member")
    public String adminMemberPage(Model model) {
        List<Member> allGeneralMember = memberServiceImpl.findAllGeneralMember();
        model.addAttribute("allMember", allGeneralMember);
        return "admin/adminMember";
    }

    @GetMapping(value = "/item")
    public String adminMemberItem(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
                                  @RequestParam(value = "keyword", defaultValue = "",required = false) String keyword,
                                  @RequestParam(value = "sort_by", defaultValue = "createDate", required = false) String sort_by) {


        int pageSize = 6;
        int allItemNum = itemServiceImpl.getAllItemNum();
        int pageNum = Math.floorDiv(allItemNum, pageSize);
        int startPage = Math.max(page - 1, 0);
        int endPage = Math.min(page + 1, pageNum);

        Page<Item> allItem = itemServiceImpl.findByKeyword(PageRequest.of(page, 6, Sort.by(sort_by)), keyword);

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("allItem", allItem);
        return "admin/adminItem";
    }

    @PostMapping(value = "/item/{itemId}")
    @Transactional
    public void adminMemberItemEdit(HttpServletResponse response, @PathVariable String itemId, @RequestParam int size) throws IOException {
        Long id = Long.parseLong(itemId);
        Item findItem = itemServiceImpl.findOneItem(id);
        findItem.setStockQuantity(size);

        ScriptUtils.alertAndBackPage(response, "수정 되었습니다");
    }
    @PostMapping(value = "/item/status")
    @Transactional
    public void adminMemberItemStatusAllEdit(HttpServletResponse response, @RequestParam Boolean status) throws IOException {
        List<Item> allItem = itemServiceImpl.findAllItem();
        if (status) {
            allItem.forEach(item -> item.setSaleStatus(true));
        } else {
            allItem.forEach(item -> item.setSaleStatus(false));
        }

        ScriptUtils.alertAndBackPage(response, "수정 되었습니다");
    }


    @PostMapping(value = "/item/status/{itemId}")
    @Transactional
    public void adminMemberItemStatusEdit(HttpServletResponse response, @PathVariable String itemId, @RequestParam Boolean status) throws IOException {
        Long id = Long.parseLong(itemId);
        Item findItem = itemServiceImpl.findOneItem(id);
        if (status) {
            findItem.setSaleStatus(true);
        } else {
            findItem.setSaleStatus(false);
        }

        ScriptUtils.alertAndBackPage(response, "수정 되었습니다");
    }

    @PostMapping(value = "/item/delivery/{orderItemId}")
    @Transactional
    public void adminMemberItemDeliveryEdit(HttpServletResponse response, @PathVariable String orderItemId,
                                            @RequestParam String delivery_status) throws IOException {
        Long orderItem_Id = Long.parseLong(orderItemId);
        OrderItem findOrderItem = orderItemServiceImpl.findOrderItemById(orderItem_Id);
        if (delivery_status.equals("ready")) {
            findOrderItem.setDeliveryStatus(DeliveryStatus.READY);
        } else if (delivery_status.equals("going")) {
            findOrderItem.setDeliveryStatus(DeliveryStatus.GOING);
        } else if (delivery_status.equals("complete")) {
            findOrderItem.setDeliveryStatus(DeliveryStatus.COMPLETE);
        } else {
            findOrderItem.setDeliveryStatus(DeliveryStatus.NOTFOUND);
        }

        ScriptUtils.alertAndBackPage(response, "수정 되었습니다");

    }

    @DeleteMapping(value = "/item/delete/{itemId}")
    @Transactional
    public void adminMemberItemDelete(@PathVariable String itemId) throws IOException {
        Long id = Long.parseLong(itemId);
        itemServiceImpl.deleteByItemId(id);
    }

    @GetMapping(value = "/review")
    public String adminReview(Model model) {
        List<Review> allReview = reviewServiceImpl.findAllReview();

        model.addAttribute("allReview", allReview);
        return "/admin/adminReview";
    }

    @DeleteMapping(value = "/review/delete/{reviewId}")
    public void adminDeleteReview(Model model, @PathVariable String reviewId) {
        Long id = Long.parseLong(reviewId);
        reviewServiceImpl.deleteReview(id);
    }

    @GetMapping(value = "/inquiry")
    public String adminInquiry(Model model) {
        List<Inquiry> allInquiry = inquiryServiceImpl.findAllInquiryByGeneralMember();

        model.addAttribute("allInquiry", allInquiry);
        return "/admin/adminInquiry";
    }

    @GetMapping(value = "/inquiry/answer/{inquiryId}")
    public String adminInquiryAnswer(Model model, @PathVariable String inquiryId) {
        Long id = Long.parseLong(inquiryId);
        Inquiry findInquiry = inquiryServiceImpl.findById(id);

        model.addAttribute("inquiry", findInquiry);
        return "/admin/adminInquiryAnswer";
    }

    @PostMapping(value = "/inquiry/answer/{inquiryId}")
    @Transactional
    public void adminInquiryAnswer(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                    @PathVariable String inquiryId,
                                     HttpServletResponse response,
                                     @RequestParam String answer) throws IOException {
        String username = principalDetails.getUsername();
        Member adminMember = memberServiceImpl.findByUserId(username);

        Long id = Long.parseLong(inquiryId);
        Inquiry findInquiry = inquiryServiceImpl.findById(id);

        Inquiry child = new Inquiry(answer);
        child.setMember(adminMember);
        child.setItem(findInquiry.getItem());
        inquiryServiceImpl.save(child);

        findInquiry.setChild(child);
        child.setParent(findInquiry);

        ScriptUtils.alertAndBackPage(response, "문의가 작성 되었습니다.");
    }
    @PutMapping(value = "/inquiry/answer/{inquiryId}")
    @Transactional
    public void adminInquiryPutAnswer(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                   @PathVariable String inquiryId,
                                   HttpServletResponse response,
                                   @RequestParam String answer) throws IOException {
        String username = principalDetails.getUsername();
        Member adminMember = memberServiceImpl.findByUserId(username);

        Long id = Long.parseLong(inquiryId);
        Inquiry findInquiry = inquiryServiceImpl.findById(id);

        Inquiry child = new Inquiry(answer);
        child.setMember(adminMember);
        child.setItem(findInquiry.getItem());
        inquiryServiceImpl.save(child);

        findInquiry.setChild(child);
        child.setParent(findInquiry);

        ScriptUtils.alertAndBackPage(response, "문의가 작성 되었습니다.");
    }



    @GetMapping(value = "/order")
    public String adminMemberOrderList(Model model) {
        long start = System.currentTimeMillis();
        List<Order> allOrder = orderServiceImpl.findAllOrder();
        long diff = System.currentTimeMillis() - start;

        model.addAttribute("allOrder", allOrder);
        return "/admin/adminOrder";
    }

    @GetMapping(value = "/order/{orderId}")
    public String adminMemberOrderDetail(Model model, @PathVariable Long orderId
                                        ,@AuthenticationPrincipal PrincipalDetails principalDetails) {

        Order findOrder = orderServiceImpl.findByOrderId(orderId);
        model.addAttribute("order", findOrder);


        return "/admin/adminOrderDetail";
    }

    @PostMapping(value = "/order/status/{orderId}")
    public String adminMemberOrderList(Model model, @PathVariable Long orderId) {
        Order findOrder = orderServiceImpl.findByOrderId(orderId);
        return "/admin/adminOrder";
    }

    public void isLogined(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        Member findMember = memberServiceImpl.findByUserId(principalDetails.getUsername());
        System.out.println("findMember.getRole() = " + findMember.getRole());
    }
}
