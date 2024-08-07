package com.kmgServer.project.jwt;

import com.kmgServer.project.dto.CustomUserDetails;
import com.kmgServer.project.dto.MemberDTO;
import io.jsonwebtoken.ExpiredJwtException;
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
import java.io.PrintWriter;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String accessToken = request.getHeader("Authorization");

        if (accessToken == null || !accessToken.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 공백 제거 후 토큰 검증
        accessToken = accessToken.substring(7); // "Bearer " 부분 제거
        accessToken = accessToken.trim(); // 앞뒤 공백 제거

        //토큰 만료 여부 확인. 만료시 다음 필터로 넘기지 않음.

        if (jwtUtil.isExpired(accessToken)) {
            // 토큰이 만료된 경우 처리
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json"); // 응답 타입 설정 (선택 사항)
            PrintWriter writer = response.getWriter();
            writer.print("{\"error\":\"access token expired\"}");
            writer.flush();
            writer.close();
            return;
        }

        //카테고리를 통해 엑세스 토큰인지 확인
        String category = jwtUtil.getCategory(accessToken);

        if(!category.equals("access")){//액세스 토큰이 아닐 경우
            PrintWriter writer = response.getWriter();
            writer.print("invalid access token");

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        //사용자 정보 획득하기
        String username = jwtUtil.getUsername(accessToken);
        String role = jwtUtil.getRole(accessToken);

        System.out.println("clear JWTFilter" + username + " "  + role);

        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setUsername(username);
        memberDTO.setRole(role);
        CustomUserDetails customUserDetails = new CustomUserDetails(memberDTO);

        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}
