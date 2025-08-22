package com.gyeongditor.storyfield.swagger;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.dto.UserDTO.SignUpDTO;
import com.gyeongditor.storyfield.dto.UserDTO.UpdateUserDTO;
import com.gyeongditor.storyfield.dto.UserDTO.UserResponseDTO;
import com.gyeongditor.storyfield.response.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "사용자")
@RequestMapping("/api/user")
public interface UserApi {

    @Operation(summary = "회원가입", description = "신규 유저를 등록합니다.")
    @ApiErrorExample({ErrorCode.USER_409_001}) // 중복 이메일
    @PostMapping("/signup")
    ApiResponseDTO<UserResponseDTO> signUp(@Valid @RequestBody SignUpDTO signUpDTO);

    @Operation(summary = "회원 정보 조회", description = "AccessToken을 기반으로 로그인한 사용자의 정보를 조회합니다.",
            parameters = {@Parameter(name = "Authorization", description = "Bearer {accessToken}", required = true)})
    @ApiErrorExample({
            ErrorCode.AUTH_401_003, // 토큰 없음
            ErrorCode.AUTH_401_004, // 토큰 유효하지 않음
            ErrorCode.AUTH_401_005, // 토큰 만료
            ErrorCode.AUTH_403_002  // 접근 권한 없음
    })
    @GetMapping("/me")
    ApiResponseDTO<UserResponseDTO> getUser(@RequestHeader("Authorization") String authorizationHeader);

    @Operation(summary = "회원 정보 수정", description = "AccessToken을 기반으로 로그인한 사용자의 정보를 수정합니다.",
            parameters = {@Parameter(name = "Authorization", description = "Bearer {accessToken}", required = true)})
    @ApiErrorExample({
            ErrorCode.REQ_422_001, // 데이터 유효성 검사 실패
            ErrorCode.AUTH_401_004, // 토큰 유효하지 않음
            ErrorCode.AUTH_403_002  // 권한 없음
    })
    @PutMapping("/me")
    ApiResponseDTO<UserResponseDTO> updateUser(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody UpdateUserDTO updateUserDTO);

    @Operation(summary = "회원 탈퇴", description = "AccessToken을 기반으로 로그인한 사용자의 계정을 삭제합니다.",
            parameters = {@Parameter(name = "Authorization", description = "Bearer {accessToken}", required = true)})
    @ApiErrorExample({
            ErrorCode.USER_404_001, // 존재하지 않는 계정
            ErrorCode.AUTH_401_004, // 토큰 유효하지 않음
            ErrorCode.AUTH_403_002  // 권한 없음
    })
    @DeleteMapping("/me")
    ApiResponseDTO<Void> deleteUser(@RequestHeader("Authorization") String authorizationHeader);

    @Operation(summary = "이메일 인증", description = "회원가입 또는 정보 수정 시 이메일에 전달된 인증 링크를 통해 계정을 활성화합니다.")
    @ApiErrorExample({
            ErrorCode.RES_404_001, // 토큰 불일치/만료
            ErrorCode.USER_410_001 // 탈퇴된 계정
    })
    @GetMapping("/verify/{token}")
    ApiResponseDTO<UserResponseDTO> verifyEmail(@PathVariable String token);
}
