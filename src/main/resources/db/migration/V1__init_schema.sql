-- V1__init_schema.sql
-- Creates the core app_users table.
-- No roles/permissions/role_permissions/user_roles tables — those are replaced
-- by the static RolePermissions.java switch and a single enum column on app_users.

CREATE EXTENSION IF NOT EXISTS "pgcrypto"; -- enables gen_random_uuid()

-- ── Users ────────────────────────────────────────────────────────────────────
CREATE TABLE app_users (
                           id                        UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,

    -- Identity
                           username                  VARCHAR(100) NOT NULL,
                           email                     VARCHAR(255) NOT NULL,
                           password_hash             VARCHAR(255) NOT NULL,
                           full_name                 VARCHAR(255) NOT NULL,
                           phone                     VARCHAR(20),

    -- Single column replaces roles + user_roles tables
    -- Stores the RoleCode enum name: ROLE_CANDIDATE, ROLE_OFFICER_L1, etc.
                           role                      VARCHAR(30)  NOT NULL,

    -- Account state
                           is_active                 BOOLEAN      NOT NULL DEFAULT TRUE,
                           is_locked                 BOOLEAN      NOT NULL DEFAULT FALSE,
                           failed_login_count        INTEGER      NOT NULL DEFAULT 0,
                           last_login_at             TIMESTAMPTZ,

    -- Password reset (stored in DB so it survives Redis restarts)
                           password_reset_token      VARCHAR(255),
                           password_reset_expires_at TIMESTAMPTZ,

    -- Auditing (populated by Spring Data JPA AuditingEntityListener)
                           created_at                TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                           created_by                VARCHAR(255),           -- email of creator, or "SYSTEM"
                           updated_at                TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                           updated_by                VARCHAR(255),
                           version                   BIGINT       NOT NULL DEFAULT 0,  -- optimistic locking

    -- Constraints
                           CONSTRAINT uq_app_users_email    UNIQUE (email),
                           CONSTRAINT uq_app_users_username UNIQUE (username),
                           CONSTRAINT chk_app_users_role    CHECK (role IN (
                                                                            'ROLE_CANDIDATE',
                                                                            'ROLE_OFFICER_L1',
                                                                            'ROLE_OFFICER_L2',
                                                                            'ROLE_ADMIN',
                                                                            'ROLE_SUPER_ADMIN'
                               ))
);

-- Indexes — covering the three most common lookup patterns
CREATE INDEX idx_app_users_email    ON app_users (email);
CREATE INDEX idx_app_users_username ON app_users (username);
CREATE INDEX idx_app_users_role     ON app_users (role);

-- Partial index: active staff lookups (admin panel)
CREATE INDEX idx_app_users_active_staff
    ON app_users (role, is_active)
    WHERE role != 'ROLE_CANDIDATE';

COMMENT ON TABLE  app_users                IS 'All portal users: candidates, officers, admins, super admins.';
COMMENT ON COLUMN app_users.role           IS 'Maps to RoleCode enum. Permissions are resolved statically in RolePermissions.java.';
COMMENT ON COLUMN app_users.version        IS 'Optimistic locking — incremented on every UPDATE to prevent lost-update races.';
COMMENT ON COLUMN app_users.password_reset_token IS 'Null when no active reset request.';