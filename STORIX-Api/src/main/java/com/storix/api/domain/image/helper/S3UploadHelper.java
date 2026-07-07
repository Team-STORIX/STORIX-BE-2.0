package com.storix.api.domain.image.helper;

import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.image.domain.EventImageSurface;
import com.storix.domain.domains.image.exception.ImageInvalidContentTypeException;
import com.storix.domain.domains.image.exception.ImageUploadFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3UploadHelper {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    // 앱 이벤트 이미지 업로드. 경로: public/event/{appEventId}/{surface}
    public String uploadEventImage(MultipartFile file, Long appEventId, EventImageSurface surface) {
        String appEventSegment = appEventId == null ? "common" : appEventId.toString();
        String prefix = STORIXStatic.S3Prefix.EVENT + "/" + appEventSegment + "/" + surface.getValue();
        return upload(file, prefix);
    }

    // {prefix}/{UUID}.ext 형태로 업로드 후 objectKey 반환
    public String upload(MultipartFile file, String objectKeyPrefix) {
        String contentType = file.getContentType();
        String ext = contentTypeToExt(contentType);
        String objectKey = objectKeyPrefix + "/" + UUID.randomUUID() + "." + ext;
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(file.getBytes()));
        } catch (IOException e) {
            throw ImageUploadFailedException.EXCEPTION;
        }
        return objectKey;
    }

    // 실패해도 요청은 진행. 남은 객체는 후속 청소 대상
    public void delete(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return;
        }
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build());
        } catch (Exception e) {
            log.warn(">>> [S3] object 삭제 실패. key={}", objectKey, e);
        }
    }

    private String contentTypeToExt(String contentType) {
        if (contentType == null) {
            throw ImageInvalidContentTypeException.EXCEPTION;
        }
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> throw ImageInvalidContentTypeException.EXCEPTION;
        };
    }
}
