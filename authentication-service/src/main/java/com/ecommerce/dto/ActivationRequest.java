package com.ecommerce.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;

@RegisterForReflection
public record ActivationRequest(@NotBlank String token) {}
