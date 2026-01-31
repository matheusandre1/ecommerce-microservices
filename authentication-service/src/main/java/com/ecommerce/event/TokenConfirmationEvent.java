package com.ecommerce.event;

import com.ecommerce.entity.ActionType;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record TokenConfirmationEvent(Long userId, String email, ActionType actionType) {}
