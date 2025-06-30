DELETE FROM comments;
DELETE FROM post_likes;
DELETE FROM friendships;
DELETE FROM posts;
DELETE FROM users;

-- Tạo 3 user
SET IDENTITY_INSERT users ON;
INSERT INTO users (id, email, password, role) VALUES (1, 'current_user@test.com', 'p', 'ROLE_USER');
INSERT INTO users (id, email, password, role) VALUES (2, 'friend_user@test.com', 'p', 'ROLE_USER');
INSERT INTO users (id, email, password, role) VALUES (3, 'other_user@test.com', 'p', 'ROLE_USER');
SET IDENTITY_INSERT users OFF;

-- current_user và friend_user là bạn bè
INSERT INTO friendships (user_one_id, user_two_id, status, action_user_id) VALUES (1, 2, 'ACCEPTED', 2);

-- Mỗi user đăng 1 bài viết
SET IDENTITY_INSERT posts ON;
INSERT INTO posts (id, content, user_id, created_at) VALUES (101, 'Post by current_user', 1, GETDATE());
-- Tạo bài viết của friend_user trễ hơn 1 phút để kiểm tra sắp xếp
INSERT INTO posts (id, content, user_id, created_at) VALUES (102, 'Post by friend_user', 2, DATEADD(minute, 1, GETDATE()));
INSERT INTO posts (id, content, user_id, created_at) VALUES (103, 'Post by other_user', 3, GETDATE());
SET IDENTITY_INSERT posts OFF;