package com.example.socialnetwork.repository;

import com.example.socialnetwork.entity.Friendship;
import com.example.socialnetwork.entity.FriendshipId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, FriendshipId> {
    // Tìm tất cả bạn bè đã được chấp nhận của một user
    @Query("SELECT f FROM Friendship f WHERE (f.userOneId = :userId OR f.userTwoId = :userId) AND f.status = 'ACCEPTED'")
    List<Friendship> findAcceptedFriends(@Param("userId") Long userId);

    // Tìm các lời mời đang chờ mà user này là người nhận (action_user_id != userId)
    @Query("SELECT f FROM Friendship f WHERE (f.userOneId = :userId OR f.userTwoId = :userId) AND f.status = 'PENDING' AND f.actionUserId <> :userId")
    List<Friendship> findPendingRequestsForUser(@Param("userId") Long userId);

    // Lấy danh sách ID của tất cả bạn bè
    @Query("SELECT CASE WHEN f.userOneId = :userId THEN f.userTwoId ELSE f.userOneId END FROM Friendship f WHERE (f.userOneId = :userId OR f.userTwoId = :userId) AND f.status = 'ACCEPTED'")
    List<Long> findFriendIdsByUserId(@Param("userId") Long userId);

    // Đếm số lượng bạn bè mới trong một khoảng thời gian nhất định
    @Query("SELECT COUNT(f) FROM Friendship f WHERE (f.userOneId = :userId OR f.userTwoId = :userId) AND f.status = 'ACCEPTED' AND f.updatedAt BETWEEN :start AND :end")
    long countNewFriendsByUserIdInPeriod(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}