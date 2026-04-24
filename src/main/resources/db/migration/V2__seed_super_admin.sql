-- V2__seed_super_admin.sql
-- Seeds the initial Super Admin account.
--
-- BEFORE RUNNING:
--   1. Generate a real BCrypt hash (cost 12) for your chosen password:
--        new BCryptPasswordEncoder(12).encode("YourChosenPassword")
--      Or use the CLI:
--        htpasswd -bnBC 12 "" "YourChosenPassword" | tr -d ':\n'
--   2. Replace the placeholder hash below with the actual output.
--   3. Set the password in your secrets manager — do not commit it to source control.
--
-- ON CONFLICT DO NOTHING: safe to re-run; will not overwrite a changed password.

INSERT INTO app_users (
    id,
    username,
    email,
    password_hash,
    full_name,
    role,
    is_active,
    is_locked,
    failed_login_count,
    created_at,
    updated_at,
    created_by,
    version
)
VALUES (
           gen_random_uuid(),
           'superadmin',
           'superadmin@onboardguard.internal',
           '$2a$12$REPLACE_WITH_REAL_BCRYPT_HASH_DO_NOT_COMMIT_REAL_VALUE',
           'Platform Super Administrator',
           'ROLE_SUPER_ADMIN',
           TRUE,
           FALSE,
           0,
           NOW(),
           NOW(),
           'SYSTEM',
           0
       )
    ON CONFLICT (email) DO NOTHING;