DELETE FROM comments;
DELETE FROM post_likes;
DELETE FROM friendships;
DELETE FROM posts;
DELETE FROM users;

SET IDENTITY_INSERT users ON;
INSERT INTO users (id, email, password, role) VALUES (1, 'user1@test.com', 'p', 'ROLE_USER');
INSERT INTO users (id, email, password, role) VALUES (2, 'user2@test.com', 'p', 'ROLE_USER');
INSERT INTO users (id, email, password, role) VALUES (3, 'user3@test.com', 'p', 'ROLE_USER');
SET IDENTITY_INSERT users OFF;

-- Dữ liệu cho kịch bản test: user2 đã gửi yêu cầu cho user1
INSERT INTO friendships (user_one_id, user_two_id, status, action_user_id) VALUES (1, 2, 'PENDING', 2);