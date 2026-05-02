package com.onboardguard.shared.common.events;

import java.time.Instant;

public record SlaBreachNotificationEvent(
        int newBreachedAlerts,
        int newBreachedCases,
        Instant timestamp
) {}