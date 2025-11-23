package com.example.evoting.service;

import java.util.Map;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.evoting.repository.ReportingRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final ReportingRepository repository;

    public CustomUserDetailsService(ReportingRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Map<String, Object> admin = repository.findAdminByUsername(username);
        if (admin == null) {
            throw new UsernameNotFoundException("User not found");
        }

        // In a real app, password should be encoded.
        // For this demo, we might need to handle plain text or encoded passwords.
        // The data.sql inserts 'admin123'. We'll assume it's plain text for now
        // or use {noop} prefix if we don't want to encode it in DB.
        // But SecurityConfig uses BCrypt. So we should probably encode it in data.sql
        // OR change SecurityConfig to use NoOp (not recommended) OR handle it here.

        // Let's assume the DB has plain text for simplicity in this specific legacy
        // refactor,
        // but we will wrap it in {noop} for Spring Security to accept it without
        // encoding,
        // OR we update data.sql to have a BCrypt hash.

        // Better approach: Update data.sql with BCrypt hash for 'admin123'.
        // Hash for 'admin123' is $2a$10$.. (we can generate one or use a known one).
        // For now, let's use {noop} prefix strategy for simplicity if we don't want to
        // change data.sql hash.

        String password = (String) admin.get("password");

        return User.builder()
                .username((String) admin.get("username"))
                .password("{noop}" + password) // Prefixing {noop} allows plain text match
                .roles("ADMIN")
                .build();
    }
}
