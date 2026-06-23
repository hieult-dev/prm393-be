package com.myfschool.service;

import com.myfschool.entity.PasswordResetToken;
import com.myfschool.repository.PasswordResetTokenRepository;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetTokenService extends AbstractCrudService<PasswordResetToken> {

    public PasswordResetTokenService(PasswordResetTokenRepository repository) {
        super(repository, "Password reset token");
    }
}
