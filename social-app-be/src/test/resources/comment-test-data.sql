-- Dọn dẹp dữ liệu cũ
DELETE FROM comments;
DELETE FROM post_likes;
DELETE FROM friendships;
DELETE FROM posts;
DELETE FROM users;

-- Thêm dữ liệu mới với ID cố định
SET IDENTITY_INSERT users ON;
INSERT INTO users (id, email, password, role, full_name) VALUES (1, 'user1@test.com', 'p', 'ROLE_USER', 'Post Owner');
INSERT INTO users (id, email, password, role, full_name) VALUES (2, 'user2@test.com', 'p', 'ROLE_USER', 'Comment Owner');
INSERT INTO users (id, email, password, role, full_name) VALUES (3, 'user3@test.com', 'p', 'ROLE_USER', 'Unrelated User');
SET IDENTITY_INSERT users OFF;

SET IDENTITY_INSERT posts ON;
INSERT INTO posts (id, content, user_id, created_at, updated_at) VALUES (101, 'Post 1 by User 1', 1, GETDATE(), GETDATE());
SET IDENTITY_INSERT posts OFF;

SET IDENTITY_INSERT comments ON;
INSERT INTO comments (id, content, user_id, post_id, created_at, updated_at) VALUES (1001, 'Comment by User 2', 2, 101, GETDATE(), GETDATE());
SET IDENTITY_INSERT comments OFF;