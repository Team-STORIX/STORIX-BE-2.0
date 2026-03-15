package com.storix.domain.domains.user.application.port;

import com.storix.domain.domains.user.domain.User;

public interface LoadUserPort {

    User findById(Long userId);

    Boolean findIsAdultVerifiedById(Long userId);
}
