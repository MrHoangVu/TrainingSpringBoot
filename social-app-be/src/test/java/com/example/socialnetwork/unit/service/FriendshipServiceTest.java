package com.example.socialnetwork.unit.service;

import com.example.socialnetwork.dto.response.UserProfileResponse;
import com.example.socialnetwork.entity.*;
import com.example.socialnetwork.repository.FriendshipRepository;
import com.example.socialnetwork.repository.UserRepository;
import com.example.socialnetwork.service.FriendshipService;
import com.example.socialnetwork.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceTest {

    @Mock
    private FriendshipRepository friendshipRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private FriendshipService friendshipService;

    private User currentUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        currentUser = User.builder().id(1L).email("current@user.com").build();
        otherUser = User.builder().id(2L).email("other@user.com").build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(currentUser.getEmail(), null)
        );
        when(userRepository.findByEmail(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
    }

    @Test
    void sendFriendRequest_Success() {
        // Given
        when(userRepository.findById(otherUser.getId())).thenReturn(Optional.of(otherUser));
        when(friendshipRepository.findById(any(FriendshipId.class))).thenReturn(Optional.empty());
        // When
        friendshipService.sendFriendRequest(otherUser.getId());
        // Then
        verify(friendshipRepository, times(1)).save(any(Friendship.class));
    }

    @Test
    void sendFriendRequest_ToSelf_ThrowsException() {
        // Thêm dòng mock này
        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));

        assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.sendFriendRequest(currentUser.getId());
        });
    }

    @Test
    void sendFriendRequest_WhenAlreadyFriends_ThrowsException() {
        // Given: Hai người đã là bạn
        Friendship acceptedFriendship = Friendship.builder().status(FriendshipStatus.ACCEPTED).build();
        when(friendshipRepository.findById(any(FriendshipId.class))).thenReturn(Optional.of(acceptedFriendship));
        when(userRepository.findById(otherUser.getId())).thenReturn(Optional.of(otherUser));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            friendshipService.sendFriendRequest(otherUser.getId());
        });
        assertEquals("You are already friends.", exception.getMessage());
    }

    @Test
    void sendFriendRequest_WhenRequestIsPending_ThrowsException() {
        // Given: Đã có một yêu cầu đang chờ
        Friendship pendingFriendship = Friendship.builder().status(FriendshipStatus.PENDING).build();
        when(friendshipRepository.findById(any(FriendshipId.class))).thenReturn(Optional.of(pendingFriendship));
        when(userRepository.findById(otherUser.getId())).thenReturn(Optional.of(otherUser));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            friendshipService.sendFriendRequest(otherUser.getId());
        });
        assertEquals("A friend request is already pending.", exception.getMessage());
    }

    @Test
    void acceptFriendRequest_Success() {
        Friendship pendingRequest = Friendship.builder()
                .userOneId(1L).userTwoId(2L)
                .status(FriendshipStatus.PENDING)
                .actionUserId(otherUser.getId()) // otherUser đã gửi yêu cầu
                .build();
        when(friendshipRepository.findById(any(FriendshipId.class))).thenReturn(Optional.of(pendingRequest));

        friendshipService.acceptFriendRequest(otherUser.getId());

        assertEquals(FriendshipStatus.ACCEPTED, pendingRequest.getStatus());
        assertEquals(currentUser.getId(), pendingRequest.getActionUserId());
        verify(friendshipRepository, times(1)).save(pendingRequest);
    }

    @Test
    void acceptFriendRequest_InvalidState_ThrowsException() {
        // Yêu cầu không phải PENDING
        Friendship acceptedRequest = Friendship.builder().status(FriendshipStatus.ACCEPTED).build();
        when(friendshipRepository.findById(any(FriendshipId.class))).thenReturn(Optional.of(acceptedRequest));

        assertThrows(IllegalStateException.class, () -> {
            friendshipService.acceptFriendRequest(otherUser.getId());
        });
    }

    @Test
    void unfriend_Success() {
        Friendship acceptedFriendship = Friendship.builder()
                .userOneId(1L).userTwoId(2L)
                .status(FriendshipStatus.ACCEPTED)
                .build();
        when(friendshipRepository.findById(any(FriendshipId.class))).thenReturn(Optional.of(acceptedFriendship));
        doNothing().when(friendshipRepository).delete(acceptedFriendship);

        friendshipService.unfriend(otherUser.getId());

        verify(friendshipRepository, times(1)).delete(acceptedFriendship);
    }

    @Test
    void getFriendList_ReturnsFriendProfiles() {
        Friendship friendship = Friendship.builder().userOneId(1L).userTwoId(2L).status(FriendshipStatus.ACCEPTED).build();
        when(friendshipRepository.findAcceptedFriends(currentUser.getId())).thenReturn(Collections.singletonList(friendship));
        when(userRepository.findAllById(Collections.singletonList(otherUser.getId()))).thenReturn(Collections.singletonList(otherUser));
        when(userService.mapUserToProfileResponse(otherUser)).thenReturn(new UserProfileResponse());

        List<UserProfileResponse> friends = friendshipService.getFriendList();

        assertFalse(friends.isEmpty());
        assertEquals(1, friends.size());
    }

    @Test
    void declineFriendRequest_Success() {
        // Given
        Friendship pendingRequest = Friendship.builder()
                .userOneId(1L).userTwoId(2L)
                .status(FriendshipStatus.PENDING)
                .actionUserId(otherUser.getId()) // otherUser đã gửi yêu cầu
                .build();
        when(friendshipRepository.findById(any(FriendshipId.class))).thenReturn(Optional.of(pendingRequest));

        // When
        friendshipService.declineFriendRequest(otherUser.getId());

        // Then
        verify(friendshipRepository, times(1)).save(pendingRequest);
        assertEquals(FriendshipStatus.DECLINED, pendingRequest.getStatus());
        assertEquals(currentUser.getId(), pendingRequest.getActionUserId());
    }


    @Test
    void declineOrCancelFriendRequest_NoPendingRequest_ThrowsException() {
        // Given
        Friendship acceptedRequest = Friendship.builder().status(FriendshipStatus.ACCEPTED).build();
        when(friendshipRepository.findById(any(FriendshipId.class))).thenReturn(Optional.of(acceptedRequest));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            friendshipService.declineFriendRequest(otherUser.getId());
        });
    }

    @Test
    void getPendingFriendRequests_ReturnsRequesterProfiles() {
        // Given
        Friendship pendingRequest = Friendship.builder()
                .userOneId(1L).userTwoId(2L)
                .status(FriendshipStatus.PENDING)
                .actionUserId(otherUser.getId()) // otherUser gửi yêu cầu cho currentUser
                .build();
        when(friendshipRepository.findPendingRequestsForUser(currentUser.getId())).thenReturn(Collections.singletonList(pendingRequest));
        when(userRepository.findAllById(Collections.singletonList(otherUser.getId()))).thenReturn(Collections.singletonList(otherUser));
        when(userService.mapUserToProfileResponse(otherUser)).thenReturn(new UserProfileResponse());

        // When
        List<UserProfileResponse> requesters = friendshipService.getPendingFriendRequests();

        // Then
        assertFalse(requesters.isEmpty());
        assertEquals(1, requesters.size());
    }



    // Thêm các test case này vào file FriendshipServiceTest.java đã có

    @Test
    void cancelFriendRequest_Success() {
        // Given: currentUser đã gửi yêu cầu cho otherUser
        Friendship sentRequest = Friendship.builder()
                .userOneId(1L).userTwoId(2L)
                .status(FriendshipStatus.PENDING)
                .actionUserId(currentUser.getId())
                .build();
        when(friendshipRepository.findById(any(FriendshipId.class))).thenReturn(Optional.of(sentRequest));
        doNothing().when(friendshipRepository).delete(any(Friendship.class));

        // When
        friendshipService.cancelFriendRequest(otherUser.getId());

        // Then
        verify(friendshipRepository, times(1)).delete(sentRequest);
    }

    @Test
    void cancelFriendRequest_NotSender_ThrowsException() {
        // Given: otherUser gửi yêu cầu, currentUser không phải người gửi
        Friendship receivedRequest = Friendship.builder()
                .status(FriendshipStatus.PENDING)
                .actionUserId(otherUser.getId())
                .build();
        when(friendshipRepository.findById(any(FriendshipId.class))).thenReturn(Optional.of(receivedRequest));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            friendshipService.cancelFriendRequest(otherUser.getId());
        });
    }

    @Test
    void blockUser_NewRelationship_CreatesAndBlocks() {
        // Given: Chưa có quan hệ nào trước đó
        when(friendshipRepository.findById(any(FriendshipId.class))).thenReturn(Optional.empty());

        // When
        friendshipService.blockUser(otherUser.getId());

        // Then
        ArgumentCaptor<Friendship> friendshipCaptor = ArgumentCaptor.forClass(Friendship.class);
        verify(friendshipRepository, times(1)).save(friendshipCaptor.capture());
        Friendship savedFriendship = friendshipCaptor.getValue();
        assertEquals(FriendshipStatus.BLOCKED, savedFriendship.getStatus());
        assertEquals(currentUser.getId(), savedFriendship.getActionUserId());
    }

    @Test
    void blockUser_ExistingRelationship_UpdatesToBlocked() {
        // Given: Hai người đã là bạn
        Friendship acceptedFriendship = Friendship.builder().status(FriendshipStatus.ACCEPTED).build();
        when(friendshipRepository.findById(any(FriendshipId.class))).thenReturn(Optional.of(acceptedFriendship));

        // When
        friendshipService.blockUser(otherUser.getId());

        // Then
        verify(friendshipRepository, times(1)).save(acceptedFriendship);
        assertEquals(FriendshipStatus.BLOCKED, acceptedFriendship.getStatus());
    }

    @Test
    void unblockUser_Success() {
        // Given: currentUser đã chặn otherUser
        Friendship blockedRelationship = Friendship.builder()
                .status(FriendshipStatus.BLOCKED)
                .actionUserId(currentUser.getId())
                .build();
        when(friendshipRepository.findById(any(FriendshipId.class))).thenReturn(Optional.of(blockedRelationship));
        doNothing().when(friendshipRepository).delete(any(Friendship.class));

        // When
        friendshipService.unblockUser(otherUser.getId());

        // Then
        verify(friendshipRepository, times(1)).delete(blockedRelationship);
    }

    @Test
    void unblockUser_NotBlocker_ThrowsException() {
        // Given: otherUser đã chặn currentUser
        Friendship blockedRelationship = Friendship.builder()
                .status(FriendshipStatus.BLOCKED)
                .actionUserId(otherUser.getId())
                .build();
        when(friendshipRepository.findById(any(FriendshipId.class))).thenReturn(Optional.of(blockedRelationship));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            friendshipService.unblockUser(otherUser.getId());
        });
    }
}