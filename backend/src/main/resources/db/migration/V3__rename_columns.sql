ALTER TABLE posts
    RENAME COLUMN creation_date TO created_at;

ALTER TABLE posts
    RENAME COLUMN deletion_date TO deleted_at;

ALTER TABLE comments
    RENAME COLUMN creation_date TO created_at;

ALTER TABLE comments
    RENAME COLUMN deletion_date TO deleted_at;

ALTER TABLE subreddits
    RENAME COLUMN creation_date TO created_at;

ALTER TABLE users
    RENAME COLUMN deletion_date TO deleted_at;