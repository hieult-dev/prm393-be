package com.myfschool.controller;

import com.myfschool.entity.PasswordResetToken;
import com.myfschool.service.PasswordResetTokenService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/password-reset-tokens")
public class PasswordResetTokenController extends AbstractCrudController<PasswordResetToken> {

    public PasswordResetTokenController(PasswordResetTokenService service) {
        super(service);
    }
}
