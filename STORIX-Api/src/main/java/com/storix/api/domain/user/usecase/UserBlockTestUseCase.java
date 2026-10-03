package com.storix.api.domain.user.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.user.service.UserBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;

@UseCase
@RequiredArgsConstructor
@Profile({"local", "dev"})
public class UserBlockTestUseCase {

    private final UserBlockService userBlockService;

    public CustomResponse<Integer> unblockUser(Long blockerId, Long blockedUserId) {
        int deleted = userBlockService.unblockUser(blockerId, blockedUserId);

        return CustomResponse.onSuccess(SuccessCode.USER_UNBLOCK_SUCCESS, deleted);
    }
}
