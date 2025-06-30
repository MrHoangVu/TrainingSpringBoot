package com.example.socialnetwork.service;

import com.example.socialnetwork.dto.request.UpdateProfileRequest;
import com.example.socialnetwork.dto.response.UserProfileResponse;
import com.example.socialnetwork.entity.User;
import com.example.socialnetwork.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    // Helper method để lấy user đang đăng nhập
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public UserProfileResponse getCurrentUserProfile() {
        User user = getCurrentUser();
        return mapUserToProfileResponse(user);
    }

    public UserProfileResponse updateCurrentUserProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();

        user.setFullName(request.getFullName());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setAddress(request.getAddress());
        user.setOccupation(request.getOccupation());

        User updatedUser = userRepository.save(user);
        return mapUserToProfileResponse(updatedUser);
    }

    public String updateUserAvatar(MultipartFile file) {
        User user = getCurrentUser();
        String fileName = fileStorageService.storeFile(file);

        // Tạo URL để truy cập file
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(fileName)
                .toUriString();

        user.setAvatarUrl(fileDownloadUri);
        userRepository.save(user);

        return fileDownloadUri;
    }
    public UserProfileResponse getUserProfileById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
        return mapUserToProfileResponse(user);
    }

    // Helper method để map Entity sang DTO
    public UserProfileResponse mapUserToProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .dateOfBirth(user.getDateOfBirth())
                .address(user.getAddress())
                .occupation(user.getOccupation())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}