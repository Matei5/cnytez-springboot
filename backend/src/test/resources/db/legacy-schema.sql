CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    profile_photo_url VARCHAR(255),
    profile_photourl VARCHAR(255),
    deletion_date TIMESTAMP WITH TIME ZONE
);

CREATE TABLE filters (
    id INTEGER PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    label VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE subreddits (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(255) NOT NULL,
    description VARCHAR(500) NOT NULL,
    icon_url VARCHAR(255),
    creation_date TIMESTAMP WITH TIME ZONE NOT NULL,
    owner_id UUID NOT NULL REFERENCES users(id)
);

CREATE TABLE subreddit_members (
    subreddit_id UUID NOT NULL REFERENCES subreddits(id),
    user_id UUID NOT NULL REFERENCES users(id),
    PRIMARY KEY (subreddit_id, user_id)
);

CREATE TABLE posts (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    text TEXT,
    image VARCHAR(255),
    filter_id INTEGER REFERENCES filters(id),
    filter INTEGER,
    creation_date TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    deletion_date TIMESTAMP WITH TIME ZONE,
    owner_id UUID NOT NULL REFERENCES users(id),
    subreddit_id UUID NOT NULL REFERENCES subreddits(id)
);

CREATE TABLE comments (
    id UUID PRIMARY KEY,
    text TEXT,
    creation_date TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    deletion_date TIMESTAMP WITH TIME ZONE,
    owner_id UUID NOT NULL REFERENCES users(id),
    post_id UUID NOT NULL REFERENCES posts(id),
    parent_comment_id UUID REFERENCES comments(id)
);

CREATE TABLE post_votes (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    post_id UUID NOT NULL REFERENCES posts(id),
    vote_type VARCHAR(255) NOT NULL,
    UNIQUE (user_id, post_id)
);

CREATE TABLE comment_votes (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    comment_id UUID NOT NULL REFERENCES comments(id),
    vote_type VARCHAR(255) NOT NULL,
    UNIQUE (user_id, comment_id)
);

INSERT INTO users (
    id, username, email, password, profile_photo_url, profile_photourl
) VALUES (
    '00000000-0000-0000-0000-000000000001',
    'legacy-user',
    'legacy@example.com',
    'not-a-real-password',
    NULL,
    'https://legacy.example/profile.png'
);

INSERT INTO filters (id, name, label)
VALUES (2, 'sepia', 'Legacy sepia label');

INSERT INTO subreddits (
    id, name, display_name, description, creation_date, owner_id
) VALUES (
    '00000000-0000-0000-0000-000000000002',
    'legacy',
    'Legacy',
    'Legacy migration test subreddit',
    CURRENT_TIMESTAMP,
    '00000000-0000-0000-0000-000000000001'
);

INSERT INTO posts (
    id, title, filter_id, filter, creation_date, owner_id, subreddit_id
) VALUES (
    '00000000-0000-0000-0000-000000000003',
    'Legacy post',
    NULL,
    2,
    CURRENT_TIMESTAMP,
    '00000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000002'
);
