package com.gyeongditor.storyfield.Controller;

import com.gyeongditor.storyfield.dto.UserDTO.LoginDTO;
import com.gyeongditor.storyfield.dto.UserDTO.ResponseDTO;
import com.gyeongditor.storyfield.exception.UserAccountLockedException;
import com.gyeongditor.storyfield.exception.UserNotEnabledException;
import com.gyeongditor.storyfield.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            HttpHeaders headers = authService.login(loginDTO.getEmail(), loginDTO.getPassword());
            ResponseDTO response = new ResponseDTO(HttpStatus.OK.value(), "로그인에 성공했습니다.");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(response);
        } catch (UsernameNotFoundException e) {
            ResponseDTO response = new ResponseDTO(HttpStatus.NOT_FOUND.value(), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (UserNotEnabledException e) {
            ResponseDTO response = new ResponseDTO(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (UserAccountLockedException e) {
            ResponseDTO response = new ResponseDTO(HttpStatus.LOCKED.value(), e.getMessage());
            return ResponseEntity.status(HttpStatus.LOCKED).body(response);
        } catch (AuthenticationException e) {
            int remainingAttempts = authService.getRemainingLoginAttempts(loginDTO.getEmail());
            String message = "이메일 주소나 비밀번호가 올바르지 않습니다. " +
                    remainingAttempts + "번 더 로그인에 실패하면 계정이 잠길 수 있습니다.";
            ResponseDTO response = new ResponseDTO(HttpStatus.UNAUTHORIZED.value(), message);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(name = "Refresh-Token") String refreshToken) {
        boolean logoutSuccess = authService.logout(refreshToken);

        if (logoutSuccess) {
            return ResponseEntity.noContent().build(); // 204 No Content
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // or INTERNAL_SERVER_ERROR
        }
    }
}
