package com.ecommerce.dto;

import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@RegisterForReflection
public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String fullName
) {
    public User toUser(String hashedPassword) {
        User user = new User();
        user.email = email;
        user.passwordHash = hashedPassword;
        user.fullName = fullName;
        user.role = Role.CUSTOMER;
        return user;
    }
}