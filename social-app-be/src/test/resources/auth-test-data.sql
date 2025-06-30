DELETE FROM comments;
DELETE FROM post_likes;
DELETE FROM friendships;
DELETE FROM posts;
DELETE FROM users;

SET IDENTITY_INSERT users ON;
INSERT INTO users (id, email, password, role) VALUES (1, 'user@test.com', '$2a$10$GiseH.Y5p2d2pE./y2s5Zu2TY20uS4vGj5xRqdRkDE2yv25GvSgJm', 'ROLE_USER');
SET IDENTITY_INSERT users OFF;