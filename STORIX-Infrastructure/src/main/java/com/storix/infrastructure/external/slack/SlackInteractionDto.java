package com.storix.infrastructure.external.slack;


public record SlackInteractionDto(
        String actionId,
        String pendingId,
        String responseUrl
) {
}
