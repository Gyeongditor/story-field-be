package com.gyeongditor.storyfield.Entity;

import com.gyeongditor.storyfield.dto.UserDTO.UpdateUserDTO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user")
@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private UUID userId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String userName;

    @CreationTimestamp // INSERT 쿼리가 발생할 때, 현재 시간을 자동으로 저장
    private LocalDateTime created_at; // 회원가입한 시간

    @UpdateTimestamp // UPDATE 쿼리가 발생할 때, 현재 시간을 자동으로 저장
    private LocalDateTime updated_at; // 마지막으로 수정한 시간

    private boolean accountNonExpired; // 계정 만료 여부
    private boolean accountNonLocked; // 계정 잠김 여부
    private boolean credentialsNonExpired; // 자격 증명 만료 여부
    private boolean enabled; // 계정 활성화 여부
    private String mailVerificationToken; // 이메일 인증 토큰
    private int failedLoginAttempts; // 로그인 시도 횟수
    private LocalDateTime lockTime; // 계정 잠금 해제 시간


    public void updateUser(UpdateUserDTO updateUserDTO, PasswordEncoder passwordEncoder, String mailVerificationToken) {
        this.userName = updateUserDTO.getUserName();
        this.email = updateUserDTO.getEmail();
        this.mailVerificationToken = mailVerificationToken;
        this.enabled = false;

        // 새로운 비밀번호가 null이 아니고, 기존 비밀번호와 다를 때만 인코딩하여 업데이트
        if (updateUserDTO.getPassword() != null && !updateUserDTO.getPassword().equals(this.password)) {
            this.password = passwordEncoder.encode(updateUserDTO.getPassword());
        }
    }

    public void enableAccount() {
        this.enabled = true;
        this.mailVerificationToken = null;
    }

    // 로그인 실패 시 로그인 시도 횟수 증가
    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
    }

    // 로그인 성공 시 로그인 시도 횟수 초기화
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
    }

    // 계정 잠금
    public void lockAccount() {
        this.accountNonLocked = false;
        this.lockTime = LocalDateTime.now();
    }

    // 계정 잠금 풀기
    public void unlockAccount() {
        this.accountNonLocked = true;
        this.lockTime = null;
    }

    public boolean isLockTimeExpired(int lockDurationMinutes) {
        if (this.lockTime == null) {
            return true; // 잠금 시간이 없으면 바로 해제 가능
        }
        LocalDateTime expiryTime = this.lockTime.plusMinutes(lockDurationMinutes);
        return expiryTime.isBefore(LocalDateTime.now()); // 현재 시간이 잠금 만료 시간 이전이면 해제 가능
    }
}
