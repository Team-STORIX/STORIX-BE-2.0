package com.storix.domain.domains.user.service;

import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.domain.User;
import com.storix.domain.domains.user.dto.StandardProfileInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserAdaptor userAdaptor;

    public User findUserById(Long userId) {
        return userAdaptor.findUserById(userId);
    }

    public Map<Long, StandardProfileInfo> getProfileByUserIds(List<Long> userIds) {
        return userAdaptor.findStandardProfileInfoByUserIds(userIds);
    }
}
