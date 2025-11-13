package com.samilyak.gateway.config;

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
public class JwtRoleConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        String role = jwt.getClaim("role");
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (role != null && !role.isBlank()) {
            authorities.add(new SimpleGrantedAuthority(role));
        }

        return Mono.just(new JwtAuthenticationToken(jwt, authorities, jwt.getSubject()));
    }
}