package org.project.shop.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.project.shop.config.ScriptUtils;
import org.project.shop.domain.Address;
import org.project.shop.domain.Member;
import org.project.shop.domain.Role;
import org.project.shop.service.MailService;
import org.project.shop.service.MemberServiceImpl;
import org.project.shop.web.AddressForm;
import org.project.shop.web.LoginForm;
import org.project.shop.web.MemberForm;
import org.project.shop.web.findIDForm;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/member")
public class MemberController {
    private final MemberServiceImpl memberServiceImpl;
    private final MailService mailService;

    @GetMapping(value = "/new")
    public String createForm(@RequestParam(value = "error", required = false) String error,
                             @RequestParam(value = "exception", required = false) String exception,
                             Model model) {
        model.addAttribute("memberForm", new MemberForm());
        return "/member/createMemberForm";
    }

    @ModelAttribute("roles")
    private Role[] roles() {
        return Role.values();
    }

    @PostMapping(value = "/new")
    public String signUp(HttpServletResponse response, @Valid MemberForm form, BindingResult result) throws IOException {
        if (result.hasErrors()) {
            return "/member/createMemberForm";
        }

        String id = form.getUserId();
        String password = form.getPassword1();
        String name = form.getName();
        String nickname = form.getNickname();
        String phoneNum = form.getPhoneNum();
        String email = form.getEmail();
        String roles = form.getRoles();

//        mailService.sendEmail();

        if (memberServiceImpl.findByUserId(id) != null) {
            ScriptUtils.alert(response, "아이디가 존재합니다");
            return "/member/createMemberForm";
        } else {
            Member member = new Member(id, password, name, phoneNum, email);
            if (roles.equals(Role.ROLE_ADMIN.toString())) {
                member.setRole(Role.ROLE_ADMIN.toString());
            } else if (roles.equals(Role.ROLE_ANONYMOUS)) {
                member.setRole(Role.ROLE_ANONYMOUS.toString());
            } else{
                member.setRole(Role.ROLE_USER.toString());
            }

            if (memberServiceImpl.checkReqexId(id) && memberServiceImpl.checkReqexPw(password)) {
                result.reject("signupFailed", "아이디나 패스워드가 올바르지 않습니다");
                return "/member/createMemberForm";
            }

            try {
                memberServiceImpl.save(member);
            } catch (DataIntegrityViolationException e) {
                e.printStackTrace();
                result.reject("signupFailed", "이미 등록된 사용자입니다.");
                return "/member/createMemberForm";
            } catch (Exception e) {
                e.printStackTrace();
                result.reject("signupFailed", e.getMessage());
                return "/member/createMemberForm";
            }

            return "redirect:/";
        }


    }

    @PostMapping(value = "/idCheck")
    @ResponseBody
    public int idCheck(@RequestParam String userId) throws Exception {
        return memberServiceImpl.checkDuplicateMember(userId);
    }

    @PostMapping(value = "/pwCheck")
    @ResponseBody
    public boolean pwCheck(@RequestParam String pw) throws Exception {
        return memberServiceImpl.checkReqexPw(pw);
    }


    @GetMapping(value = "/login")
    public String createLoginForm(@RequestParam(value = "error", required = false) String error,
                                  @RequestParam(value = "exception", required = false) String exception,
                                  Model model) {
        model.addAttribute("error", error);
        model.addAttribute("exception", exception);
        model.addAttribute("loginForm", new LoginForm());
        return "/member/loginForm";
    }

    @PostMapping(value = "/login")
    public String login(@Valid LoginForm form, BindingResult result) {
        if (result.hasErrors()) {
            return "/member/loginForm";
        }

        return "redirect:/";
    }

    @GetMapping("/findId")
    public String findId(Model model) {
        model.addAttribute("findIDForm", new findIDForm());
        return "/member/findUserId";
    }
    @PostMapping("/findId")
    public String findIdPost(HttpServletResponse response, @RequestParam String phoneNum, @RequestParam String email, Model model) throws IOException {
        String findMemberID = memberServiceImpl.findMemberIdByEmailAndPhoneNum(email, phoneNum);

        if (findMemberID != null) {
            model.addAttribute("userId", findMemberID);
            return "/member/resultId";
        }else{
            ScriptUtils.alert(response, "핸드폰 번호 또는 이메일이 다릅니다");
            return "/member/findUserId";
        }

    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        return "redirect:/";
    }

    @GetMapping(value = "/memberList")
    public String memberList(Model model) {
        List<Member> allMember = memberServiceImpl.findAllMember();
        model.addAttribute("members", allMember);
        return "/member/memberList";
    }

    @GetMapping(value = "/address")
    public String setDeliveryMember(Model model) throws IOException {
        model.addAttribute("AddressForm", new AddressForm());
        return "member/address";
    }

    @PostMapping(value = "/address")
    @Transactional
    public String setDeliveryMemberPost(HttpServletRequest request, HttpServletResponse response, AddressForm addressForm) throws IOException {
        String zipcode = addressForm.getZipcode();
        String address1 = addressForm.getAddress1();
        String address2 = addressForm.getAddress2();
        String reference = addressForm.getReference();


        Object principal;
        try {
            principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserDetails userDetails = (UserDetails) principal;
            String username = ((UserDetails) principal).getUsername();
        } catch (Exception e) {
            ScriptUtils.alert(response, "올바르지 못한 접근입니다");
            return "redirect:/";
        }

        Member findMember = memberServiceImpl.findByUserId(((UserDetails) principal).getUsername());
        Address address = new Address(zipcode, address1, address2, reference);
        findMember.setAddress(address);
        ScriptUtils.alert(response,"배송지가 설정 되었습니다");

        return "/order/orderList";
    }

    @DeleteMapping(value = "/delete/{memberNum}")
    @Transactional
    @ResponseBody
    public void deleteMemberById(@RequestParam String memberNum) {
        memberServiceImpl.deleteMemberByMemberId(Long.valueOf(memberNum));
    }

    // 이메일 인증
    public void sendCodeToEmail(String toEmail) {

    }

    private void checkDuplicatedEmail(String email) {
    }
}
