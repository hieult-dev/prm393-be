package com.myfschool.service;

import com.myfschool.entity.User;
import com.myfschool.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService extends AbstractCrudService<User> {

    public UserService(UserRepository repository) {
        super(repository, "User");
    }
}
