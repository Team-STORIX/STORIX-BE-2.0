package com.storix.domain.domains.user.application.port;

public interface LoadUserPort {

    Boolean findIsAdultVerifiedById(Long userId);
}
