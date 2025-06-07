package com.michaelcao.bookstore_backend.service.impl;

import com.michaelcao.bookstore_backend.dto.address.AddressDTO;
import com.michaelcao.bookstore_backend.dto.user.ChangePasswordRequest;
import com.michaelcao.bookstore_backend.dto.user.UserProfileDTO;
import com.michaelcao.bookstore_backend.dto.user.UpdateAvatarRequest;
import com.michaelcao.bookstore_backend.dto.user.UpdateProfileRequest;
import com.michaelcao.bookstore_backend.entity.Address;
import com.michaelcao.bookstore_backend.entity.User;
import com.michaelcao.bookstore_backend.exception.ResourceNotFoundException;
import com.michaelcao.bookstore_backend.repository.UserRepository;
import com.michaelcao.bookstore_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder; // Import PasswordEncoder
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional
import com.michaelcao.bookstore_backend.dto.user.UpdateUserStatusRequest;
import com.michaelcao.bookstore_backend.dto.user.UserManagementDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils; // Import StringUtils
import org.springframework.security.core.GrantedAuthority;
import java.util.stream.Collectors;
import java.util.Set; // Import Set
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Inject PasswordEncoder    // --- Helper method: Map User entity sang UserProfileDTO ---
    private UserProfileDTO mapToUserProfileDTO(User user) {
        AddressDTO addressDTO = null;
        if (user.getDefaultAddress() != null) {
            Address address = user.getDefaultAddress();
            addressDTO = new AddressDTO(
                address.getStreet(),
                address.getCity(),
                address.getDistrict(),
                address.getCountry(),
                address.getPhone(),
                address.getRecipientName()
            );
        }
          return new UserProfileDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getPhone(),
                addressDTO
        );
    }
    private UserManagementDTO mapToUserManagementDTO(User user) {
        // Lấy danh sách tên Role từ Set<Role> hoặc Collection<GrantedAuthority>
        // Dùng getAuthorities() an toàn hơn vì nó đã được UserDetails cung cấp
        Set<String> roleNames = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority) // Lấy tên role (ví dụ: "ROLE_CUSTOMER")
                .collect(Collectors.toSet());

        return new UserManagementDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.isEnabled(), // Lấy trạng thái enabled
                roleNames,        // Lấy danh sách tên roles
                user.getAvatarUrl() // Thêm avatarUrl
        );
    }


    @Override
    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(Long userId) {
        log.debug("Fetching profile for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User profile not found for ID: {}", userId);
                    return new ResourceNotFoundException("User", "ID", userId);
                });
        return mapToUserProfileDTO(user);
    }

    @Override
    @Transactional // Cần transaction vì có cập nhật DB
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("Attempting to change password for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for password change with ID: {}", userId);
                    return new ResourceNotFoundException("User", "ID", userId);
                });

        // 1. Xác thực mật khẩu cũ
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Incorrect current password provided for user ID: {}", userId);
            throw new IllegalArgumentException("Incorrect current password.");
        }

        // 2. Kiểm tra mật khẩu mới có khác mật khẩu cũ không (tùy chọn)
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            log.warn("New password is the same as the current password for user ID: {}", userId);
            throw new IllegalArgumentException("New password cannot be the same as the current password.");
        }

        // 3. (Tùy chọn) Thêm validation phức tạp hơn cho mật khẩu mới nếu cần (ví dụ: độ dài, ký tự đặc biệt...)
        // (Validation cơ bản về độ dài đã được xử lý bằng @Size trên DTO)

        // 4. Hash mật khẩu mới và cập nhật
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed successfully for user ID: {}", userId);

        // TODO: Nên vô hiệu hóa các Refresh Token cũ của user này sau khi đổi mật khẩu
        // refreshTokenService.deleteByUserId(userId);
        // log.info("Old refresh tokens invalidated for user ID: {}", userId);
    }    // --- Implement updateProfile ---
    @Override
    @Transactional
    public UserProfileDTO updateProfile(Long userId, UpdateProfileRequest request) {
         log.info("Attempting to update profile for user ID: {}", userId);
         User user = userRepository.findById(userId)
                 .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));         // Cập nhật tên
         if (request.getName() != null && !request.getName().isBlank()) {
              user.setName(request.getName());
         }
         
         // Cập nhật số điện thoại
         if (request.getPhone() != null) {
              user.setPhone(request.getPhone());
         }
         
         // Cập nhật địa chỉ mặc định
         if (request.getDefaultAddress() != null) {
              AddressDTO addressDTO = request.getDefaultAddress();
              Address address = new Address(
                  addressDTO.getStreet(),
                  addressDTO.getCity(),
                  addressDTO.getDistrict(),
                  addressDTO.getCountry(),
                  addressDTO.getPhone(),
                  addressDTO.getRecipientName()
              );
              user.setDefaultAddress(address);
         }

         User updatedUser = userRepository.save(user);
         log.info("Profile updated successfully for user ID: {}", userId);
         return mapToUserProfileDTO(updatedUser);
    }

    // --- Admin Methods ---

    @Override
    @Transactional(readOnly = true)
    public Page<UserManagementDTO> getAllUsers(Pageable pageable, String keyword) {
        log.debug("Admin request: Fetching all users with pagination: {}, keyword: '{}'", pageable, keyword);
        Page<User> userPage;
        if (StringUtils.hasText(keyword)) {
            // Nếu có keyword, tìm kiếm theo tên hoặc email
            // Cần tạo phương thức tìm kiếm trong UserRepository
            userPage = userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword, pageable); // *** CẦN TẠO QUERY NÀY ***
        } else {
            // Nếu không có keyword, lấy tất cả user
            userPage = userRepository.findAll(pageable);
        }

        // Map sang DTO
        // Lưu ý: Query findAll và tìm kiếm ở trên chưa JOIN FETCH Roles.
        // Sẽ có N+1 query lấy Roles khi gọi mapToUserManagementDTO.
        // Cần tối ưu bằng JOIN FETCH hoặc EntityGraph nếu cần hiệu năng cao hơn.
        return userPage.map(this::mapToUserManagementDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public UserManagementDTO getUserByIdForAdmin(Long userId) {
        log.debug("Admin request: Fetching user details for ID: {}", userId);
        User user = userRepository.findById(userId) // Tạm thời chưa cần fetch roles ở đây
                .orElseThrow(() -> {
                    log.warn("Admin request failed: User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User", "ID", userId);
                });
        // Mapping sẽ gây N+1 query lấy roles nếu roles là LAZY
        return mapToUserManagementDTO(user);
    }

    @Override
    @Transactional
    public UserManagementDTO updateUserStatus(Long userId, UpdateUserStatusRequest request) {
        log.info("Admin request: Updating status for user ID: {} to enabled={}", userId, request.getEnabled());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Admin status update failed: User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User", "ID", userId);
                });

        // TODO: Có thể thêm kiểm tra không cho phép admin tự khóa chính mình?
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // User adminUser = (User) auth.getPrincipal();
        // if (user.getId().equals(adminUser.getId()) && !request.getEnabled()) {
        //    throw new OperationNotAllowedException("Admin cannot disable their own account.");
        // }

        user.setEnabled(request.getEnabled());
        User updatedUser = userRepository.save(user);
        log.info("User status updated successfully for user ID: {}", userId);

        // TODO: Nếu khóa user (enabled=false), nên xóa hết Refresh Token của họ
        // if (!updatedUser.isEnabled()) {
        //     refreshTokenService.deleteByUserId(userId);
        //     log.info("Refresh tokens invalidated for disabled user ID: {}", userId);
        // }

        // Mapping sẽ gây N+1 query lấy roles nếu roles là LAZY
        return mapToUserManagementDTO(updatedUser);
    }

    // --- Implement updateUserRoles nếu cần ---
    /*
    @Override
    @Transactional
    public UserManagementDTO updateUserRoles(Long userId, UpdateUserRolesRequest request) {
        log.info("Admin request: Updating roles for user ID: {} to {}", userId, request.getRoleNames());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));

        // Tìm các đối tượng Role từ tên role trong request
        Set<Role> newRoles = request.getRoleNames().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName)))
                .collect(Collectors.toSet());

        user.setRoles(newRoles); // Ghi đè Set roles cũ
        User updatedUser = userRepository.save(user);
        log.info("User roles updated successfully for user ID: {}", userId);
        return mapToUserManagementDTO(updatedUser); // mapToUserManagementDTO đã lấy roles từ user
    }
   */

    @Override
    @Transactional
    public UserProfileDTO updateAvatar(Long userId, UpdateAvatarRequest request) {
        log.info("Attempting to update avatar for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for avatar update with ID: {}", userId);
                    return new ResourceNotFoundException("User", "ID", userId);
                });

        // Cập nhật avatar URL
        user.setAvatarUrl(request.getAvatarUrl());
        User updatedUser = userRepository.save(user);
        log.info("Avatar updated successfully for user ID: {}", userId);
        return mapToUserProfileDTO(updatedUser);
    }
}