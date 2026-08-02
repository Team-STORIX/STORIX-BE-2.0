package com.storix.domain.domains.event.dto;

import com.storix.domain.domains.event.domain.StoryCardDraw;

public record StoryCardDrawResult(StoryCardDraw draw, boolean created) {
}
