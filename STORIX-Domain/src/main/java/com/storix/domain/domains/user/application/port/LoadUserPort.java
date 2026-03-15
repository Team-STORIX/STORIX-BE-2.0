package com.storix.domain.domains.user.application.port;

import com.storix.domain.domains.user.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface LoadUserPort {

    Slice<User> searchArtists(String keyword, Pageable pageable);

    User findById(Long userId);

    Boolean findIsAdultVerifiedById(Long userId);
}
