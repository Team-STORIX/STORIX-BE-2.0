package com.storix.domain.domains.user.service;

import com.storix.domain.domains.user.dto.AdminUserListResponse;
import com.storix.domain.domains.user.dto.AdminUserSearchCondition;
import com.storix.domain.domains.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;

    public Page<AdminUserListResponse> searchUsers(AdminUserSearchCondition condition, Pageable pageable) {
        String nickName = StringUtils.hasText(condition.nickName()) ? condition.nickName().trim() : null;
        return userRepository.searchAdminUsers(
                condition.userId(),
                nickName,
                condition.accountState(),
                pageable
        );
    }
}
