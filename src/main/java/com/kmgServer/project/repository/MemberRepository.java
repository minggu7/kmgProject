package com.kmgServer.project.repository;

import com.kmgServer.project.dto.MemberDTO;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepository {
    private final SqlSessionTemplate sql;


    public void signup(MemberDTO memberDTO) {
        sql.insert("Member.signup", memberDTO);
    }

    public boolean signupPass(MemberDTO memberDTO) {
        Integer count = sql.selectOne("Member.signupPass", memberDTO);
        return count == 0;
    }

    public boolean FindByusername(MemberDTO memberDTO) {
        //SELECT 하여 이메일이 있으면 TRUE, 없으면 FALSE를 받는다.
        Integer count = sql.selectOne("Member.FindByusername", memberDTO);
        //이메일 있다면 true, 없으면 false
        return count != null && count > 0;
    }

    public boolean PasswordIsValid(MemberDTO memberDTO){
        Integer count = sql.selectOne("Member.PasswordIsValid", memberDTO);
        return count != null && count > 0;
    }

    public List<MemberDTO> MemberFindAll() {
        return sql.selectList("Member.FindAll");
    }

    public  MemberDTO FindById(Long id) {
        return sql.selectOne("Member.FindById", id);
    }

    public void MemberUpdate(MemberDTO memberDTO) {
        sql.update("Member.Update", memberDTO);
    }

    public void MemberDelete(Long id) {
        sql.delete("Member.Delete", id);
    }

    public String getPasswordByUsername(String username) {
        return sql.selectOne("Member.getPasswordByUsername", username);
    }

    public MemberDTO findByUsername(String username) {
        return sql.selectOne("Member.findByUsername", username);
    }
}
