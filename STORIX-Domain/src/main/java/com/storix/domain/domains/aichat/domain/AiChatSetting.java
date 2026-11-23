package com.storix.domain.domains.aichat.domain;

import com.storix.common.model.BaseTimeEntity;
import com.storix.domain.domains.aichat.converter.AiChatWorksTypesConverter;
import com.storix.domain.domains.aichat.exception.AiChatUnsupportedWorksTypeException;
import com.storix.domain.domains.works.domain.WorksType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.EnumSet;
import java.util.Set;

@Entity
@Getter
@Table(
        name = "ai_chat_settings",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ai_chat_setting_user", columnNames = {"user_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiChatSetting extends BaseTimeEntity {

    private static final Set<WorksType> SELECTABLE_WORKS_TYPES =
            EnumSet.of(WorksType.WEBTOON, WorksType.WEBNOVEL, WorksType.BOOK);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ai_chat_setting_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 사용자가 켠 값. 실제로 19금을 보여 줄지는 매 질문마다 성인 인증과 같이 본다
    @Column(name = "adult_enabled", nullable = false)
    private boolean adultEnabled;

    // 비어 있으면 상관없음
    @Convert(converter = AiChatWorksTypesConverter.class)
    @Column(name = "works_types", nullable = false, length = 50)
    private Set<WorksType> worksTypes;

    // null 이면 온보딩을 아직 안 봤다
    @Column(name = "onboarding_version")
    private Integer onboardingVersion;

    private AiChatSetting(Long userId) {
        this.userId = userId;
        this.adultEnabled = false;
        this.worksTypes = EnumSet.noneOf(WorksType.class);
    }

    public static AiChatSetting defaultOf(Long userId) {
        return new AiChatSetting(userId);
    }

    // 챗봇 설정에서 고를 수 있는 작품 유형. AI 서버 카탈로그에도 같은 값을 내려준다
    public static Set<WorksType> selectableWorksTypes() {
        return EnumSet.copyOf(SELECTABLE_WORKS_TYPES);
    }

    public void update(boolean adultEnabled, Set<WorksType> worksTypes) {
        if (!SELECTABLE_WORKS_TYPES.containsAll(worksTypes)) {
            throw AiChatUnsupportedWorksTypeException.EXCEPTION;
        }
        this.adultEnabled = adultEnabled;
        this.worksTypes = worksTypes.isEmpty() ? EnumSet.noneOf(WorksType.class) : EnumSet.copyOf(worksTypes);
    }

    public void completeOnboarding(int version) {
        this.onboardingVersion = version;
    }

    public boolean hasSeenOnboarding(int currentVersion) {
        return onboardingVersion != null && onboardingVersion >= currentVersion;
    }
}
