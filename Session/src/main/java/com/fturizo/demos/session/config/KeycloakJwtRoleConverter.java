package com.fturizo.demos.session.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;

public class KeycloakJwtRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt source) {
        Map<String, Object> realmAccess =  source.getClaim("realm_access");
        if(realmAccess != null && realmAccess.containsKey("roles")){
            List<String> realmRoles = (List<String>)realmAccess.get("roles");
            if(realmRoles != null){
                return realmRoles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }
}
