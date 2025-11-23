package com.example.evoting.config;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.evoting.repository.ReportingRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AuthSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(AuthSuccessHandler.class);
    private final ReportingRepository repository;

    public AuthSuccessHandler(ReportingRepository repository) {
        this.repository = repository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        HttpSession session = request.getSession(true);
        // Respect explicit login type from the login form if provided
        String loginType = request.getParameter("loginType");
        if (loginType == null) loginType = "voter";

        try {
            if ("admin".equalsIgnoreCase(loginType)) {
                Map<String, Object> admin = repository.findAdminByUsername(username);
                if (admin != null) {
                    session.setAttribute("isAdmin", true);
                    session.setAttribute("user", username);
                    logger.info("Admin logged in: {}", username);
                    response.sendRedirect("/admin");
                    return;
                }
                // fallback to voter if admin not found
            }

            // Treat as voter
            Map<String, Object> voter = repository.findVoterByNid(username);
            if (voter != null) {
                Object vid = voter.get("voter_id");
                session.setAttribute("voterId", vid);
                session.setAttribute("user", voter.get("full_name"));
                session.setAttribute("isAdmin", false);
                logger.info("Voter logged in: {} (id={})", username, vid);
                response.sendRedirect("/vote");
                return;
            }

            // fallback: if admin exists even though loginType was voter
            Map<String, Object> admin2 = repository.findAdminByUsername(username);
            if (admin2 != null) {
                session.setAttribute("isAdmin", true);
                session.setAttribute("user", username);
                logger.info("Admin logged in (fallback): {}", username);
                response.sendRedirect("/admin");
                return;
            }

        } catch (Exception e) {
            logger.error("Error during post-auth processing for user {}: {}", username, e.getMessage());
        }

        // default
        session.setAttribute("user", username);
        response.sendRedirect("/");
    }
}
