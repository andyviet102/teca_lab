package vn.teca.digitization.migrate.conveter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import vn.teca.digitization.migrate.service.AuthService;

import java.util.Collection;

@Component
@Slf4j
public class JwtAuthorityConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Autowired
    private AuthService authService;

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        return authService.findAuthorities(jwt);
    }
}
