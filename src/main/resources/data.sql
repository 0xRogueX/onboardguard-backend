-- Description: Seeds the initial system administrators
-- Default Login Password for both accounts is: password123
-- BCrypt Hash used: $2a$10$wT5H5dK6X.t/b.4fL7hQv.uM.X6w9.e/B7/8iR.N9.B/3m.A1.M2.

INSERT INTO users (
    email,
    full_name,
    phone,
    password_hash,
    role,
    is_active,
    is_locked,
    created_at,
    updated_at,
    version      -- Added version column
) VALUES (
             'dev.vivek.dadhaniya@gmail.com',
             'Vivek Dadhaniya',
             '+911005550001',
             '$2a$12$f2rXDoQMZARDYQpgir7/cOBaPrNgGdzSw/j0z6S539CPpbTYdpPxC',
             'ROLE_SUPER_ADMIN',
             true,
             false,
             CURRENT_TIMESTAMP,
             CURRENT_TIMESTAMP,
             0    -- Initialize version to 0 for Hibernate
         ),
         (
             'vrundachavda112@gmail.com',
             'Vrunda Chavda',
             '+911005550002',
             '$2a$12$f2rXDoQMZARDYQpgir7/cOBaPrNgGdzSw/j0z6S539CPpbTYdpPxC',
             'ROLE_ADMIN',
             true,
             false,
             CURRENT_TIMESTAMP,
             CURRENT_TIMESTAMP,
             0    -- Initialize version to 0 for Hibernate
         )
    ON CONFLICT (email) DO NOTHING;