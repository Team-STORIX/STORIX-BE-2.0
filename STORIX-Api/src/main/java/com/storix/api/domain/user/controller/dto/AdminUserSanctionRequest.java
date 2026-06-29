package com.storix.api.domain.user.controller.dto;

import com.storix.domain.domains.user.domain.UserSanctionType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AdminUserSanctionRequest(
        @NotNull UserSanctionType type,
        @Size(max = 500) String memo
) {

}
