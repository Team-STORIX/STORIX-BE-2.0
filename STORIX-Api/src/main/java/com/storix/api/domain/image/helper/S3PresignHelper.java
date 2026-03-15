package com.storix.api.domain.image.helper;

import com.storix.domain.domains.image.dto.PresignedUrlResponse;

import com.storix.domain.domains.image.exception.ImageInvalidContentTypeException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3PresignHelper {

    private final S3Presigner presigner;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static String nowAsKeyPart() {
        return LocalDateTime.now().format(FORMATTER);
    }

    // 공개/비공개 컨텐츠 용 (프로필, 게시글, 리뷰)
    public PresignedUrlResponse createPresignedPutUrl(Long userId, String contentType, String objectKeyPrefix) {

        String ext = contentTypeToExt(contentType);
        String objectKey = objectKeyPrefix + "/" + userId + "/" + UUID.randomUUID() + "." + ext;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);

        return new PresignedUrlResponse(
                presignedRequest.url().toString(),
                objectKey,
                presignRequest.signatureDuration().toSeconds()
        );
    }

    // 작가 팬 콘텐츠(비공개) 조회용
    public String createPresignedGetUrl(String objectKey) {

        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10)) // 피드 캐싱 시간과 동일하게 업데이트할 에정
                .getObjectRequest(objectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);

        return presignedRequest.url().toExternalForm();
    }

    // 이미지 다운로드 용
    public PresignedUrlResponse createPresignedDownloadUrl(String objectKey, String contentType) {

        String ext = contentTypeToExt(contentType);
        String downloadFileName = nowAsKeyPart() + "." + ext;

        GetObjectRequest getReq = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .responseContentDisposition(downloadFileName)
                .responseContentType(contentType)
                .build();

        GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(getReq)
                .build();

        PresignedGetObjectRequest presigned = presigner.presignGetObject(presignReq);

        return new PresignedUrlResponse(
                presigned.url().toString(),
                objectKey,
                presignReq.signatureDuration().toSeconds()
        );
    }

    // ContentType
    private String contentTypeToExt(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> throw ImageInvalidContentTypeException.EXCEPTION;
        };
    }

}
