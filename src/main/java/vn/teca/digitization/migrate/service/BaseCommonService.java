package vn.teca.digitization.migrate.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class BaseCommonService {

    protected String findUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return ((Jwt) authentication.getPrincipal()).getClaimAsString("preferred_username");
        }
        return "System";
    }

    protected String findKcId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return ((Jwt) authentication.getPrincipal()).getClaimAsString("sub");
        }
        return "System";
    }

    protected String getErrorDetail(Exception e) {

        String stackTrace = ExceptionUtils.getStackTrace(e);

        Pattern pattern = Pattern.compile("(at (.*?)\\))|(\\.\\.\\. (.*?) more)");

        Matcher matcher = pattern.matcher(stackTrace);

        while (matcher.find()) {
            String matchedString = matcher.group();

            if (!matchedString.contains("vn.teca.vneid.organization.") || !matchedString.contains(".java")) {
                stackTrace = stackTrace.replace(matchedString, "");
            }
        }

        stackTrace = stackTrace
                .trim()
                .replaceAll("(?m)^[ \t]*\r?\n", "");

        if (stackTrace.length() > 4000) {
            stackTrace = stackTrace.substring(0, 4000);
        }

        return stackTrace;
    }
}
