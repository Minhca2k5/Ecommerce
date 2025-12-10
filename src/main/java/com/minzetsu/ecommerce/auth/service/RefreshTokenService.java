package com.minzetsu.ecommerce.auth.service;

import com.minzetsu.ecommerce.auth.entity.RefreshToken;
import com.minzetsu.ecommerce.auth.repository.RefreshTokenRepository;
import com.minzetsu.ecommerce.common.config.CustomUserDetails;
import com.minzetsu.ecommerce.common.exception.NotFoundException;
import com.minzetsu.ecommerce.user.entity.User;
import com.minzetsu.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${application.security.jwt.refresh-token.expiration:604800000}")
    private long refreshExpiration;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public RefreshToken createRefreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check if user already has a token, delete it or update it
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUser(user);
        RefreshToken refreshToken;
        if (existingToken.isPresent()) {
            refreshToken = existingToken.get();
        } else {
            refreshToken = new RefreshToken();
            refreshToken.setUser(user);
        }

        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpiration));
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtService.generateRefreshToken(userDetails);
        refreshToken.setToken(token);

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException(token.getToken() + " Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Transactional
    public int deleteByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return refreshTokenRepository.deleteByUser(user);
    }
}
