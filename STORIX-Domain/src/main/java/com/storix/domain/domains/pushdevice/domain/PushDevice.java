package com.storix.domain.domains.pushdevice.domain;

import com.storix.common.model.BaseTimeEntity;
import com.storix.domain.domains.pushdevice.dto.SyncDeviceCommand;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "push_devices",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_push_device_installation",
                columnNames = {"user_id", "installation_id"}
        ),
        indexes = {
                @Index(name = "idx_push_device_user_active", columnList = "user_id, is_active"),
                @Index(name = "idx_push_device_fcm_token", columnList = "fcm_token")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushDevice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "push_device_id")
    private Long id;

    // 유저 식별자
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 기기 식별자
    @Column(name = "installation_id", nullable = false, length = 64)
    private String installationId;

    // FCM 토큰
    @Column(name = "fcm_token", nullable = false, length = 512)
    private String fcmToken;

    // 기기 정보
    @Enumerated(EnumType.STRING)
    @Column(name = "os_platform", nullable = false, length = 16)
    private OSPlatform osPlatform;

    @Column(name = "app_version", nullable = false, length = 32)
    private String appVersion;

    @Column(name = "os_version", nullable = false, length = 32)
    private String osVersion;

    @Column(name = "device_model", nullable = false, length = 64)
    private String deviceModel;

    // 기기 알림 권한
    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    // FCM 발송에 성공한 마지막 시각
    @Column(name = "last_success_at")
    private LocalDateTime lastSuccessAt;


    /** 생성자 메서드 */
    @Builder
    public PushDevice(Long userId,
                      String installationId,
                      String fcmToken,
                      OSPlatform osPlatform,
                      String appVersion,
                      String osVersion,
                      String deviceModel) {
        this.userId = userId;
        this.installationId = installationId;
        this.fcmToken = fcmToken;
        this.osPlatform = osPlatform;
        this.appVersion = appVersion;
        this.osVersion = osVersion;
        this.deviceModel = deviceModel;
        this.isActive = true;
    }

    public static PushDevice from(Long userId, SyncDeviceCommand cmd) {
        return PushDevice.builder()
                .userId(userId)
                .installationId(cmd.installationId())
                .fcmToken(cmd.fcmToken())
                .osPlatform(cmd.osPlatform())
                .appVersion(cmd.appVersion())
                .osVersion(cmd.osVersion())
                .deviceModel(cmd.deviceModel())
                .build();
    }


    /** 비즈니스 메서드 */
    // 1. 디바이스 정보 + FCM 토큰 갱신
    public void refresh(SyncDeviceCommand cmd) {
        this.fcmToken = cmd.fcmToken();
        this.appVersion = cmd.appVersion();
        this.osVersion = cmd.osVersion();
        this.isActive = true;
    }

    // 2. FCM 토큰 갱신
    public void refreshFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
        this.isActive = true;
    }
}
