package vn.teca.digitization.migrate.conveter;

import com.nimbusds.jwt.JWTParser;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public class JwtClaimIssuerConverter implements Converter<HttpServletRequest, String> {

    private final BearerTokenResolver resolver = new DefaultBearerTokenResolver();

    @Override
    public String convert(HttpServletRequest request) {
        try {
            return Optional.ofNullable(JWTParser.parse(resolver.resolve(request))
                            .getJWTClaimsSet()
                            .getStringClaim(JwtClaimNames.ISS))
                    .orElseThrow(() -> new InvalidBearerTokenException("Missing issuer"));
        } catch (Exception ex) {
            throw new InvalidBearerTokenException(ex.getMessage(), ex);
        }
    }
}
