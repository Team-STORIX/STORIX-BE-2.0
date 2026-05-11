package com.storix.domain.domains.user.service;

import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserAdaptor userAdaptor;

    public User findUserById(Long userId) {
        return userAdaptor.findUserById(userId);
    }

}
