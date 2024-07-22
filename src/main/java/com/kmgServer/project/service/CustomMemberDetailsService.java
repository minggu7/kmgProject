package com.kmgServer.project.service;

import com.kmgServer.project.dto.CustomUserDetails;
import com.kmgServer.project.dto.MemberDTO;
import com.kmgServer.project.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomMemberDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        MemberDTO memberDTO = memberRepository.findByUsername(username);

        if(memberDTO == null){
            throw new UsernameNotFoundException("User not found");
        }

        if(memberDTO != null){
            //사용자 정보 있다면
            System.out.println("memberDTO = " + memberDTO);
            return new CustomUserDetails(memberDTO);

        }

        return null;
    }
}
