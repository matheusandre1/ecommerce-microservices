package com.ecommerce.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken extends TokenEntity {

    public boolean revoked = false;
}
