package com.example.socialnetwork.service;

import com.alibaba.excel.EasyExcel;

import com.example.socialnetwork.dto.report.WeeklyReportData;
import com.example.socialnetwork.entity.User;
import com.example.socialnetwork.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final FriendshipRepository friendshipRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public ByteArrayInputStream generateWeeklyActivityReport() throws IOException {
        User currentUser = getCurrentUser();
        Long userId = currentUser.getId();

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(7);

        // 1. Thu thập dữ liệu
        long postsLastWeek = postRepository.countByUserIdAndCreatedAtBetween(userId, start, end);
        long newFriendsLastWeek = friendshipRepository.countNewFriendsByUserIdInPeriod(userId, start, end);
        long newLikesLastWeek = postLikeRepository.countByPostUserIdAndCreatedAtBetween(userId, start, end);
        long newCommentsLastWeek = commentRepository.countByPostUserIdAndCreatedAtBetween(userId, start, end);

        // 2. Chuẩn bị danh sách dữ liệu để ghi ra Excel
        List<WeeklyReportData> reportData = new ArrayList<>();
        reportData.add(new WeeklyReportData("New Posts Last 7 Days", postsLastWeek));
        reportData.add(new WeeklyReportData("New Friends Last 7 Days", newFriendsLastWeek));
        reportData.add(new WeeklyReportData("New Likes Received Last 7 Days", newLikesLastWeek));
        reportData.add(new WeeklyReportData("New Comments Received Last 7 Days", newCommentsLastWeek));

        // 3. Sử dụng EasyExcel để ghi dữ liệu vào stream
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            EasyExcel.write(out, WeeklyReportData.class)
                    .sheet("Weekly Activity Report")
                    .doWrite(reportData);

            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}