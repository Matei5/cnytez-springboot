-- copy any legacy profile_photourl data if profile_photo_url is null
UPDATE users
SET profile_photo_url = profile_photourl
WHERE profile_photo_url IS NULL AND profile_photourl IS NOT NULL;

ALTER TABLE users
    DROP COLUMN IF EXISTS profile_photourl;

-- copy any legacy filter data if filter_id is null
UPDATE posts
SET filter_id = filter
WHERE filter_id IS NULL AND filter IS NOT NULL;

ALTER TABLE posts
    DROP COLUMN IF EXISTS filter;
