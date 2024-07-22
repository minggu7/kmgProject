package com.kmgServer.project.service;

import com.kmgServer.project.dto.MemberDTO;
import com.kmgServer.project.repository.MemberRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
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
        if (!memberRepository.signupPass(memberDTO)) {
            return "이미 사용중인 이메일 입니다";
        }
        return "Pass";
    }

    public void signup(MemberDTO memberDTO) {
        memberDTO.setPassword(bCryptPasswordEncoder.encode(memberDTO.getPassword()));
        memberRepository.signup(memberDTO);
    }

    public String login(MemberDTO memberDTO) {
        if (!memberRepository.FindByusername(memberDTO)) {
            return "유저 이름이 없습니다.";
        }

        String storedHashedPassword = memberRepository.getPasswordByUsername(memberDTO.getUsername());
        if (storedHashedPassword == null || !bCryptPasswordEncoder.matches(memberDTO.getPassword(), storedHashedPassword)) {
            return "비밀번호가 일치하지 않습니다.";
        }

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

    public String generateAccessToken(String username) {
        String secretKey = "onegorepopochugoretabunbbobbotaleetadoregojo";
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String verifyPassword(String username) {
        return memberRepository.getPasswordByUsername(username);
    }

    public void updatePassword(MemberDTO memberDTO) {
        memberDTO.setPassword(bCryptPasswordEncoder.encode(memberDTO.getPassword()));
        memberRepository.MemberUpdate(memberDTO);
    }
}