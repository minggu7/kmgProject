package com.kmgServer.project.jwt;

import com.kmgServer.project.dto.CustomUserDetails;
import com.kmgServer.project.dto.RefreshDTO;
import com.kmgServer.project.service.RefreshService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final RefreshService refreshService;


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        String username = null;
        String password = null;

        try {
            // JSON 형식의 요청 바디를 읽어들이기 위해 BufferedReader 사용
            StringBuilder sb = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // JSON 파싱
            JSONObject json = new JSONObject(sb.toString());
            username = json.getString("username");
            password = json.getString("password");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        System.out.println("Attempting authentication with username: " + username + " and password: " + password);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);

        try {
            return authenticationManager.authenticate(authToken);
        } catch (AuthenticationException e) {
            System.out.println("Authentication failed in attemptAuthentication: " + e.getMessage());
            throw e; // 예외를 던져서 unsuccessfulAuthentication 메서드가 호출되도록 함
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication)throws  IOException{
        //유저 정보 빼오기
        String username = authentication.getName();
        //유저 권한 빼오기
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        System.out.println(username + " " + role + "clear!!"); // 디버깅을 위한 출력

        //토큰 생성(Access 토큰, refresh 토큰 생성)
        String access = jwtUtil.createJwt("access", username, role, 60000L);
        String refresh = jwtUtil.createJwt("refresh", username, role, 86400000L);

        //Refresh토큰 저장
        addRefreshDTO(username, refresh, 86400000L);

        //응답 설정
        response.setHeader("access", access);
        response.addCookie(createCookie("refresh", refresh));
        response.setStatus(HttpStatus.OK.value());

        // JSON 응답 생성
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String jsonResponse = String.format("{\"ACCESS_TOKEN\": \"%s\"}", access);
        response.getWriter().write(jsonResponse);
    }

    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed){
        System.out.println("false!!");
        response.setStatus(401);
    }

    private Cookie createCookie(String key, String value){
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        cookie.setHttpOnly(true);

        return cookie;
    }

    private void addRefreshDTO(String username, String refresh, Long expiredMs){

        LocalDateTime expiration = LocalDateTime.now().plusMinutes(expiredMs);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String expirationString = expiration.format(formatter);


        RefreshDTO refreshDTO = new RefreshDTO();
        refreshDTO.setUsername(username);
        refreshDTO.setRefresh(refresh);
        refreshDTO.setExpiration(expirationString);

        refreshService.CreateRefresh(refreshDTO);
    }
}
