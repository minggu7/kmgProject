package com.kmgServer.project.service;

import com.kmgServer.project.dto.MemberDTO;
import com.kmgServer.project.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public MemberDTO FindById(Long id) {
        return memberRepository.FindById(id);
    }

    public String singupPass(MemberDTO memberDTO) {
        //이미 있는 이메일이라면
        if(!memberRepository.signupPass(memberDTO)){
            return "이미 사용중인 이메일 입니다";
        }
        //DB에 해당 이메일 없을 경우
        return "Pass";
    }

    public void signup(MemberDTO memberDTO) {
        memberDTO.setPassword(bCryptPasswordEncoder.encode(memberDTO.getPassword()));
        memberRepository.signup(memberDTO);
    }


    public String login(MemberDTO memberDTO) {
        //이메일 매칭에 실패 했다면
        if(!memberRepository.FindByusername(memberDTO)){
            return "유저 이름이 없습니다.";
        }

        String storedHashedPassword = memberRepository.getPasswordByUsername(memberDTO.getUsername());
        //만약 복호화 한 비밀번호와 사용자 입력 비밀번호가 다르다면?
        if(storedHashedPassword == null || !bCryptPasswordEncoder.matches(memberDTO.getPassword(), storedHashedPassword)){
            return "비밀번호가 일치하지 않습니다.";
        }

        //이메일 일치, 비밀번호 일치시 메인화면으로 갈 자격이 있다.
        return "Pass";
    }

    public List<MemberDTO> FindAll() {
        return memberRepository.MemberFindAll();
    }

    public void update(MemberDTO memberDTO) {
        memberRepository.MemberUpdate(memberDTO);
    }

    public void delete(Long id) {
        memberRepository.MemberDelete(id);
    }
}