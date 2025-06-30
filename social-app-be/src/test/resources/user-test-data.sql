DELETE FROM comments;
DELETE FROM post_likes;
DELETE FROM friendships;
DELETE FROM posts;
DELETE FROM users;

SET IDENTITY_INSERT users ON;
INSERT INTO users (id, email, password, role, full_name) VALUES (1, 'user1@test.com', 'p', 'ROLE_USER', 'User One');
SET IDENTITY_INSERT users OFF;