package com.gyeongditor.storyfield.Controller;

import com.gyeongditor.storyfield.dto.UserDTO.UpdateUserDTO;
import com.gyeongditor.storyfield.dto.UserDTO.UserResponseDTO;
import com.gyeongditor.storyfield.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import seungil.login_boilerplate.dto.SignUpDTO;


import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    // 회원 가입
    @PostMapping("/signup")
    public ResponseEntity<UserResponseDTO> signUp(@Valid @RequestBody SignUpDTO signUpDTO) {
        UserResponseDTO userResponseDTO = userService.signUp(signUpDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDTO);
    }

    // 회원 정보 조회
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable UUID userId) {
        try {
            UserResponseDTO userResponseDTO = userService.getUserById(userId);
            return ResponseEntity.ok(userResponseDTO);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // 회원 정보 수정
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable UUID userId, @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        try {
            UserResponseDTO updatedUser = userService.updateUser(userId, updateUserDTO);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // 회원 삭제
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    // 이메일 인증
    @GetMapping("/verify/{token}")
    public ResponseEntity<String> verifyEmail(@PathVariable String token) {
        try {
            userService.verifyEmail(token);  // 성공 시 내부적으로 상태 저장 처리
            return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰입니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

}
