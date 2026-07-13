package de.agiehl.mediauploader.security;

import de.agiehl.mediauploader.config.AppProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;

@Service
public class AuthenticationCookieService {

    public static final String COOKIE_NAME = "media-uploader-auth";
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final AppProperties.Security securityProperties;
    private final Clock clock;

    public AuthenticationCookieService(AppProperties properties, Clock clock) {
        this.securityProperties = properties.security();
        this.clock = clock;
    }

    public ResponseCookie createCookie() {
        return ResponseCookie.from(COOKIE_NAME, createToken())
                .httpOnly(true)
                .secure(securityProperties.secureCookie())
                .sameSite("Lax")
                .path("/")
                .maxAge(securityProperties.cookieLifetime())
                .build();
    }

    public boolean isValid(String token) {
        try {
            String[] parts = token.split("\\.", 2);
            return parts.length == 2
                    && !isExpired(parts[0])
                    && signaturesMatch(parts[0], parts[1]);
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private String createToken() {
        long expiresAt = Instant.now(clock).plus(securityProperties.cookieLifetime()).getEpochSecond();
        String payload = Long.toString(expiresAt);
        return payload + "." + sign(payload);
    }

    private boolean isExpired(String expiresAt) {
        return Instant.ofEpochSecond(Long.parseLong(expiresAt)).isBefore(Instant.now(clock));
    }

    private boolean signaturesMatch(String payload, String suppliedSignature) {
        return MessageDigest.isEqual(
                sign(payload).getBytes(StandardCharsets.US_ASCII),
                suppliedSignature.getBytes(StandardCharsets.US_ASCII));
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(
                    securityProperties.cookieSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Authentication cookie could not be signed", exception);
        }
    }
}
