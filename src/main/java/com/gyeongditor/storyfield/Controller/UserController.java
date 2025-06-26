package com.gyeongditor.storyfield.Controller;

import com.gyeongditor.storyfield.dto.UserDTO.UserLoginRequestDTO;
import com.gyeongditor.storyfield.dto.UserDTO.UserResponseDTO;
import com.gyeongditor.storyfield.dto.UserDTO.UserSignupRequestDTO;
import com.gyeongditor.storyfield.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;


@RestController
@RequestMapping("/api/user")
@Tag(name = "User API", description = "회원 관련 API")
public class UserController {

    private UserService userService;

    @PostMapping("/signup")
    @Operation(
            summary = "회원가입",
            description = "신규 회원 정보를 등록합니다.",
            requestBody = @RequestBody(
                    description = "회원가입 요청 데이터",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserSignupRequestDTO.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<Void> signup(
            @RequestBody UserSignupRequestDTO requestDTO
    ) {
        userService.signup(requestDTO);

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

