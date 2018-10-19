package com.comp30022.team_russia.assist.features.emergency.services;

import com.comp30022.team_russia.assist.base.ActionResult;

import java9.util.concurrent.CompletableFuture;

/**
 * Emergency alerting service.
 */
public interface EmergencyAlertService {
    CompletableFuture<ActionResult<Void>> sendEmergency();

    CompletableFuture<ActionResult<EmergencyAlertDto>> getEmergency(int eventId);

    CompletableFuture<ActionResult<Void>> handleEmergency(int eventId);
}