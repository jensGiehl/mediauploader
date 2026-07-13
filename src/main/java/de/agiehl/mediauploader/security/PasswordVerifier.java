package de.agiehl.mediauploader.security;

import de.agiehl.mediauploader.config.AppProperties;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class PasswordVerifier {

    private final byte[] expectedPassword;

    public PasswordVerifier(AppProperties properties) {
        this.expectedPassword = properties.security().password().getBytes(StandardCharsets.UTF_8);
    }

    public boolean matches(String suppliedPassword) {
        return MessageDigest.isEqual(
                expectedPassword,
                suppliedPassword.getBytes(StandardCharsets.UTF_8));
    }
}
