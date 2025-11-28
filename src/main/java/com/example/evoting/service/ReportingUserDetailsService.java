package com.example.evoting.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.evoting.repository.ReportingRepository;

@Service
public class ReportingUserDetailsService implements UserDetailsService {

    private final ReportingRepository repository;

    public ReportingUserDetailsService(ReportingRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // First check admin table
        Map<String, Object> admin = repository.findAdminByUsername(username);
        if (admin != null) {
            String pw = (String) admin.get("password");
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            return new User(username, pw, authorities);
        }

        // Then check voter by NID
        Map<String, Object> voter = repository.findVoterByNid(username);
        if (voter == null) {
            // Fallback: check by Voter ID (e.g. "1", "2")
            voter = repository.findVoterByIdString(username);
        }

        if (voter != null) {
            String pw = (String) voter.get("password");
            if (pw == null) {
                pw = "VOTER_SECRET";
            }
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            return new User(username, pw, authorities);
        }

        throw new UsernameNotFoundException("User not found: " + username);
    }
}
