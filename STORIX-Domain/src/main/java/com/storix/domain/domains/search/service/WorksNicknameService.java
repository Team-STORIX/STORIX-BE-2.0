package com.storix.domain.domains.search.service;

import com.storix.domain.domains.search.dto.WorksNicknameBulkResponse;
import com.storix.domain.domains.search.dto.WorksNicknameEntry;
import com.storix.domain.domains.search.dto.WorksNicknameResponse;
import com.storix.domain.domains.search.exception.DuplicateWorksNicknameException;
import com.storix.domain.domains.search.exception.InvalidWorksNicknameException;
import com.storix.domain.domains.works.adaptor.WorksAdaptor;
import com.storix.domain.domains.works.domain.WorksNickname;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WorksNicknameService {

    private static final int MAX_NICKNAME_LENGTH = 100;

    private final WorksAdaptor worksAdaptor;

    @Transactional(readOnly = true)
    public List<WorksNicknameResponse> findNicknames(Long worksId) {
        worksAdaptor.findById(worksId);
        return worksAdaptor.findNicknames(worksId).stream()
                .map(WorksNicknameResponse::from)
                .toList();
    }

    public WorksNicknameResponse addNickname(Long worksId, String nickname) {
        worksAdaptor.findById(worksId);

        WorksNickname worksNickname = new WorksNickname(worksId, nickname.trim());
        if (worksNickname.getNormalized().isEmpty()) throw InvalidWorksNicknameException.EXCEPTION;
        if (worksAdaptor.existsNickname(worksId, worksNickname.getNormalized())) {
            throw DuplicateWorksNicknameException.EXCEPTION;
        }

        try {
            return WorksNicknameResponse.from(worksAdaptor.saveNicknameAndFlush(worksNickname));
        } catch (DataIntegrityViolationException e) {
            throw DuplicateWorksNicknameException.EXCEPTION;
        }
    }

    public WorksNicknameBulkResponse addNicknames(List<WorksNicknameEntry> entries) {
        List<Long> worksIds = entries.stream().map(WorksNicknameEntry::worksId).filter(Objects::nonNull).distinct().toList();
        Set<Long> existingWorksIds = new HashSet<>(worksAdaptor.findExistingWorksIds(worksIds));
        Set<String> registered = worksAdaptor.findNicknamesByWorksIds(existingWorksIds).stream()
                .map(nickname -> nickname.getWorksId() + ":" + nickname.getNormalized())
                .collect(Collectors.toCollection(HashSet::new));

        List<WorksNickname> newNicknames = new ArrayList<>();
        int duplicateCount = 0;
        int unknownWorksCount = 0;
        int invalidCount = 0;

        for (WorksNicknameEntry entry : entries) {
            if (entry.worksId() == null || entry.nickname() == null || entry.nickname().isBlank() || entry.nickname().length() > MAX_NICKNAME_LENGTH) {
                invalidCount++;
                continue;
            }
            WorksNickname worksNickname = new WorksNickname(entry.worksId(), entry.nickname().trim());
            if (worksNickname.getNormalized().isEmpty()) {
                invalidCount++;
            } else if (!existingWorksIds.contains(entry.worksId())) {
                unknownWorksCount++;
            } else if (!registered.add(entry.worksId() + ":" + worksNickname.getNormalized())) {
                duplicateCount++;
            } else {
                newNicknames.add(worksNickname);
            }
        }

        worksAdaptor.saveNicknames(newNicknames);
        return new WorksNicknameBulkResponse(newNicknames.size(), duplicateCount, unknownWorksCount, invalidCount);
    }

    public void deleteNickname(Long worksId, Long nicknameId) {
        worksAdaptor.deleteNickname(worksAdaptor.findNickname(nicknameId, worksId));
    }
}
