package com.storix.storix_api.domains.plus.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@MappedSuperclass
@Getter
public abstract class BoardImage {

    @Column(name = "image_object_key", nullable = false)
    protected String imageObjectKey;

    @Column(name = "sort_order", nullable = false)
    protected int sortOrder;

    public void changeOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

}
