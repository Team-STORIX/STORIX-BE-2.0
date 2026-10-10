package com.storix.domain.domains.hashtag.dto;

import lombok.Getter;

@Getter
public class HashtagScore {

    private final Long id;
    private final String name;
    private double rawScore;
    private int positiveCount;

    public HashtagScore(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addRawScore(double score) {
        this.rawScore += score;
    }

    public void increasePositiveCount() {
        this.positiveCount++;
    }
}
