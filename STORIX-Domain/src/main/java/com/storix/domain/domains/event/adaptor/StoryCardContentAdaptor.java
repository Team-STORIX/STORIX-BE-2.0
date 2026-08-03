package com.storix.domain.domains.event.adaptor;

import com.storix.domain.domains.event.domain.StoryCardImmersion;
import com.storix.domain.domains.event.domain.StoryCardMessage;
import com.storix.domain.domains.event.exception.StoryCardContentNotFoundException;
import com.storix.domain.domains.event.repository.StoryCardImmersionRepository;
import com.storix.domain.domains.event.repository.StoryCardMessageRepository;
import com.storix.domain.domains.works.domain.Genre;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StoryCardContentAdaptor {

    private final StoryCardMessageRepository storyCardMessageRepository;
    private final StoryCardImmersionRepository storyCardImmersionRepository;

    public StoryCardMessage pickMessage(Genre genre) {
        return storyCardMessageRepository.findRandomByGenre(genre.getDbValue())
                .orElseThrow(() -> StoryCardContentNotFoundException.EXCEPTION);
    }

    public StoryCardImmersion pickImmersion() {
        return storyCardImmersionRepository.findRandom()
                .orElseThrow(() -> StoryCardContentNotFoundException.EXCEPTION);
    }
}
