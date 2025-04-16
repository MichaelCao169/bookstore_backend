package com.michaelcao.bookstore_backend.config;


import com.michaelcao.bookstore_backend.entity.Role;
import com.michaelcao.bookstore_backend.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Initializing application roles...");
        createRoleIfNotFound(ROLE_CUSTOMER);
        createRoleIfNotFound(ROLE_ADMIN);
        log.info("Role initialization complete.");
    }

    private void createRoleIfNotFound(String roleName) {
        Optional<Role> roleOpt = roleRepository.findByName(roleName);
        if (roleOpt.isEmpty()) {
            Role newRole = Role.builder().name(roleName).build();
            roleRepository.save(newRole);
            log.info("Created role: {}", roleName);
        } else {
            log.debug("Role already exists: {}", roleName);
        }
    }
}