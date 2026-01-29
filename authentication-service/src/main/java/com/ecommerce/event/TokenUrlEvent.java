package com.ecommerce.event;

import com.ecommerce.entity.ActionType;

public record TokenUrlEvent(Long userId, String email, ActionType actionType, String url) {}
