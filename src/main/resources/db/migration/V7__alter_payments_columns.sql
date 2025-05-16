-- Untuk kolom course_id
ALTER TABLE payments ALTER COLUMN course_id TYPE UUID USING course_id::uuid;

-- Untuk kolom user_id
ALTER TABLE payments ALTER COLUMN user_id TYPE UUID USING user_id::uuid;