DELETE FROM comments;
DELETE FROM post_likes;
DELETE FROM friendships;
DELETE FROM posts;
DELETE FROM users;

SET IDENTITY_INSERT users ON;
INSERT INTO users (id, email, password, role) VALUES (1, 'reporter@test.com', 'p', 'ROLE_USER');
SET IDENTITY_INSERT users OFF;