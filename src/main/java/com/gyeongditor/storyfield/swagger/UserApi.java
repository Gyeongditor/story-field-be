package com.gyeongditor.storyfield.swagger;

import com.gyeongditor.storyfield.dto.ApiResponseDTO;
import com.gyeongditor.storyfield.dto.UserDTO.SignUpDTO;
import com.gyeongditor.storyfield.dto.UserDTO.UpdateUserDTO;
import com.gyeongditor.storyfield.dto.UserDTO.UserResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "사용자")
@RequestMapping("/api/user")
public interface UserApi {

    @Operation(summary = "회원가입", description = "신규 유저를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {"status":201,"code":"USER_201_001","message":"회원가입이 완료되었습니다.",
             "data":{"email":"newuser@example.com","username":"홍길동"}}"""))),
            @ApiResponse(responseCode = "409", description = "중복 이메일",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {"status":409,"code":"USER_409_001","message":"중복된 이메일","data":null}""")))
    })
    @PostMapping("/signup")
    ApiResponseDTO<UserResponseDTO> signUp(@Valid @RequestBody SignUpDTO signUpDTO);

    @Operation(summary = "회원 정보 조회", description = "AccessToken을 기반으로 로그인한 사용자의 정보를 조회합니다.",
            parameters = {@Parameter(name = "Authorization", description = "Bearer {accessToken}", required = true)})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {"status":200,"code":"USER_200_001","message":"회원 정보 조회가 성공적으로 완료되었습니다.",
             "data":{"email":"me@example.com","username":"홍길동"}}"""))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {"status":401,"code":"AUTH_401_004","message":"토큰 유효하지 않음","data":null}""")))
    })
    @GetMapping("/me")
    ApiResponseDTO<UserResponseDTO> getUser(@RequestHeader("Authorization") String authorizationHeader);

    @Operation(summary = "회원 정보 수정", description = "AccessToken을 기반으로 로그인한 사용자의 정보를 수정합니다.",
            parameters = {@Parameter(name = "Authorization", description = "Bearer {accessToken}", required = true)})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {"status":200,"code":"USER_200_002","message":"회원 정보가 성공적으로 수정되었습니다.",
             "data":{"email":"new@example.com","username":"길동이"}}"""))),
            @ApiResponse(responseCode = "422", description = "유효성 실패",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {"status":422,"code":"REQ_422_001","message":"데이터 유효성 검사 실패","data":null}""")))
    })
    @PutMapping("/me")
    ApiResponseDTO<UserResponseDTO> updateUser(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody UpdateUserDTO updateUserDTO);

    @Operation(summary = "회원 탈퇴", description = "AccessToken을 기반으로 로그인한 사용자의 계정을 삭제합니다.",
            parameters = {@Parameter(name = "Authorization", description = "Bearer {accessToken}", required = true)})
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {"status":204,"code":"USER_204_001","message":"회원이 성공적으로 삭제되었습니다.","data":null}""")))
    })
    @DeleteMapping("/me")
    ApiResponseDTO<Void> deleteUser(@RequestHeader("Authorization") String authorizationHeader);

    @Operation(summary = "이메일 인증", description = "회원가입 또는 정보 수정 시 이메일에 전달된 인증 링크를 통해 계정을 활성화합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인증 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {"status":200,"code":"USER_200_003","message":"이메일 인증이 완료되었습니다.",
             "data":{"email":"newuser@example.com","username":"홍길동"}}"""))),
            @ApiResponse(responseCode = "404", description = "토큰 불일치/만료",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
            {"status":404,"code":"RES_404_001","message":"유효한 인증 토큰이 없습니다.","data":null}""")))
    })
    @GetMapping("/verify/{token}")
    ApiResponseDTO<UserResponseDTO> verifyEmail(@PathVariable String token);
}
