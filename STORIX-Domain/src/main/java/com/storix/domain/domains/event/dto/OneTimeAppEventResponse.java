package com.storix.domain.domains.event.dto;

import java.util.Map;

public record OneTimeAppEventResponse(
        Long id,

        String type,

        boolean ackRequired,

        Map<String, Object> payload
) {
}
