package com.myfschool.service;

import com.myfschool.dto.response.RefreshTokenResult;
import com.myfschool.entity.RefreshToken;
import com.myfschool.entity.User;
import com.myfschool.exception.InvalidCredentialsException;
import com.myfschool.exception.ResourceNotFoundException;
import com.myfschool.repository.RefreshTokenRepository;
import com.myfschool.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final UserRepository userRepository;
    private final Duration expiration;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository repository,
            UserRepository userRepository,
            @Value("${app.jwt.refresh-expiration}") Duration expiration
    ) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.expiration = expiration;
    }

    @Transactional
    public RefreshTokenResult createForUser(User user) {
        String rawToken = generateRawToken();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(user.getId());
        refreshToken.setTokenHash(hash(rawToken));
        refreshToken.setExpiresAt(LocalDateTime.now().plus(expiration));
        refreshToken.setRevoked(false);
        repository.save(refreshToken);
        return new RefreshTokenResult(rawToken, expiration.toSeconds(), user);
    }

    @Transactional
    public RefreshTokenResult rotate(String rawToken) {
        RefreshToken existing = findValidToken(rawToken);
        existing.setRevoked(true);
        existing.setRevokedAt(LocalDateTime.now());
        repository.save(existing);

        User user = userRepository.findById(existing.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", existing.getUserId()));
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new InvalidCredentialsException();
        }
        return createForUser(user);
    }

    @Transactional
    public void revoke(String rawToken) {
        repository.findByTokenHash(hash(rawToken))
                .filter(token -> !Boolean.TRUE.equals(token.getRevoked()))
                .ifPresent(token -> {
                    token.setRevoked(true);
                    token.setRevokedAt(LocalDateTime.now());
                    repository.save(token);
                });
    }

    public long getExpirationSeconds() {
        return expiration.toSeconds();
    }

    private RefreshToken findValidToken(String rawToken) {
        RefreshToken token = repository.findByTokenHash(hash(rawToken))
                .orElseThrow(InvalidCredentialsException::new);
        if (Boolean.TRUE.equals(token.getRevoked())
                || !token.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new InvalidCredentialsException();
        }
        return token;
    }

    private String generateRawToken() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
