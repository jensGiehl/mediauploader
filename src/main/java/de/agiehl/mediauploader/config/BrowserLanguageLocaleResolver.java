package de.agiehl.mediauploader.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

public class BrowserLanguageLocaleResolver implements LocaleResolver {

    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        return request.getLocales().asIterator()
                .next()
                .getLanguage()
                .equals(Locale.GERMAN.getLanguage())
                ? Locale.GERMAN
                : DEFAULT_LOCALE;
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        throw new UnsupportedOperationException("The locale is determined by the Accept-Language header.");
    }
}
