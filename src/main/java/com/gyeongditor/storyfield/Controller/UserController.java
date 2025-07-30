package com.gyeongditor.storyfield.Controller;

import com.gyeongditor.storyfield.dto.UserDTO.SignUpDTO;
import com.gyeongditor.storyfield.dto.UserDTO.UpdateUserDTO;
import com.gyeongditor.storyfield.dto.UserDTO.UserResponseDTO;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "신규 유저 등록")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류")
    })
    @PostMapping("/signup")
    public ApiResponseDTO<UserResponseDTO> signUp(@Valid @RequestBody SignUpDTO signUpDTO) {
        return userService.signUp(signUpDTO);
    }

    @Operation(summary = "유저 조회", description = "userId에 해당하는 유저 정보를 조회합니다.")
    @GetMapping("/{userId}")
    public ApiResponseDTO<UserResponseDTO> getUser(@PathVariable UUID userId) {
        return userService.getUserById(userId);
    }

    @Operation(summary = "유저 정보 수정", description = "userId에 해당하는 유저 정보를 수정합니다.")
    @PutMapping("/{userId}")
    public ApiResponseDTO<UserResponseDTO> updateUser(@PathVariable UUID userId, @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        return userService.updateUser(userId, updateUserDTO);
    }

    @Operation(summary = "유저 삭제", description = "userId에 해당하는 유저를 삭제합니다.")
    @DeleteMapping("/{userId}")
    public ApiResponseDTO<Void> deleteUser(@PathVariable UUID userId) {
        return userService.deleteUser(userId);
    }

    @Operation(summary = "이메일 인증", description = "이메일로 전달된 토큰을 통해 회원 인증을 수행합니다.")
    @GetMapping("/verify/{token}")
    public ApiResponseDTO<UserResponseDTO> verifyEmail(@PathVariable String token) {
        return userService.verifyEmail(token);
    }
}
