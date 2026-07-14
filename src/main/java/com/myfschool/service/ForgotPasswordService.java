package com.myfschool.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.myfschool.dto.request.ResetPasswordRequest;
import com.myfschool.dto.response.ResetTokenResponse;
import com.myfschool.entity.PasswordResetToken;
import com.myfschool.entity.User;
import com.myfschool.exception.BadRequestException;
import com.myfschool.exception.InvalidCredentialsException;
import com.myfschool.exception.ResourceNotFoundException;
import com.myfschool.repository.UserRepository;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ForgotPasswordService {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final String PHONE_PROVIDER = "phone";

    private final FirebaseAuth firebaseAuth;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenService passwordResetTokenService;

    public ForgotPasswordService(
            FirebaseAuth firebaseAuth,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            PasswordResetTokenService passwordResetTokenService
    ) {
        this.firebaseAuth = firebaseAuth;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetTokenService = passwordResetTokenService;
    }

    @Transactional
    public ResetTokenResponse verifyPhone(String firebaseIdToken) {
        FirebaseToken firebaseToken = verifyFirebaseToken(requireFirebaseToken(firebaseIdToken));
        requirePhoneProvider(firebaseToken);
        String phoneNumber = extractPhoneNumber(firebaseToken);
        String normalizedPhone = normalizePhoneNumber(phoneNumber);
        User user = userRepository.findByPhone(normalizedPhone)
                .orElseThrow(() -> new ResourceNotFoundException("User", "phone", normalizedPhone));
        return passwordResetTokenService.issueForUser(user.getId());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BadRequestException("Mật khẩu xác nhận không khớp");
        }
        if (request.newPassword().length() < MIN_PASSWORD_LENGTH) {
            throw new BadRequestException("Mật khẩu mới phải có ít nhất 8 ký tự");
        }

        PasswordResetToken resetToken = passwordResetTokenService.requireUsableToken(request.resetToken());
        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", resetToken.getUserId()));
        user.setUserPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        passwordResetTokenService.markUsed(resetToken);
    }

    private String requireFirebaseToken(String firebaseIdToken) {
        if (firebaseIdToken == null || firebaseIdToken.isBlank()) {
            throw new InvalidCredentialsException("Firebase ID token không hợp lệ");
        }
        String idToken = firebaseIdToken.trim();
        if (idToken.isBlank()) {
            throw new InvalidCredentialsException("Firebase ID token không hợp lệ");
        }
        return idToken;
    }

    private FirebaseToken verifyFirebaseToken(String idToken) {
        try {
            return firebaseAuth.verifyIdToken(idToken);
        } catch (FirebaseAuthException exception) {
            throw new InvalidCredentialsException("Firebase ID token không hợp lệ");
        }
    }

    private String extractPhoneNumber(FirebaseToken firebaseToken) {
        Object phoneNumber = firebaseToken.getClaims().get("phone_number");
        if (!(phoneNumber instanceof String value) || value.isBlank()) {
            throw new BadRequestException("Firebase token không có phone_number");
        }
        return value;
    }

    private void requirePhoneProvider(FirebaseToken firebaseToken) {
        Object firebaseClaim = firebaseToken.getClaims().get("firebase");
        if (!(firebaseClaim instanceof Map<?, ?> firebaseMap)) {
            return;
        }
        Object provider = firebaseMap.get("sign_in_provider");
        if (provider != null && !PHONE_PROVIDER.equals(provider.toString())) {
            throw new BadRequestException("Firebase token không được xác thực bằng phone");
        }
    }

    private String normalizePhoneNumber(String phoneNumber) {
        String normalized = phoneNumber.trim()
                .replace(" ", "")
                .replace("-", "")
                .replace(".", "")
                .replace("(", "")
                .replace(")", "");
        if (normalized.startsWith("+84")) {
            return "0" + normalized.substring(3);
        }
        if (normalized.startsWith("84")) {
            return "0" + normalized.substring(2);
        }
        return normalized;
    }
}
