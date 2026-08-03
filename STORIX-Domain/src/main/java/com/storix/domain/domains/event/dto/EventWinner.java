package com.storix.domain.domains.event.dto;

public record EventWinner(
        Long userId,
        int drawOrder
) {
}
