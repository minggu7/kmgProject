package com.kmgServer.project.jwt;

import com.kmgServer.project.dto.CustomUserDetails;
import com.kmgServer.project.dto.MemberDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            System.out.println("token null");
            filterChain.doFilter(request, response);
            return; // 조건이 해당되면 메소드 종료 (필수)
        }

        System.out.println("authorization now");

        String token = authorization.split(" ")[1];

        // 토큰 소멸 시간 검증
        if (jwtUtil.isExpired(token)) {
            System.out.println("token expired");
            filterChain.doFilter(request, response);
            return; // 조건이 해당되면 메소드 종료 (필수)
        }

        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        System.out.println(username + " " + role);

        if (username == null || role == null) {
            System.out.println("Invalid token");
            filterChain.doFilter(request, response);
            return; // 조건이 해당되면 메소드 종료 (필수)
        }

        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setUsername(username);
        // SecurityContextHolder에 정확한 비밀번호를 넣을 필요는 없음. token 정보에 없어서 매번 DB에 요청해야 함. 임시적으로 생성만 해주자
        memberDTO.setPassword("temppassword");
        memberDTO.setRole(role);

        // UserDetails에 만들어진 회원 정보 객체를 담자
        CustomUserDetails customUserDetails = new CustomUserDetails(memberDTO);

        // 만든 정보(customUserDetails)를 통해 UsernamePasswordAuthenticationToken 을 생성해주자.
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

        // 넘겨주기
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}
