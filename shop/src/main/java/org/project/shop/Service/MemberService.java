package org.project.shop.service;

import lombok.RequiredArgsConstructor;
import org.project.shop.repository.MemberRepository;
import org.project.shop.domain.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
//@RequiredArgsConstructor
public class MemberService{
    private final MemberRepository memberRepository;
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Long join(Member member) {
        validateDuplicateMember(member);
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        List<Member> findMember = memberRepository.findByName(member.getName());
        if(!findMember.isEmpty()) throw new IllegalStateException("이미 존재하는 회원입니다");
    }

    public List<Member> findAllMember(){
        return memberRepository.findAll();
    }

    public Member findOneMember(Long memberId){
        return memberRepository.findMember(memberId);
    }
}