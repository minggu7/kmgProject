package com.kmgServer.project.jwt;

import com.kmgServer.project.service.RefreshService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {
    private final JWTUtil jwtUtil;
    private final RefreshService refreshService;
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws  IOException, ServletException{
        String requestUri = request.getRequestURI();
        if(!requestUri.matches("^\\/logout$")){

            filterChain.doFilter(request, response);
            return;
        }
        String requestMethod = request.getMethod();
        if(!requestMethod.equals("POST")){

            filterChain.doFilter(request, response);
            return;
        }

        //리프레쉬 토큰 확인하고 가져오기
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        for(Cookie cookie : cookies){
            if(cookie.getName().equals("refresh")){
                refresh = cookie.getValue();
            }
        }

        if(refresh == null){

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        //제출한 토큰 만료시간 검증
        try{
            jwtUtil.isExpired(refresh);
        }catch (ExpiredJwtException e){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        //만료시간 유효할 시 refresh토큰인지 확인 후 검사
        String category = jwtUtil.getCategory(refresh);
        if(!category.equals("refresh")){

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;

        }

        //DB내에 저장 된 사용자의 refresh 토큰과 제출 리프레쉬 토큰 비교. 만료 되었는지 검사
        Boolean isExist = refreshService.existsByRefresh(refresh);
        if(!isExist){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        //유효한 사용자임을 확인 했음. 이제 로그아웃에 따른 토큰 컨트롤을 하자
        //DB에서 삭제
        refreshService.deleteRefresh(refresh);

        //Cookie에서 null 처리
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        //변경된 쿠키 값으로 생성
        response.addCookie(cookie);
        response.setStatus(HttpServletResponse.SC_OK);//로그아웃 작업 완료
    }
}
