package com.ecommerce.event;

import com.ecommerce.entity.ActionType;

public record TokenConfirmationEvent(Long userId, String email, ActionType actionType) {}
