package com.storix.domain.domains.plus.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "reader_board_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReaderBoardImage extends BoardImage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reader_board_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reader_board_id")
    private ReaderBoard readerBoard;

    /** 생성자 로직 **/
    @Builder
    public ReaderBoardImage(ReaderBoard readerBoard, String imageObjectKey, int sortOrder) {
        this.readerBoard = readerBoard;
        this.imageObjectKey = imageObjectKey;
        this.sortOrder = sortOrder;
    }

    public static ReaderBoardImage of(ReaderBoard readerBoard, String objectKey, int sortOrder) {
        return new ReaderBoardImage(readerBoard, objectKey, sortOrder);
    }

}
