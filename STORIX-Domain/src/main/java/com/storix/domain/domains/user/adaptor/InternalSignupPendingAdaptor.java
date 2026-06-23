package com.storix.domain.domains.user.adaptor;

import com.storix.domain.domains.user.domain.AdminSignupPending;
import com.storix.domain.domains.user.domain.DeveloperSignupPending;
import com.storix.domain.domains.user.exception.admin.AdminSignupPendingNotFoundException;
import com.storix.domain.domains.user.exception.developer.DeveloperSignupPendingNotFoundException;
import com.storix.domain.domains.user.repository.AdminSignupPendingRepository;
import com.storix.domain.domains.user.repository.DeveloperSignupPendingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InternalSignupPendingAdaptor {

    private final DeveloperSignupPendingRepository developerRepository;
    private final AdminSignupPendingRepository adminRepository;

    // 개발자
    public void save(DeveloperSignupPending pending) {
        developerRepository.save(pending);
    }

    public DeveloperSignupPending getDeveloperPending(String pendingId) {
        return developerRepository.findById(pendingId)
                .orElseThrow(() -> DeveloperSignupPendingNotFoundException.EXCEPTION);
    }

    public boolean existsDeveloperPending(String pendingId) {
        return developerRepository.existsById(pendingId);
    }

    public void deleteDeveloperPending(String pendingId) {
        developerRepository.deleteById(pendingId);
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
