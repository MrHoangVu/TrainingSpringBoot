-- Bảng người dùng
CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name NVARCHAR(255),
    date_of_birth DATE,
    occupation NVARCHAR(255),
    address NVARCHAR(255),
    avatar_url VARCHAR(500),
    role VARCHAR(50) NOT NULL,
    otp VARCHAR(10),
    otp_generated_time DATETIME2,
    password_reset_token VARCHAR(255),
    password_reset_token_expiry DATETIME2,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

-- Bảng bài viết
CREATE TABLE posts (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    content NTEXT,
    image_url VARCHAR(500),
    user_id BIGINT NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT fk_posts_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Bảng bình luận
CREATE TABLE comments (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    content NVARCHAR(1000) NOT NULL,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT fk_comments_users FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_comments_posts FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

-- Bảng lượt thích bài viết
CREATE TABLE post_likes (
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    PRIMARY KEY (user_id, post_id),
    CONSTRAINT fk_post_likes_users FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_post_likes_posts FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

-- Bảng quan hệ bạn bè
CREATE TABLE friendships (
    user_one_id BIGINT NOT NULL,
    user_two_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    action_user_id BIGINT NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    PRIMARY KEY (user_one_id, user_two_id),
    CONSTRAINT fk_friendships_user_one FOREIGN KEY (user_one_id) REFERENCES users(id),
    CONSTRAINT fk_friendships_user_two FOREIGN KEY (user_two_id) REFERENCES users(id),
    CONSTRAINT fk_friendships_action_user FOREIGN KEY (action_user_id) REFERENCES users(id),
    -- Đảm bảo user_one_id luôn nhỏ hơn user_two_id để tránh trùng lặp (A-B và B-A)
    CONSTRAINT check_user_ids CHECK (user_one_id < user_two_id)
);