package com.datavalley.feedback.security;

import com.datavalley.feedback.entity.AdminUser;
import com.datavalley.feedback.repository.AdminUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;

    public AppUserDetailsService(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminUser admin = adminUserRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Admin user not found"));

        return new User(
            admin.getUsername(),
            admin.getPasswordHash(),
            admin.isActive(),
            true,
            true,
            true,
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }
}
