package com.example.socialnetwork.service;

import com.example.socialnetwork.dto.response.FriendshipStatusResponse;
import com.example.socialnetwork.dto.response.UserProfileResponse;
import com.example.socialnetwork.entity.Friendship;
import com.example.socialnetwork.entity.FriendshipId;
import com.example.socialnetwork.entity.FriendshipStatus;
import com.example.socialnetwork.entity.User;
import com.example.socialnetwork.repository.FriendshipRepository;
import com.example.socialnetwork.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public void sendFriendRequest(Long recipientId) {
        User sender = getCurrentUser();
        userRepository.findById(recipientId)
                .orElseThrow(() -> new EntityNotFoundException("Recipient user not found with id: " + recipientId));


        FriendshipId friendshipId = createFriendshipId(sender.getId(), recipientId);
        Optional<Friendship> existingFriendship = friendshipRepository.findById(friendshipId);

        if (existingFriendship.isPresent()) {
            Friendship f = existingFriendship.get();
            switch (f.getStatus()) {
                case ACCEPTED:
                    throw new IllegalStateException("You are already friends.");
                case PENDING:
                    throw new IllegalStateException("A friend request is already pending.");
                case BLOCKED:
                    throw new IllegalStateException("Cannot send request. The user is blocked or has blocked you.");
                case DECLINED:
                    if (f.getActionUserId().equals(sender.getId())) {
                        throw new IllegalStateException("Your previous request was declined. You cannot send another one at this time.");
                    }
                    break;
            }
        }

        // Nếu không có hoặc là trường hợp DECLINED được phép gửi lại
        Friendship friendship = existingFriendship.orElse(new Friendship());
        friendship.setUserOneId(friendshipId.getUserOneId());
        friendship.setUserTwoId(friendshipId.getUserTwoId());
        friendship.setStatus(FriendshipStatus.PENDING);
        friendship.setActionUserId(sender.getId());

        friendshipRepository.save(friendship);
    }

    public void acceptFriendRequest(Long requesterId) {
        User currentUser = getCurrentUser();
        FriendshipId friendshipId = createFriendshipId(currentUser.getId(), requesterId);

        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Friendship request not found."));

        // Chỉ người nhận lời mời mới được chấp nhận
        if (friendship.getStatus() != FriendshipStatus.PENDING || !friendship.getActionUserId().equals(requesterId)) {
            throw new IllegalStateException("Invalid friend request or you are not the recipient.");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendship.setActionUserId(currentUser.getId()); // Người chấp nhận
        friendshipRepository.save(friendship);
    }

    public void declineFriendRequest(Long requesterId) {
        User currentUser = getCurrentUser();
        FriendshipId friendshipId = createFriendshipId(currentUser.getId(), requesterId);

        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Friendship request not found."));

        if (friendship.getStatus() != FriendshipStatus.PENDING || !friendship.getActionUserId().equals(requesterId)) {
            throw new IllegalStateException("No pending request to decline.");
        }

        friendship.setStatus(FriendshipStatus.DECLINED);
        friendship.setActionUserId(currentUser.getId());
        friendshipRepository.save(friendship);
    }

    public void cancelFriendRequest(Long recipientId) {
        User currentUser = getCurrentUser();
        FriendshipId friendshipId = createFriendshipId(currentUser.getId(), recipientId);

        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Friendship request not found."));

        // Đảm bảo chỉ người gửi mới có thể hủy
        if (friendship.getStatus() != FriendshipStatus.PENDING || !friendship.getActionUserId().equals(currentUser.getId())) {
            throw new IllegalStateException("No sent request to cancel.");
        }

        friendshipRepository.delete(friendship);
    }

    public void blockUser(Long userIdToBlock) {
        User currentUser = getCurrentUser();
        FriendshipId friendshipId = createFriendshipId(currentUser.getId(), userIdToBlock);

        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElse(new Friendship());

        friendship.setUserOneId(friendshipId.getUserOneId());
        friendship.setUserTwoId(friendshipId.getUserTwoId());
        friendship.setStatus(FriendshipStatus.BLOCKED);
        friendship.setActionUserId(currentUser.getId());

        friendshipRepository.save(friendship);
    }

    public void unblockUser(Long userIdToUnblock) {
        User currentUser = getCurrentUser();
        FriendshipId friendshipId = createFriendshipId(currentUser.getId(), userIdToUnblock);

        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("No block relationship found."));

        // Đảm bảo chỉ người đã chặn mới có thể bỏ chặn
        if (friendship.getStatus() != FriendshipStatus.BLOCKED || !friendship.getActionUserId().equals(currentUser.getId())) {
            throw new IllegalStateException("You did not block this user.");
        }
        // Khi bỏ chặn, mối quan hệ sẽ bị xóa hoàn toàn, hai người trở thành người lạ
        friendshipRepository.delete(friendship);
    }

    public void unfriend(Long friendId) {
        User currentUser = getCurrentUser();
        FriendshipId friendshipId = createFriendshipId(currentUser.getId(), friendId);

        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Friendship not found."));

        if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
            throw new IllegalStateException("You are not friends with this user.");
        }

        friendshipRepository.delete(friendship);
    }

    public List<UserProfileResponse> getFriendList() {
        User currentUser = getCurrentUser();
        List<Friendship> friendships = friendshipRepository.findAcceptedFriends(currentUser.getId());

        List<Long> friendIds = friendships.stream()
                .flatMap(f -> Stream.of(f.getUserOneId(), f.getUserTwoId()))
                .filter(id -> !id.equals(currentUser.getId()))
                .distinct()
                .collect(Collectors.toList());

        return userRepository.findAllById(friendIds).stream()
                .map(userService::mapUserToProfileResponse) // Tái sử dụng hàm map từ UserService
                .collect(Collectors.toList());
    }

    public List<UserProfileResponse> getPendingFriendRequests() {
        User currentUser = getCurrentUser();
        List<Friendship> pendingRequests = friendshipRepository.findPendingRequestsForUser(currentUser.getId());

        List<Long> requesterIds = pendingRequests.stream()
                .map(Friendship::getActionUserId)
                .collect(Collectors.toList());

        return userRepository.findAllById(requesterIds).stream()
                .map(userService::mapUserToProfileResponse)
                .collect(Collectors.toList());
    }

    public FriendshipStatusResponse getFriendshipStatus(Long otherUserId) {
        User currentUser = getCurrentUser();
        Long currentUserId = currentUser.getId();

        if (currentUserId.equals(otherUserId)) {
            return new FriendshipStatusResponse("SELF");
        }

        FriendshipId friendshipId = createFriendshipId(currentUserId, otherUserId);
        Optional<Friendship> friendshipOpt = friendshipRepository.findById(friendshipId);

        if (friendshipOpt.isEmpty()) {
            return new FriendshipStatusResponse("NONE"); // Chưa có quan hệ
        }

        Friendship friendship = friendshipOpt.get();

        // Dựa vào status và actionUserId để quyết định
        switch (friendship.getStatus()) {
            case ACCEPTED:
                return new FriendshipStatusResponse("FRIENDS"); // Đã là bạn bè

            case PENDING:
                // Nếu người thực hiện hành động cuối cùng (gửi lời mời) là MÌNH
                if (friendship.getActionUserId().equals(currentUserId)) {
                    return new FriendshipStatusResponse("PENDING_SENT");
                }
                // Nếu người thực hiện hành động cuối cùng là NGƯỜI KIA
                else {
                    return new FriendshipStatusResponse("PENDING_RECEIVED");
                }

            case BLOCKED:
                // Nếu mình là người chặn
                if (friendship.getActionUserId().equals(currentUserId)) {
                    return new FriendshipStatusResponse("BLOCKED_BY_ME");
                }
                // Nếu người kia chặn mình
                else {
                    return new FriendshipStatusResponse("BLOCKED_BY_OTHER");
                }

            case DECLINED:
                // Sau khi từ chối, coi như không có quan hệ gì
                return new FriendshipStatusResponse("NONE");

            default:
                return new FriendshipStatusResponse("NONE");
        }
    }


    // Helper để tạo FriendshipId một cách nhất quán (user id nhỏ hơn đứng trước)
    private FriendshipId createFriendshipId(Long user1Id, Long user2Id) {
        if (user1Id.equals(user2Id)) {
            throw new IllegalArgumentException("Cannot create friendship with oneself.");
        }
        return user1Id < user2Id ? new FriendshipId(user1Id, user2Id) : new FriendshipId(user2Id, user1Id);
    }

}
