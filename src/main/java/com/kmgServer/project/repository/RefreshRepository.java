package com.kmgServer.project.repository;

import com.kmgServer.project.dto.RefreshDTO;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Repository
@RequiredArgsConstructor
public class RefreshRepository {
    private final SqlSessionTemplate sql;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void CreateRefresh(RefreshDTO refreshDTO){
        refreshDTO.setExpiration(LocalDateTime.parse(refreshDTO.getExpiration(), formatter).toString());
        sql.insert("Refresh.create", refreshDTO);
    }

    //만료 기간 체크
    public boolean existsByRefresh(String refresh){
        Integer count = sql.selectOne("Refresh.exists", refresh);
        return count != null && count > 0;
    }

    //토큰 삭제
    public void deleteRefresh(String refresh){
        sql.delete("Refresh.delete", refresh);
    }
}
