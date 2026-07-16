package com.storix.domain.domains.notification.metrics;

import com.storix.domain.domains.notification.adaptor.AdminNotificationAdaptor;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class AdminNotificationMetrics {

    public AdminNotificationMetrics(MeterRegistry meterRegistry, AdminNotificationAdaptor adminNotificationAdaptor) {
        Gauge.builder("admin.notification.sending.inflight", adminNotificationAdaptor, AdminNotificationAdaptor::countSending)
                .description("현재 SENDING 상태인 운영자 알림 수")
                .register(meterRegistry);
    }
}
