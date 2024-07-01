package vn.teca.digitization.migrate.config.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import vn.teca.digitization.migrate.model.Auditor;

import java.util.Objects;
import java.util.Optional;

public class AuditorProvider implements AuditorAware<Auditor> {

    @Override
    public Optional<Auditor> getCurrentAuditor() {
        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(Objects::nonNull)
                .filter(authentication -> authentication.getPrincipal() instanceof Jwt)
                .map(authentication -> buildAuditor((Jwt) authentication.getPrincipal()));
    }

    private static Auditor buildAuditor(Jwt jwt) {
        return Auditor.builder()
                .id(jwt.getClaimAsString("sub"))
                .username(jwt.getClaimAsString("preferred_username"))
                .build();
    }
}