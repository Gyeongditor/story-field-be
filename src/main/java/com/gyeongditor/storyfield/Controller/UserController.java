package com.gyeongditor.storyfield.Controller;

import com.gyeongditor.storyfield.dto.UserDTO.SignUpDTO;
import com.gyeongditor.storyfield.dto.UserDTO.UpdateUserDTO;
import com.gyeongditor.storyfield.dto.UserDTO.UserResponseDTO;
import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "사용자")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "신규 유저를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일"),
            @ApiResponse(responseCode = "400", description = "입력값 유효성 실패")
    })
    @PostMapping("/signup")
    public ApiResponseDTO<UserResponseDTO> signUp(@Valid @RequestBody SignUpDTO signUpDTO) {
        return userService.signUp(signUpDTO);
    }

    @Operation(
            summary = "회원 정보 조회",
            description = "AccessToken을 기반으로 로그인한 사용자의 정보를 조회합니다.",
            parameters = {
                    @Parameter(name = "Authorization", description = "Bearer {accessToken}", required = true)
            }
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/me")
    public ApiResponseDTO<UserResponseDTO> getUser(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String accessToken = authorizationHeader.replace("Bearer ", "");
        return userService.getUserByAccessToken(accessToken);
    }

    @Operation(
            summary = "회원 정보 수정",
            description = "AccessToken을 기반으로 로그인한 사용자의 정보를 수정합니다.",
            parameters = {
                    @Parameter(name = "Authorization", description = "Bearer {accessToken}", required = true)
            }
    )
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @PutMapping("/me")
    public ApiResponseDTO<UserResponseDTO> updateUser(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody UpdateUserDTO updateUserDTO
    ) {
        String accessToken = authorizationHeader.replace("Bearer ", "");
        return userService.updateUserByAccessToken(accessToken, updateUserDTO);
    }

    @Operation(
            summary = "회원 탈퇴",
            description = "AccessToken을 기반으로 로그인한 사용자의 계정을 삭제합니다.",
            parameters = {
                    @Parameter(name = "Authorization", description = "Bearer {accessToken}", required = true)
            }
    )
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @DeleteMapping("/me")
    public ApiResponseDTO<Void> deleteUser(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String accessToken = authorizationHeader.replace("Bearer ", "");
        return userService.deleteUserByAccessToken(accessToken);
    }

    @Operation(summary = "이메일 인증", description = "회원가입 또는 정보 수정 시 이메일에 전달된 인증 링크를 통해 계정을 활성화합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인증 성공"),
            @ApiResponse(responseCode = "404", description = "토큰에 해당하는 사용자를 찾을 수 없음")
    })
    @GetMapping("/verify/{token}")
    public ApiResponseDTO<UserResponseDTO> verifyEmail(@PathVariable String token) {
        return userService.verifyEmail(token);
    }
}
