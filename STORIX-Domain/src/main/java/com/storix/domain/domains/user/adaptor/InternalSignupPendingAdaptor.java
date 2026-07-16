package com.storix.domain.domains.user.adaptor;

import com.storix.domain.domains.user.domain.AdminSignupPending;
import com.storix.domain.domains.user.domain.TesterSignupPending;
import com.storix.domain.domains.user.exception.admin.AdminSignupPendingNotFoundException;
import com.storix.domain.domains.user.exception.tester.TesterSignupPendingNotFoundException;
import com.storix.domain.domains.user.repository.AdminSignupPendingRepository;
import com.storix.domain.domains.user.repository.TesterSignupPendingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InternalSignupPendingAdaptor {

    private final TesterSignupPendingRepository testerRepository;
    private final AdminSignupPendingRepository adminRepository;

    // 테스터
    public void save(TesterSignupPending pending) {
        testerRepository.save(pending);
    }

    public TesterSignupPending getTesterPending(String pendingId) {
        return testerRepository.findById(pendingId)
                .orElseThrow(() -> TesterSignupPendingNotFoundException.EXCEPTION);
    }

    public boolean existsTesterPending(String pendingId) {
        return testerRepository.existsById(pendingId);
    }

    public void deleteTesterPending(String pendingId) {
        testerRepository.deleteById(pendingId);
    }

    // 관리자
    public void save(AdminSignupPending pending) {
        adminRepository.save(pending);
    }

    public AdminSignupPending getAdminPending(String pendingId) {
        return adminRepository.findById(pendingId)
                .orElseThrow(() -> AdminSignupPendingNotFoundException.EXCEPTION);
    }

    public void deleteAdminPending(String pendingId) {
        adminRepository.deleteById(pendingId);
    }
}
