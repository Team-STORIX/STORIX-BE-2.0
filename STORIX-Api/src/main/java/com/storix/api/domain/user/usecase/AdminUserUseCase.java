package com.storix.api.domain.user.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.domain.domains.user.domain.AccountState;
import com.storix.domain.domains.user.domain.Role;
import com.storix.domain.domains.user.dto.AdminUserListResponse;
import com.storix.domain.domains.user.dto.AdminUserPageResponse;
import com.storix.domain.domains.user.dto.AdminUserSearchCondition;
import com.storix.domain.domains.user.exception.auth.ForbiddenApproachException;
import com.storix.domain.domains.user.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@UseCase
@RequiredArgsConstructor
public class AdminUserUseCase {

    private final AdminUserService adminUserService;

    public AdminUserPageResponse searchUsers(
            AuthUserDetails authUserDetails,
            Long userId,
            String nickName,
            AccountState accountState,
            Pageable pageable
    ) {
        validateAdmin(authUserDetails);
        Page<AdminUserListResponse> userInfos = adminUserService.searchUsers(
                new AdminUserSearchCondition(userId, nickName, accountState),
                pageable
        );
        return AdminUserPageResponse.from(userInfos);
    }

    private void validateAdmin(AuthUserDetails authUserDetails) {
        if (authUserDetails == null || authUserDetails.getRole() != Role.SUPER_ADMIN) {
            throw ForbiddenApproachException.EXCEPTION;
        }
    }
}
