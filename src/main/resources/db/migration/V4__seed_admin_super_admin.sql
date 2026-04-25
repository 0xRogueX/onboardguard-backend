-- V3__seed_admin_super_admin.sql
-- Description: Seeds the initial system administrators
-- Default Login Password for both accounts is: password123
-- BCrypt Hash used: $2a$10$wT5H5dK6X.t/b.4fL7hQv.uM.X6w9.e/B7/8iR.N9.B/3m.A1.M2.

-- Note: Adjust the table name to 'users' if your @Table annotation is named differently.
-- If you are using PostgreSQL with sequence generation, you may need to specify the sequence for the ID (e.g., nextval('app_user_seq')).

INSERT INTO users (
    email,
    full_name,
    phone,
    password_hash,
    role,
    active,
    locked,
    created_at,
    updated_at
) VALUES (
    'superadmin@onboardguard.com',
    'Vivek Dadhaniya',
    '+911005550001',
    '$2a$10$wT5H5dK6X.t/b.4fL7hQv.uM.X6w9.e/B7/8iR.N9.B/3m.A1.M2.',
    'ROLE_SUPER_ADMIN',
    true,
    false,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'admin@onboardguard.com',
    'Vrunda Chavda',
    '+911005550002',
    '$2a$10$wT5H5dK6X.t/b.4fL7hQv.uM.X6w9.e/B7/8iR.N9.B/3m.A1.M2.',
    'ROLE_ADMIN',
    true,
    false,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);