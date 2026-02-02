package com.minzetsu.ecommerce.auth.service;

import com.minzetsu.ecommerce.auth.dto.request.LoginRequest;
import com.minzetsu.ecommerce.auth.dto.request.RegisterRequest;
import java.util.Random;
import java.time.LocalDateTime;
import com.minzetsu.ecommerce.auth.repository.EmailVerificationCodeRepository;
import com.minzetsu.ecommerce.auth.entity.EmailVerificationCode;
import com.minzetsu.ecommerce.auth.dto.request.RegisterOtpVerifyRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minzetsu.ecommerce.auth.dto.request.RefreshTokenRequest;
import com.minzetsu.ecommerce.auth.dto.response.AuthResponse;
import com.minzetsu.ecommerce.auth.entity.RefreshToken;
import com.minzetsu.ecommerce.common.config.CustomUserDetails;
import com.minzetsu.ecommerce.common.exception.AccountDisableException;
import com.minzetsu.ecommerce.common.exception.AlreadyExistException;
import com.minzetsu.ecommerce.common.exception.AppException;
import com.minzetsu.ecommerce.common.exception.NotFoundException;
import com.minzetsu.ecommerce.user.entity.Role;
import com.minzetsu.ecommerce.user.entity.User;
import com.minzetsu.ecommerce.user.repository.RoleRepository;
import com.minzetsu.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationCodeRepository emailVerificationCodeRepository;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;
    private final Random random = new Random();

    private void validateRegistration(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AlreadyExistException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AlreadyExistException("Email already exists");
        }
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Backward-compatible endpoint now starts OTP flow.
        requestRegisterOtp(request);
        return AuthResponse.builder().message("Verification code sent to email").build();
    }

    @Transactional
    public AuthResponse requestRegisterOtp(RegisterRequest request) {
        validateRegistration(request);
        String code = String.format("%06d", random.nextInt(1_000_000));
        try {
            EmailVerificationCode entity = new EmailVerificationCode();
            entity.setEmail(request.getEmail().trim().toLowerCase());
            entity.setCodeHash(passwordEncoder.encode(code));
            entity.setRegisterPayloadJson(objectMapper.writeValueAsString(request));
            entity.setAttempts(0);
            entity.setVerified(false);
            entity.setExpiresAt(LocalDateTime.now().plusMinutes(1));
            emailVerificationCodeRepository.save(entity);
            emailService.sendOtp(entity.getEmail(), code);
            return AuthResponse.builder().message("Verification code sent to email").build();
        } catch (Exception ex) {
            log.warn("Failed to send register OTP to email={}: {}", request.getEmail(), ex.getMessage());
            throw new AppException("Cannot send verification code. Please check email or try again later.", HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public AuthResponse verifyRegisterOtp(RegisterOtpVerifyRequest request) {
        EmailVerificationCode code = emailVerificationCodeRepository
                .findTopByEmailOrderByCreatedAtDesc(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new AppException("Verification code not found", HttpStatus.BAD_REQUEST));

        if (Boolean.TRUE.equals(code.getVerified())) throw new AppException("Code already used", HttpStatus.BAD_REQUEST);
        if (code.getExpiresAt().isBefore(LocalDateTime.now())) throw new AppException("Verification code expired", HttpStatus.BAD_REQUEST);
        if (code.getAttempts() >= 5) throw new AppException("Too many attempts", HttpStatus.BAD_REQUEST);

        code.setAttempts(code.getAttempts() + 1);
        if (!passwordEncoder.matches(request.getCode(), code.getCodeHash())) {
            emailVerificationCodeRepository.save(code);
            throw new AppException("Invalid verification code", HttpStatus.BAD_REQUEST);
        }

        try {
            RegisterRequest registerRequest = objectMapper.readValue(code.getRegisterPayloadJson(), RegisterRequest.class);
            validateRegistration(registerRequest);
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new NotFoundException("User role not found"));

            User user = User.builder()
                    .username(registerRequest.getUsername())
                    .email(registerRequest.getEmail())
                    .password(passwordEncoder.encode(registerRequest.getPassword()))
                    .fullName(registerRequest.getFullName())
                    .phone(registerRequest.getPhone())
                    .enabled(true)
                    .roles(List.of(userRole))
                    .build();
            userRepository.save(user);
            code.setVerified(true);
            emailVerificationCodeRepository.save(code);
            return AuthResponse.builder().message("User registered successfully").build();
        } catch (Exception ex) {
            throw new AppException("Cannot verify registration", HttpStatus.BAD_REQUEST);
        }
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.getEnabled()) {
            throw new AccountDisableException("Account is disabled");
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername());

        return AuthResponse.builder()
                .message("Login successful")
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .build();
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        return refreshTokenService.findByToken(request.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String accessToken = jwtService.generateToken(new CustomUserDetails(user));
                    return AuthResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(request.getRefreshToken())
                            .tokenType("Bearer")
                            .message("Token refreshed successfully")
                            .build();
                }).orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }
}
