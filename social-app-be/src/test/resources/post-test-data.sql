DELETE FROM comments;
DELETE FROM post_likes;
DELETE FROM friendships;
DELETE FROM posts;
DELETE FROM users;

SET IDENTITY_INSERT users ON;
INSERT INTO users (id, email, password, role) VALUES (1, 'user1@test.com', 'p', 'ROLE_USER');
INSERT INTO users (id, email, password, role) VALUES (2, 'user2@test.com', 'p', 'ROLE_USER');
SET IDENTITY_INSERT users OFF;

SET IDENTITY_INSERT posts ON;
INSERT INTO posts (id, content, user_id) VALUES (101, 'Post 1 by User 1', 1);
SET IDENTITY_INSERT posts OFF;