package com.gyeongditor.storyfield.Controller;

import com.gyeongditor.storyfield.dto.UserDTO.UserLoginRequestDTO;
import com.gyeongditor.storyfield.dto.UserDTO.UserResponseDTO;
import com.gyeongditor.storyfield.dto.UserDTO.UserSignupRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User API", description = "회원 관련 API")
public class UserController {

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "신규 회원을 등록.")
    public ResponseEntity<Void> signup(@RequestBody UserSignupRequestDTO requestDTO) {
        // 회원가입 로직
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 로그인")
    public ResponseEntity<String> login(@RequestBody UserLoginRequestDTO requestDTO) {
        // 로그인 로직
        return ResponseEntity.ok("로그인 성공");
    }

    @GetMapping("/{userId}")
    @Operation(summary = "회원 조회", description = "회원 정보를 반환.")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable String userId) {
        // 조회 로직
        return ResponseEntity.ok(new UserResponseDTO());
    }
}

