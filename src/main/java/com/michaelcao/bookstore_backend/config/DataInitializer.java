package com.michaelcao.bookstore_backend.config;


import com.michaelcao.bookstore_backend.entity.Role;
import com.michaelcao.bookstore_backend.entity.User;
import com.michaelcao.bookstore_backend.repository.RoleRepository;
import com.michaelcao.bookstore_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors; // Import Stream


@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {


    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    private final String adminDefaultEmail = "admin@atomik.com";
    private final String adminDefaultPassword = "Admin123";
    private final String adminDefaultName = "Atomik Administrator";
    private final String adminDefaultAvatarUrl = "/default-admin-avatar.png";

    // Additional test user
    private final String testUserEmail = "caomanhtruong1609@gmail.com";
    private final String testUserPassword = "Hihihi123";
    private final String testUserName = "Cao Manh Truong";
    private final String testUserAvatarUrl = "/default-avatar.png";




    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Bắt đầu quá trình khởi tạo dữ liệu ứng dụng...");


        log.info("Đang khởi tạo các vai trò (Roles)...");
        Role customerRole = createRoleIfNotFound(ROLE_CUSTOMER); // Lấy hoặc tạo ROLE_CUSTOMER
        Role adminRole = createRoleIfNotFound(ROLE_ADMIN);     // Lấy hoặc tạo ROLE_ADMIN
        log.info("Hoàn tất khởi tạo vai trò.");        log.info("Đang khởi tạo tài khoản Admin mặc định...");
        // Truyền cả hai vai trò vào
        createAdminUserIfNotFound(adminRole, customerRole);
        log.info("Hoàn tất khởi tạo tài khoản Admin mặc định.");

        log.info("Đang khởi tạo tài khoản test user...");
        createTestUserIfNotFound(customerRole);
        log.info("Hoàn tất khởi tạo tài khoản test user.");

        log.info("Quá trình khởi tạo dữ liệu ứng dụng đã hoàn tất.");
    }


    private Role createRoleIfNotFound(String roleName) {
        Optional<Role> roleOpt = roleRepository.findByName(roleName);
        if (roleOpt.isEmpty()) {
            Role newRole = Role.builder().name(roleName).build();
            Role savedRole = roleRepository.save(newRole);
            log.info("Đã tạo vai trò mới: {} với ID: {}", roleName, savedRole.getId());
            return savedRole;
        } else {
            log.debug("Vai trò {} đã tồn tại với ID: {}", roleName, roleOpt.get().getId());
            return roleOpt.get();
        }
    }


    /**
     * Tạo tài khoản Admin mặc định nếu chưa tồn tại.
     * Gán cả vai trò ADMIN và CUSTOMER cho tài khoản này.
     *
     * @param adminRoleEntity Đối tượng Role đại diện cho "ROLE_ADMIN".
     * @param customerRoleEntity Đối tượng Role đại diện cho "ROLE_CUSTOMER".
     */
    private void createAdminUserIfNotFound(Role adminRoleEntity, Role customerRoleEntity) {
        if (!userRepository.existsByEmail(adminDefaultEmail)) {
            Set<Role> adminRolesSet = new HashSet<>();
            adminRolesSet.add(adminRoleEntity);    // Thêm ROLE_ADMIN
            adminRolesSet.add(customerRoleEntity); // << THÊM ROLE_CUSTOMER


            User adminUser = User.builder()
                    .name(adminDefaultName)
                    .email(adminDefaultEmail)
                    .password(passwordEncoder.encode(adminDefaultPassword))
                    .roles(adminRolesSet) // Gán Set chứa cả hai vai trò
                    .enabled(true)
                    .avatarUrl(adminDefaultAvatarUrl)
                    .build();


            userRepository.save(adminUser);
            String assignedRoles = adminRolesSet.stream()
                    .map(role -> String.format("%s (ID: %d)", role.getName(), role.getId()))
                    .collect(Collectors.joining(", "));
            log.info("Đã tạo tài khoản Admin mặc định: email='{}', tên='{}', và gán các vai trò: [{}]",
                    adminDefaultEmail, adminDefaultName, assignedRoles);
        } else {
            log.debug("Tài khoản Admin mặc định với email '{}' đã tồn tại.", adminDefaultEmail);


            // Kiểm tra và đảm bảo user admin có cả ROLE_ADMIN và ROLE_CUSTOMER
            userRepository.findByEmail(adminDefaultEmail).ifPresent(existingAdmin -> {
                boolean needsUpdate = false;
                // Kiểm tra ROLE_ADMIN
                boolean hasAdminRole = existingAdmin.getRoles().stream()
                        .anyMatch(role -> adminRoleEntity.getName().equals(role.getName()));
                if (!hasAdminRole) {
                    existingAdmin.getRoles().add(adminRoleEntity);
                    log.info("Đã thêm vai trò {} (ID: {}) cho người dùng Admin hiện tại: {}",
                            adminRoleEntity.getName(), adminRoleEntity.getId(), adminDefaultEmail);
                    needsUpdate = true;
                }


                // Kiểm tra ROLE_CUSTOMER
                boolean hasCustomerRole = existingAdmin.getRoles().stream()
                        .anyMatch(role -> customerRoleEntity.getName().equals(role.getName()));
                if (!hasCustomerRole) {
                    existingAdmin.getRoles().add(customerRoleEntity);
                    log.info("Đã thêm vai trò {} (ID: {}) cho người dùng Admin hiện tại: {}",
                            customerRoleEntity.getName(), customerRoleEntity.getId(), adminDefaultEmail);
                    needsUpdate = true;
                }


                if (needsUpdate) {
                    userRepository.save(existingAdmin);
                } else {
                    String currentRoles = existingAdmin.getRoles().stream()
                            .map(Role::getName)
                            .collect(Collectors.joining(", "));
                    log.debug("Người dùng Admin '{}' đã có các vai trò cần thiết: [{}].", adminDefaultEmail, currentRoles);
                }            });
        }
    }

    /**
     * Tạo tài khoản test user nếu chưa tồn tại.
     * Gán vai trò CUSTOMER cho tài khoản này.
     *
     * @param customerRoleEntity Đối tượng Role đại diện cho "ROLE_CUSTOMER".
     */
    private void createTestUserIfNotFound(Role customerRoleEntity) {
        if (!userRepository.existsByEmail(testUserEmail)) {
            Set<Role> customerRolesSet = new HashSet<>();
            customerRolesSet.add(customerRoleEntity); // Chỉ thêm ROLE_CUSTOMER

            User testUser = User.builder()
                    .name(testUserName)
                    .email(testUserEmail)
                    .password(passwordEncoder.encode(testUserPassword))
                    .roles(customerRolesSet)
                    .enabled(true) // Đặt enabled=true để có thể đăng nhập ngay
                    .avatarUrl(testUserAvatarUrl)
                    .build();

            userRepository.save(testUser);
            log.info("Đã tạo tài khoản test user: email='{}', tên='{}', với vai trò ROLE_CUSTOMER",
                    testUserEmail, testUserName);
        } else {
            log.debug("Tài khoản test user với email '{}' đã tồn tại.", testUserEmail);
        }
    }
}

