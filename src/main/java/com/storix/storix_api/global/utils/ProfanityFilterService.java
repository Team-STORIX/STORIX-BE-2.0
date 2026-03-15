package com.storix.storix_api.global.utils;

import com.storix.storix_api.global.apiPayload.exception.topicRoom.InvalidTitleException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfanityFilterService {

    // TODO: 리스트 확정 시 별도로 분리 예정
    private final List<String> bannedWords = List.of("비속어", "욕설", "정치");

    public void validate(String text) {

        for (String word : bannedWords) {

            if (text.contains(word)) {
                throw InvalidTitleException.EXCEPTION;
            }
        }
    }
}
