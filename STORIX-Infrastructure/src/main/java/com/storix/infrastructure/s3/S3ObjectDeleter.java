package com.storix.infrastructure.s3;

import com.storix.common.utils.STORIXStatic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.S3Error;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3ObjectDeleter {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    // 다건 삭제
    public void deleteObjects(List<String> objectKeys) {
        if (objectKeys == null || objectKeys.isEmpty()) {
            return;
        }

        List<ObjectIdentifier> identifiers = objectKeys.stream()
                .filter(StringUtils::hasText)
                .map(key -> ObjectIdentifier.builder().key(key).build())
                .toList();

        for (int from = 0; from < identifiers.size(); from += STORIXStatic.S3_MAX_KEYS_PER_DELETE_REQUEST) {
            int to = Math.min(from + STORIXStatic.S3_MAX_KEYS_PER_DELETE_REQUEST, identifiers.size());
            deleteChunk(identifiers.subList(from, to));
        }
    }

    // 삭제 실패는 재시도 없이 로그로만 남기게 되어있어 수동 복구가 가능하도록 실패 키 전체를 기록한다
    private void deleteChunk(List<ObjectIdentifier> chunk) {
        try {
            DeleteObjectsResponse response = s3Client.deleteObjects(DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(Delete.builder().objects(chunk).build())
                    .build());

            if (!response.errors().isEmpty()) {
                List<String> failedKeys = response.errors().stream()
                        .map(S3Error::key)
                        .toList();
                log.error("S3 오브젝트 일부 삭제 실패: {}건, 첫 오류={}, 실패 키={}",
                        failedKeys.size(), response.errors().get(0), failedKeys);
            }
        } catch (SdkException e) {
            List<String> failedKeys = chunk.stream()
                    .map(ObjectIdentifier::key)
                    .toList();
            log.error("S3 오브젝트 일괄 삭제 실패: {}건, 실패 키={}", failedKeys.size(), failedKeys, e);
        }
    }
}
