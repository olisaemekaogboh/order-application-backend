package com.inkfront.logisticsApplication.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.security.Principal;
import java.util.Collection;

@Getter
@AllArgsConstructor
public class AuthenticatedUser implements Principal {

    private final String id;
    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;

    @Override
    public String getName() {
        return email;
    }
}