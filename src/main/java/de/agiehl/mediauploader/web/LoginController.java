package de.agiehl.mediauploader.web;

import de.agiehl.mediauploader.security.AuthenticationCookieService;
import de.agiehl.mediauploader.security.PasswordVerifier;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final PasswordVerifier passwordVerifier;
    private final AuthenticationCookieService authenticationCookieService;

    public LoginController(PasswordVerifier passwordVerifier, AuthenticationCookieService authenticationCookieService) {
        this.passwordVerifier = passwordVerifier;
        this.authenticationCookieService = authenticationCookieService;
    }

    @GetMapping("/login")
    String login() {
        return "login";
    }

    @PostMapping("/login")
    String login(@RequestParam String password, HttpServletResponse response, Model model) {
        if (!passwordVerifier.matches(password)) {
            model.addAttribute("invalidPassword", true);
            return "login";
        }

        response.addHeader(HttpHeaders.SET_COOKIE, authenticationCookieService.createCookie().toString());
        return "redirect:/";
    }
}
