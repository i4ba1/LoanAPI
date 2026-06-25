package com.loan.loanapi.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loanapi.jwt")
public record JwtProperties(String secret, long expirationMinutes) {
}
