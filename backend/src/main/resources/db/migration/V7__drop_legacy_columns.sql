DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'users'
          AND column_name = 'profile_photourl'
    ) AND EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'users'
          AND column_name = 'profile_photo_url'
    ) THEN
        UPDATE users
        SET profile_photo_url = profile_photourl
        WHERE profile_photo_url IS NULL
          AND profile_photourl IS NOT NULL;

        ALTER TABLE users
            DROP COLUMN profile_photourl;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'posts'
          AND column_name = 'filter'
    ) AND EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'posts'
          AND column_name = 'filter_id'
    ) THEN
        UPDATE posts
        SET filter_id = filter
        WHERE filter_id IS NULL
          AND filter IS NOT NULL;

        ALTER TABLE posts
            DROP COLUMN filter;
    END IF;
END $$;
