package com.ecommerce.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_action_tokens")
public class UserActionToken extends TokenEntity {

    @Enumerated(EnumType.STRING)
    public ActionType actionType;
}
