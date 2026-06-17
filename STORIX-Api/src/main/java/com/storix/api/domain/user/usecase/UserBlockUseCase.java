package com.storix.api.domain.user.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.user.service.UserBlockService;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class UserBlockUseCase {

    private final UserBlockService userBlockService;

    public CustomResponse<Void> blockUser(Long blockerId, Long blockedUserId) {
        userBlockService.blockUser(blockerId, blockedUserId);
        return CustomResponse.onSuccess(SuccessCode.USER_BLOCK_SUCCESS);
    }
}
