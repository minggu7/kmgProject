package com.kmgServer.project.controller;

import com.kmgServer.project.dto.RefreshDTO;
import com.kmgServer.project.jwt.JWTUtil;
import com.kmgServer.project.service.RefreshService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequiredArgsConstructor
public class ReissueController {
    private final JWTUtil jwtUtil;
    private final RefreshService refreshService;

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest resquest, HttpServletResponse response){

        //리프레쉬 토큰을 우선 쿠키에서 뽑아내서 검증 해야한다.
        String refresh = null;
        Cookie[] cookies = resquest.getCookies();
        for(Cookie cookie : cookies){
            //만약 쿠기에 있는 정보중에 refresh 라는 이름의 값이 있을 경우 refresh 변수에 해당 값 저장.
            if(cookie.getName().equals("refresh")){
                //토큰 값을 refresh 변수에 저장.
                refresh = cookie.getValue();
            }
        }

        if(refresh == null){
            //리프레쉬 토큰이 없을 경우 리프레쉬 토큰이 없었다고 클라이언트에 알려줌
            return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
        }

        //있을경우 리프레쉬 토큰이 유효한 토큰인지 확인
        try{
            jwtUtil.isExpired(refresh);
        }catch (ExpiredJwtException e){

            //유효하지 않다면 오류 코드 보내기
            return new ResponseEntity<>("refresh token expired", HttpStatus.BAD_REQUEST);
        }



        //클라이언트에서 온 refresh를 뜯어봤을 때 카테고리 확인. refresh 토큰인지를 확인한다.(페이로드 확인)
        String category = jwtUtil.getCategory(refresh);

        if(!category.equals("refresh")){
            //리프레쉬 토큰 아닐 경우 오류 코드
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        //현재 사용자의 리프레쉬 토큰 만료기간 확인
        Boolean isExist = refreshService.existsByRefresh(refresh);
        if(!isExist){
            //현재 리프레쉬 토큰 만료 되었다면
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        //리프레쉬 토큰 통과. 새로운 엑세스 토큰 만들어주자.
        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);

        String newAccess = jwtUtil.createJwt("access", username, role, 600000L);
        String newRefresh = jwtUtil.createJwt("refresh", username, role, 86400000L);

        //새로운 토큰을 만들기 위해 기존 RefreshToken 삭제 후 생성
        refreshService.deleteRefresh(refresh);
        addRefreshDTO(username, newRefresh, 86400000L);


        //다시 Access토큰 생성 완료. 사용하라고 넘겨주자
        response.addHeader("access", newAccess);
        response.addCookie(createCookie("refresh", newRefresh));

        return new ResponseEntity<>(HttpStatus.OK);//모든 검증 통과하고 엑세스 토큰 재발급 해드렸습니다.

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
