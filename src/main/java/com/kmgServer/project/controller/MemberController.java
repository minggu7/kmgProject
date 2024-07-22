package com.kmgServer.project.controller;

import com.kmgServer.project.dto.MemberDTO;
import com.kmgServer.project.jwt.JWTUtil;
import com.kmgServer.project.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final JWTUtil jwtUtil;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/SignUp")
    public String SignUpForm() {
        return "SignUp";
    }

    @PostMapping("/SignUp")
    public String SignUp(MemberDTO memberDTO, RedirectAttributes redirectAttributes) throws IOException {
        String signupResult = memberService.singupPass(memberDTO);
        if (signupResult.equals("Pass")) {
            memberService.signup(memberDTO);
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", signupResult);
            return "redirect:/SignUp";
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody MemberDTO memberDTO) throws IOException {
        String loginResult = memberService.login(memberDTO);
        if ("Pass".equals(loginResult)) {
            String accessToken = memberService.generateAccessToken(memberDTO.getUsername());
            Map<String, String> response = new HashMap<>();
            response.put("ACCESS_TOKEN", accessToken);
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("errorMessage", loginResult);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @GetMapping("/login")
    public String LoginForm(@ModelAttribute("errorMessage") String errorMessage) {
        return "login";
    }

    @GetMapping("/member/list")
    public String memberListPage(Model model) {
        List<MemberDTO> memberDTOList = memberService.FindAll();
        model.addAttribute("memberList", memberDTOList);
        return "MemberList";
    }

    @GetMapping("/api/member/list")
    @ResponseBody
    public ResponseEntity<List<MemberDTO>> getMemberList(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        try {
            if (!jwtUtil.isExpired(token)) {
                List<MemberDTO> memberDTOList = memberService.FindAll();
                return ResponseEntity.ok(memberDTOList);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/api/member/detail/{id}")
    @ResponseBody
    public ResponseEntity<MemberDTO> getMemberDetail(@PathVariable("id") Long id, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        try {
            if (!jwtUtil.isExpired(token)) {
                String usernameFromToken = jwtUtil.getUsername(token);
                MemberDTO member = memberService.FindById(id);

                if (member != null && member.getUsername().equals(usernameFromToken)) {
                    return ResponseEntity.ok(member);
                } else {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/member/detail/{id}")
    public String getMemberDetailPage(@PathVariable("id") Long id, Model model) {
        MemberDTO member = memberService.FindById(id);

        if (member != null) {
            model.addAttribute("MemberDTO", member);
            return "MemberDetail";
        } else {
            return "error/404";
        }
    }

    @GetMapping("/member/update/{id}")
    public String update(@PathVariable("id") Long id, Model model) {
        MemberDTO memberDTO = memberService.FindById(id);
        model.addAttribute("MemberDTO", memberDTO);
        return "MemberUpdate";
    }

    @PostMapping("/member/verify-password")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> verifyPassword(@RequestBody Map<String, String> request) {
        System.out.println("hear");
        String oldPassword = request.get("oldPassword");
        String username = request.get("username");
        String storedHashedPassword = memberService.verifyPassword(username);

        boolean isPasswordValid = bCryptPasswordEncoder.matches(oldPassword, storedHashedPassword);

        Map<String, Boolean> response = new HashMap<>();
        response.put("valid", isPasswordValid);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/member/update/{id}")
    public String update(MemberDTO memberDTO, Model model) {
        memberService.updatePassword(memberDTO);
        MemberDTO dto = memberService.FindById(memberDTO.getId());
        model.addAttribute("MemberDTO", dto);
        return "MemberDetail";
    }

    @GetMapping("/member/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        memberService.delete(id);
        return "redirect:/member/list";
    }
}