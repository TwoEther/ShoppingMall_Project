package org.project.shop.service;

import org.project.shop.domain.Member;

import java.util.List;
import java.util.Optional;

public interface MemberService {
    // 회원 가입
    public Long save(Member member);

    // 회원 조회
    public List<Member> findAllMember();
    public List<Member> findAllGeneralMember();
    public Member findOneMember(Long memberId);
    public Member findByUserId(String userId);
    public String findMemberIdByEmailAndPhoneNum(String email, String phoneNum);
    public Member findById(Long id);

    public Optional<Member> findByEmail(String email);

    // 회원 확인
    public int checkDuplicateMember(String id);

    // 회원 검증
    public boolean checkReqexId(String id);
    public boolean checkReqexPw(String id);
    public boolean checkPassword(String id, String pw);

    // 회원 삭제
    public void deleteMemberByMemberId(Long memberId);

    // 이메일 인증 <구현체에 있음>
}
