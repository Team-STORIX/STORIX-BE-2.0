package com.storix.domain.domains.notification.repository;

import com.storix.domain.domains.notification.domain.NotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {
}
