package com.storix.domain.domains.report.repository;

public interface ReportedUserCountProjection {

    Long getReportedUserId();

    Long getReportCount();
}
