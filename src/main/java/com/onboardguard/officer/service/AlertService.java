package com.onboardguard.officer.service;

import com.onboardguard.officer.dto.AlertDetailDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AlertService {

    List<AlertDetailDto> getOpenAlertsQueue();

    AlertDetailDto acknowledgeAlert(Long alertId, Long officerId);

    AlertDetailDto claimNextAvailableAlert(Long officerId);

    void dismissAlert(Long alertId, Long officerId, String reason);

    Long convertToCase(Long alertId, Long officerId);
}
