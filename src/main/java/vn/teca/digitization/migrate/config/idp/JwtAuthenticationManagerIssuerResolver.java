package vn.teca.digitization.migrate.config.idp;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.stereotype.Component;
import vn.teca.digitization.migrate.conveter.JwtAuthorityConverter;
import vn.teca.digitization.migrate.conveter.JwtClaimIssuerConverter;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class JwtAuthenticationManagerIssuerResolver implements AuthenticationManagerResolver<HttpServletRequest> {
    @Autowired
    private JwtAuthorityConverter jwtAuthorityConverter;

    @Value("${idp.base-url}")
    private String baseUrl;

    @Value("${idp.base-url-https}")
    private String baseUrlHttps;

    @Value("${idp.issuer-uri}")
    private String issuerUri;

    @Value("${idp.jwk-set-uri}")
    private String jwkSetUri;

    private final JwtClaimIssuerConverter issuerConverter = new JwtClaimIssuerConverter();

    private ConcurrentHashMap<String, AuthenticationManager> authenticationManagers = new ConcurrentHashMap<>();

    @Override
    public AuthenticationManager resolve(HttpServletRequest request) {
        String issuer = issuerConverter.convert(request);
        if (StringUtils.isNotBlank(issuer) && issuer.startsWith(baseUrlHttps)) {
            issuer = issuer.replaceFirst(baseUrlHttps, baseUrl);
        }
        if (!issuerUri.equals(issuer)) {
            throw new InvalidBearerTokenException(String.format("Untrusted issuer %s", issuer));
        }
        return authenticationManagers.computeIfAbsent(
                issuer,
                (iss) -> {
                    log.info("Creating AuthenticationManager for unregistered issuer {}", iss);
                    return jwtAuthProvider()::authenticate;
                });
    }

    private JwtAuthenticationProvider jwtAuthProvider() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        JwtAuthenticationProvider authenticationProvider = new JwtAuthenticationProvider(jwtDecoder);
        authenticationProvider.setJwtAuthenticationConverter(jwtAuthenticationConverter());
        return authenticationProvider;
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtAuthorityConverter);
        return jwtAuthenticationConverter;
    }

}
