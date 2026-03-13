package com.storix.domain.domains.plus.adaptor;

import com.storix.domain.domains.plus.domain.ReaderBoard;
import com.storix.domain.domains.plus.domain.ReaderBoardImage;
import com.storix.domain.domains.plus.dto.ReaderBoardImageInfo;
import com.storix.domain.domains.plus.repository.ReaderBoardImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BoardImageAdaptor {

    @Value("${AWS_S3_BASE_URL}") private String baseUrl;

    private final ReaderBoardImageRepository readerBoardImageRepository;

    // 게시글 이미지 저장
    public void saveReaderBoardImages(ReaderBoard readerBoard, List<String> objectKeys) {
        List<ReaderBoardImage> images = buildImages(objectKeys,
                (objectKey, sortOrder) -> ReaderBoardImage.of(readerBoard, objectKey, sortOrder)
        );

        readerBoardImageRepository.saveAll(images);
    }

    private <T> List<T> buildImages(List<String> objectKeys, ImageFactory<T> factory) {
        if (objectKeys == null || objectKeys.isEmpty()) return List.of();

        List<T> images = new ArrayList<>(objectKeys.size());
        for (int i = 0; i < objectKeys.size(); i++) {
            String key = objectKeys.get(i);
            images.add(factory.create(key, i));
        }
        return images;
    }

    @FunctionalInterface
    private interface ImageFactory<T> {
        T create(String objectKey, int sortOrder);
    }

    // 게시글 이미지 조회
    public Map<Long, List<ReaderBoardImageInfo>> findReaderBoardImagesByBoardIds( List<Long> boardIds) {

        if (boardIds == null || boardIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<ReaderBoardImage> images =
                readerBoardImageRepository.findAllByBoardIds(boardIds);

        return images.stream()
                .map(img -> ReaderBoardImageInfo.from(img, baseUrl))
                .collect(Collectors.groupingBy(
                        ReaderBoardImageInfo::boardId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

}