package com.myfschool.service;

import com.myfschool.dto.response.ResetTokenResponse;
import com.myfschool.entity.PasswordResetToken;
import com.myfschool.exception.InvalidCredentialsException;
import com.myfschool.exception.ResetTokenExpiredException;
import com.myfschool.repository.PasswordResetTokenRepository;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetTokenService extends AbstractCrudService<PasswordResetToken> {

    private static final Duration RESET_TOKEN_TTL = Duration.ofMinutes(10);
    private static final int TOKEN_BYTES = 48;

    private final PasswordResetTokenRepository repository;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetTokenService(PasswordResetTokenRepository repository) {
        super(repository, "Password reset token");
        this.repository = repository;
    }

    @Transactional
    public ResetTokenResponse issueForUser(Long userId) {
        repository.findByUserIdAndUsedFalse(userId)
                .forEach(existing -> existing.setUsed(true));

        PasswordResetToken token = new PasswordResetToken();
        token.setUserId(userId);
        token.setToken(generateToken());
        token.setExpiredAt(LocalDateTime.now().plus(RESET_TOKEN_TTL));
        token.setUsed(false);
        repository.save(token);

        return new ResetTokenResponse(token.getToken(), RESET_TOKEN_TTL.toSeconds());
    }

    @Transactional(readOnly = true)
    public PasswordResetToken requireUsableToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidCredentialsException("Reset token không hợp lệ");
        }
        PasswordResetToken token = repository.findByToken(rawToken.trim())
                .orElseThrow(() -> new InvalidCredentialsException("Reset token không hợp lệ"));
        if (Boolean.TRUE.equals(token.getUsed())) {
            throw new InvalidCredentialsException("Reset token không hợp lệ");
        }
        if (!token.getExpiredAt().isAfter(LocalDateTime.now())) {
            throw new ResetTokenExpiredException();
        }
        return token;
    }

    @Transactional
    public void markUsed(PasswordResetToken token) {
        token.setUsed(true);
        repository.save(token);
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
