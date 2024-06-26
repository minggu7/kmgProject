package com.kmgServer.project.controller;

import com.kmgServer.project.dto.MemberDTO;
import com.kmgServer.project.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {
    private final MemberService  memberService;
    @GetMapping("/SignUp")
    public String SignUpForm(){
        return "SignUp";
    }

    @PostMapping("/SignUp")
    public String SignUp(MemberDTO memberDTO, RedirectAttributes redirectAttributes)throws IOException{
        String signupResult = memberService.singupPass(memberDTO);
        //사용할 수 있는 이메일이라면
        if(signupResult.equals("Pass")){
            memberService.signup(memberDTO);
            return "redirect:/login";
        }else{
        //이미 있는 이메일이라면 에러 메시지 가지고 다시 회원가입 페이지로
            redirectAttributes.addFlashAttribute("errorMessage", signupResult);
            return "redirect:/SignUp";
        }


    }

    @PostMapping("/login")
    public String Login(MemberDTO memberDTO, RedirectAttributes redirectAttributes)throws IOException{
        //System.out.println("memberDTO = " + memberDTO); 잘 넘어오는지 확인 완료
        String loginResult = memberService.login(memberDTO);
        if(loginResult.equals("Pass")){
            //이메일 및 비밀번호 매칭 성공(로그인 성공) TRUE 값 받아옴.
            //로그인 성공 했으니 메인 화면으로
            return "redirect:/";
        }else{
            redirectAttributes.addFlashAttribute("errorMessage", loginResult);
            //로그인 실패. 로그인 실패정보를 가진 loginResult를 errorMessage에 담아서 html로 넘겨줌.
            return "redirect:/login";
        }
    }

    @GetMapping("/login")
    public String LoginForm(@ModelAttribute("errorMessage") String errorMessage){
        return "login";
    }

    @GetMapping("/member/list")
    public String findAll(Model model){
        List<MemberDTO> MemberDTOList = memberService.FindAll();
        model.addAttribute("MemberList", MemberDTOList);
        //System.out.println("MemberDTOList = " + MemberDTOList);

        return "MemberList";
    }

    @GetMapping("/member/{id}")
    public String FindById(@PathVariable("id") Long id, Model model){
        //우선 회원 상세 내용을 보여줘야한다.SELECT(WHERE 조건은 id로)
        MemberDTO memberDTO = memberService.FindById(id);
        //System.out.println("memberDTO = " + memberDTO);
        model.addAttribute("MemberDTO", memberDTO);

        return "MemberDetail";
    }

    @GetMapping("/member/update/{id}")
    public String update(@PathVariable("id") Long id, Model model){
        MemberDTO memberDTO = memberService.FindById(id);
        model.addAttribute("MemberDTO", memberDTO);
//        System.out.println("memberDTO = " + memberDTO);

        return "MemberUpdate";
    }
    @PostMapping("/member/update/{id}")
    public String update(MemberDTO memberDTO, Model model){
//        System.out.println("memberDTO = " + memberDTO);
        memberService.update(memberDTO);
        //업데이트 완료
        MemberDTO dto = memberService.FindById(memberDTO.getId());
        model.addAttribute("MemberDTO", dto);

        return "MemberDetail";
    }
    @GetMapping("/member/delete/{id}")
    public String update(@PathVariable("id") Long id){
        memberService.delete(id);

        return "redirect:/member/list";
    }


}
