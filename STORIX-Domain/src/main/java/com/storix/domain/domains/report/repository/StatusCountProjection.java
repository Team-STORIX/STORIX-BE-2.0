package com.storix.domain.domains.report.repository;

import com.storix.domain.domains.report.domain.ReportStatus;

public interface StatusCountProjection {
    ReportStatus getStatus();
    long getCount();
}
