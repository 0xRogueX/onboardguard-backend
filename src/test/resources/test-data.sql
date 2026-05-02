-- -- H2-SPECIFIC TEST DATA SCRIPT
-- -- This file provides test data specifically for H2 in-memory database
-- -- Named 'test-data.sql' to avoid conflict with main 'data.sql' (PostgreSQL syntax)
-- -- Default Login Password: password123
-- -- BCrypt Hash: $2a$10$zGRLkBrIBIKI2bReYnw9dejmVCf4UVarfKEDVuiZ4vFfoYvj3iY6q
--
-- -- H2 doesn't support PostgreSQL's ON CONFLICT syntax
-- -- Use simple INSERT instead (H2 auto-increments ID if email doesn't exist)
-- DELETE FROM users WHERE email IN ('dev.vivek.dadhaniya@gmail.com', 'vrundachavda112@gmail.com');
--
-- INSERT INTO users (
--     email,
--     full_name,
--     phone,
--     password_hash,
--     role,
--     is_active,
--     is_locked,
--     created_at,
--     updated_at,
--     version
-- ) VALUES
--     ('dev.vivek.dadhaniya@gmail.com', 'Vivek Dadhaniya', '+911005550001', '$2a$10$zGRLkBrIBIKI2bReYnw9dejmVCf4UVarfKEDVuiZ4vFfoYvj3iY6q', 'ROLE_SUPER_ADMIN', true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0),
--     ('vrundachavda112@gmail.com', 'Vrunda Chavda', '+911005550002', '$2a$10$zGRLkBrIBIKI2bReYnw9dejmVCf4UVarfKEDVuiZ4vFfoYvj3iY6q', 'ROLE_ADMIN', true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0);
--
-- -- 1. Seed Sources
-- INSERT INTO watchlist_sources (id, code, name, type, credibility_weight, active, created_at, updated_at, version)
-- VALUES
--     (1, 'UN_SC', 'UN Security Council', 'OFFICIAL', 1.0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0),
--     (2, 'INTERPOL', 'Interpol Red Notices', 'OFFICIAL', 1.0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0),
--     (3, 'CBI_INDIA', 'CBI Wanted List', 'OFFICIAL', 1.0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0),
--     (4, 'SEBI_DEBARRED', 'SEBI Debarred Entities', 'OFFICIAL', 1.0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0),
--     (5, 'RBI_DEFAULTER', 'RBI Wilful Defaulters', 'OFFICIAL', 1.0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0),
--     (6, 'NEWS_MEDIA', 'Global News Media', 'UNVERIFIED', 0.4, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0),
--     (7, 'INTERNAL_HR', 'Internal HR Blacklist', 'INTERNAL', 0.8, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0);
--
-- -- 2. Seed Categories
-- INSERT INTO watchlist_categories (id, code, name, description, base_risk_score, is_active, created_at, updated_at, version)
-- VALUES
--     (1, 'CRIMINAL', 'Criminal Records', 'Individuals with criminal background or global sanctions', 100, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0),
--     (2, 'FRAUD', 'Known Fraudsters', 'Financial and cyber fraud offenders', 90, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0),
--     (3, 'BLACKLIST', 'Blacklisted Entities', 'Vendors or individuals banned due to misconduct', 80, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0),
--     (4, 'PEP', 'Politically Exposed Persons', 'Politicians and high-risk individuals', 70, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0),
--     (5, 'PROFESSIONAL_MISCONDUCT', 'Debarred Professionals', 'Banned professionals with revoked licenses', 85, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0),
--     (6, 'EMPLOYMENT_ISSUE', 'Employment Fraud', 'Fake employment or HR scams', 75, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0);
--
-- -- 3. Seed Watchlist Entries
-- INSERT INTO watchlist_entries
-- (id, category_id, source_id, primary_name, primary_name_normalized, severity, pan_number, nationality, category_specific_data, is_active, created_at, updated_at, version)
-- VALUES
-- (1001, 1, 1, 'Osama Bin Laden', 'OSAMABINLADEN', 'HIGH', NULL, 'Afghan', '{"program":"Al-Qaida"}', true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0),
-- (1002, 2, 6, 'Jordan Belfort', 'JORDANBELFORT', 'HIGH', 'ABCDE1234F', 'American', '{"crime":"Securities Fraud"}', true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0),
-- (1003, 2, 5, 'Vijay Mallya', 'VIJAYMALLYA', 'HIGH', 'AAACV1234A', 'Indian', '{"case":"Bank Fraud"}', true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0),
-- (1004, 2, 5, 'Nirav Modi', 'NIRAVMODI', 'HIGH', 'AAACN2345B', 'Indian', '{"case":"PNB Scam"}', true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0),
-- (1005, 2, 5, 'Mehul Choksi', 'MEHULCHOKSI', 'HIGH', 'AAACM6789D', 'Indian', '{"case":"Bank Fraud"}', true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0),
-- (1006, 1, 7, 'Smit Karbathiya', 'SMITKARBATHIYA', 'HIGH', NULL, 'Indian', '{"type":"Criminal"}', true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0),
-- (1007, 2, 7, 'Divayrajsinh Sindhav', 'DIVAYRAJSINHSINDHAV', 'HIGH', NULL, 'Indian', '{"type":"Cyber Fraud"}', true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0),
-- (1008, 5, 4, 'Neel Chhantbar', 'NEELCHHANTBAR', 'MEDIUM', NULL, 'Indian', '{"status":"Debarred Professional"}', true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0),
-- (1009, 4, 6, 'Vivek Dadhaniya', 'VIVEKDADHANIYA', 'MEDIUM', NULL, 'Indian', '{"role":"PEP"}', true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0),
-- (1010, 1, 3, 'Tanmay Jotangia', 'TANMAYJOTANGIA', 'HIGH', NULL, 'Indian', '{"record":"Criminal"}', true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0);
--
-- -- 4. Seed Aliases (comment out since we don't have all requisite entries)
-- -- INSERT INTO watchlist_aliases (id, entry_id, source_id, alias_name, alias_name_normalized, alias_type, created_at, updated_at, version)
-- -- VALUES
-- --     (1, 1002, 6, 'The Wolf of Wall Street', 'THEWOLFOFWALLSTREET', 'AKA', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0),
-- --     (2, 1018, 2, 'Abu S', 'ABUS', 'SHORT_NAME', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0);
--


-- 1. Seed Sources
INSERT INTO watchlist_sources (id, code, name, type, credibility_weight, active, created_at, updated_at, version)
VALUES
    (1, 'UN_SC', 'UN Security Council', 'OFFICIAL', 1.0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (2, 'INTERPOL', 'Interpol Red Notices', 'OFFICIAL', 1.0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (3, 'CBI_INDIA', 'CBI Wanted List', 'OFFICIAL', 1.0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (4, 'SEBI_DEBARRED', 'SEBI Debarred Entities', 'OFFICIAL', 1.0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (5, 'RBI_DEFAULTER', 'RBI Wilful Defaulters', 'OFFICIAL', 1.0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (6, 'NEWS_MEDIA', 'Global News Media', 'UNVERIFIED', 0.4, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (7, 'INTERNAL_HR', 'Internal HR Blacklist', 'INTERNAL', 0.8, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 2. Seed Categories (Updated to match CategoryCode enum and WatchlistCategory entity)
INSERT INTO watchlist_categories (id, code, name, description, base_risk_score, is_active, created_at, updated_at, version)
VALUES
    (1, 'CRIMINAL', 'Criminal Records', 'Individuals with criminal background or global sanctions', 100, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (2, 'FRAUD', 'Known Fraudsters', 'Financial and cyber fraud offenders', 90, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (3, 'BLACKLIST', 'Blacklisted Entities', 'Vendors or individuals banned due to misconduct', 80, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (4, 'PEP', 'Politically Exposed Persons', 'Politicians and high-risk individuals', 70, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (5, 'PROFESSIONAL_MISCONDUCT', 'Debarred Professionals', 'Banned professionals with revoked licenses', 85, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (6, 'EMPLOYMENT_ISSUE', 'Employment Fraud', 'Fake employment or HR scams', 75, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 3. Seed Watchlist Entries (Added aadhaar_number and source_id mapping)
INSERT INTO watchlist_entries
(id, category_id, source_id, primary_name, primary_name_normalized, severity, pan_number, aadhaar_number, nationality, category_specific_data, is_active, created_at, updated_at, version)
VALUES

-- Global / Known
(1001, 1, 1, 'Osama Bin Laden', 'OSAMABINLADEN', 'HIGH', NULL, '888800001001', 'Afghan', '{"program":"Al-Qaida"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1002, 2, 6, 'Jordan Belfort', 'JORDANBELFORT', 'HIGH', 'ABCDE1234F', '888800001002', 'American', '{"crime":"Securities Fraud"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

-- Indian High Profile
(1003, 2, 5, 'Vijay Mallya', 'VIJAYMALLYA', 'HIGH', 'AAACV1234A', '888800001003', 'Indian', '{"case":"Bank Fraud"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1004, 2, 5, 'Nirav Modi', 'NIRAVMODI', 'HIGH', 'AAACN2345B', '888800001004', 'Indian', '{"case":"PNB Scam"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1005, 2, 5, 'Mehul Choksi', 'MEHULCHOKSI', 'HIGH', 'AAACM6789D', '888800001005', 'Indian', '{"case":"Bank Fraud"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

-- USER PROVIDED NAMES
(1006, 1, 7, 'Smit Karbathiya', 'SMITKARBATHIYA', 'HIGH', NULL, '888800001006', 'Indian', '{"type":"Criminal"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1007, 2, 7, 'Divayrajsinh Sindhav', 'DIVAYRAJSINHSINDHAV', 'HIGH', NULL, '888800001007', 'Indian', '{"type":"Cyber Fraud"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1008, 5, 4, 'Neel Chhantbar', 'NEELCHHANTBAR', 'MEDIUM', NULL, '888800001008', 'Indian', '{"status":"Debarred Professional"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1009, 4, 6, 'Vivek Dadhaniya', 'VIVEKDADHANIYA', 'MEDIUM', NULL, '888800001009', 'Indian', '{"role":"PEP"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1010, 1, 3, 'Tanmay Jotangia', 'TANMAYJOTANGIA', 'HIGH', NULL, '888800001010', 'Indian', '{"record":"Criminal"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1011, 3, 7, 'Rahul Dave', 'RAHULDAVE', 'MEDIUM', NULL, '888800001011', 'Indian', '{"status":"Blacklisted Vendor"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1012, 6, 7, 'Bhavika Chhatbar', 'BHAVIKACHHATBAR', 'MEDIUM', NULL, '888800001012', 'Indian', '{"type":"Employment Fraud"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1013, 4, 6, 'Darshit Dhaduk', 'DARSHITDHADUK', 'LOW', NULL, '888800001013', 'Indian', '{"role":"PEP"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1014, 1, 7, 'Vrunda Chavda', 'VRUNDACHAVDA', 'MEDIUM', NULL, '888800001014', 'Indian', '{"note":"Under investigation"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

-- Additional Indian Data
(1015, 2, 4, 'Rakesh Jhunjhunwala Fraud Case Ref', 'RAKESHJHUNJHUNWALA_REF', 'LOW', NULL, '888800001015', 'Indian', '{"note":"Reference flagged entity"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1016, 1, 3, 'Amit Kumar', 'AMITKUMAR', 'HIGH', NULL, '888800001016', 'Indian', '{"crime":"Kidney Racket"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1017, 2, 3, 'Sachin Waze', 'SACHINWAZE', 'HIGH', NULL, '888800001017', 'Indian', '{"case":"Extortion"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1018, 1, 2, 'Abu Salem', 'ABUSALEM', 'HIGH', NULL, '888800001018', 'Indian', '{"crime":"Organized Crime"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1019, 1, 2, 'Chhota Rajan', 'CHHOTARAJAN', 'HIGH', NULL, '888800001019', 'Indian', '{"crime":"Underworld"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

-- Vendors / Fraud / Risk
(1020, 3, 7, 'Shree Traders Pvt Ltd', 'SHREETRADERS', 'MEDIUM', NULL, '888800001020', 'Indian', '{"issue":"Fake billing"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1021, 6, 7, 'QuickHire Solutions', 'QUICKHIRESOLUTIONS', 'MEDIUM', NULL, '888800001021', 'Indian', '{"issue":"Job Scam"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

-- PEP
(1022, 4, 6, 'Lalu Prasad Yadav', 'LALUPRASADYADAV', 'MEDIUM', NULL, '888800001022', 'Indian', '{"position":"Politician"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1023, 4, 6, 'Sharad Pawar', 'SHARADPAWAR', 'LOW', NULL, '888800001023', 'Indian', '{"position":"Politician"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

-- Cyber / Fraud
(1024, 2, 3, 'Naresh Goyal Case Ref', 'NARESHGOYALREF', 'HIGH', NULL, '888800001024', 'Indian', '{"case":"Financial Fraud"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1025, 2, 4, 'Ketan Parekh', 'KETANPAREKH', 'HIGH', NULL, '888800001025', 'Indian', '{"case":"Stock Market Scam"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

-- Misc Additional
(1026, 1, 3, 'Ravi Pujari', 'RAVIPUJARI', 'HIGH', NULL, '888800001026', 'Indian', '{"crime":"Extortion"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1027, 5, 4, 'Suresh Accountant', 'SURESHACCOUNTANT', 'MEDIUM', NULL, '888800001027', 'Indian', '{"status":"License Revoked"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1028, 3, 7, 'Global Infra Vendor', 'GLOBALINFRAVENDOR', 'LOW', NULL, '888800001028', 'Indian', '{"issue":"Contract Violation"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1029, 6, 7, 'JobFast India', 'JOBFASTINDIA', 'MEDIUM', NULL, '888800001029', 'Indian', '{"issue":"Recruitment Scam"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1030, 1, 3, 'Deepak Boxer', 'DEEPAKBOXER', 'HIGH', NULL, '888800001030', 'Indian', '{"crime":"Organized Crime"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 4. Seed Aliases (Added source_id)
INSERT INTO watchlist_aliases (id, entry_id, source_id, alias_name, alias_name_normalized, alias_type, created_at, updated_at, version)
VALUES
    (1, 1002, 6, 'The Wolf of Wall Street', 'THEWOLFOFWALLSTREET', 'AKA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (2, 1018, 2, 'Abu S', 'ABUS', 'SHORT_NAME', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (3, 1019, 2, 'Rajan N', 'RAJANN', 'SHORT_NAME', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 5. Seed Watchlist Evidence Documents (New Entity)
INSERT INTO watchlist_evidence_documents (id, entry_id, source_id, cloud_storage_key, file_name, file_format, evidence_type, created_at, updated_at, version)
VALUES
    (1, 1001, 1, 'https://res.cloudinary.com/dr2dzhwqx/image/authenticated/s--28dDruHv--/v1777666054/candidates/1/AADHAAR_CARD/f660b1c5-d9cd-4a34-8f52-f0733c82a99f.jpg', 'un_resolution.jpg', 'JPG', 'REGULATORY_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (2, 1002, 6, 'https://res.cloudinary.com/dr2dzhwqx/image/authenticated/s--28dDruHv--/v1777666054/candidates/1/AADHAAR_CARD/f660b1c5-d9cd-4a34-8f52-f0733c82a99f.jpg', 'wsj_article.jpg', 'JPG', 'NEWS_ARTICLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (3, 1003, 5, 'https://res.cloudinary.com/dr2dzhwqx/image/authenticated/s--28dDruHv--/v1777666054/candidates/1/AADHAAR_CARD/f660b1c5-d9cd-4a34-8f52-f0733c82a99f.jpg', 'rbi_wilful_defaulter_notice.jpg', 'JPG', 'REGULATORY_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (4, 1006, 7, 'https://res.cloudinary.com/dr2dzhwqx/image/authenticated/s--28dDruHv--/v1777666054/candidates/1/AADHAAR_CARD/f660b1c5-d9cd-4a34-8f52-f0733c82a99f.jpg', 'hr_internal_memo.jpg', 'JPG', 'INTERNAL_REPORT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (5, 1008, 4, 'https://res.cloudinary.com/dr2dzhwqx/image/authenticated/s--28dDruHv--/v1777666054/candidates/1/AADHAAR_CARD/f660b1c5-d9cd-4a34-8f52-f0733c82a99f.jpg', 'sebi_debarment_order.jpg', 'JPG', 'COURT_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (6, 1018, 2, 'https://res.cloudinary.com/dr2dzhwqx/image/authenticated/s--28dDruHv--/v1777666054/candidates/1/AADHAAR_CARD/f660b1c5-d9cd-4a34-8f52-f0733c82a99f.jpg', 'interpol_red_notice.jpg', 'JPG', 'OTHER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 6. Advance sequences so manual IDs don't clash with future auto-inserts
ALTER TABLE watchlist_entries ALTER COLUMN id RESTART WITH 1031;
ALTER TABLE watchlist_sources ALTER COLUMN id RESTART WITH 8;
ALTER TABLE watchlist_categories ALTER COLUMN id RESTART WITH 7;
ALTER TABLE watchlist_aliases ALTER COLUMN id RESTART WITH 4;
ALTER TABLE watchlist_evidence_documents ALTER COLUMN id RESTART WITH 7;


-- ==============================================================================
-- SYSTEM CONFIGURATION INITIAL DATA
-- ==============================================================================

-- Delete existing to ensure a clean start for the new schema
DELETE FROM system_config;

-- SCREENING THRESHOLDS
INSERT INTO system_config (config_key, config_value, config_type, category, description, is_sensitive, created_at, updated_at, version)
VALUES
    ('SCREENING_THRESHOLD_FUZZY', '0.80', 'DOUBLE', 'SCREENING', 'Threshold for Jaro-Winkler similarity (0.0 to 1.0)', false, NOW(), NOW(), 0),
    ('SCREENING_THRESHOLD_MEDIUM', '31.0', 'DOUBLE', 'SCREENING', 'Score threshold to classify as MEDIUM risk', false, NOW(), NOW(), 0),
    ('SCREENING_THRESHOLD_HIGH', '61.0', 'DOUBLE', 'SCREENING', 'Score threshold to classify as HIGH risk', false, NOW(), NOW(), 0);

-- SCREENING MULTIPLIERS (Corroboration)
INSERT INTO system_config (config_key, config_value, config_type, category, description, is_sensitive, created_at, updated_at, version)
VALUES
    ('SCREENING_MULT_NAME_ONLY', '0.5', 'DOUBLE', 'SCREENING', 'Multiplier when only name matches', false, NOW(), NOW(), 0),
    ('SCREENING_MULT_NAME_ONE_ID', '0.8', 'DOUBLE', 'SCREENING', 'Multiplier when name and one ID match', false, NOW(), NOW(), 0),
    ('SCREENING_MULT_NAME_TWO_IDS', '1.0', 'DOUBLE', 'SCREENING', 'Multiplier when name and two IDs match', false, NOW(), NOW(), 0),
    ('SCREENING_MULT_NAME_ORG', '0.75', 'DOUBLE', 'SCREENING', 'Multiplier when name and organization match', false, NOW(), NOW(), 0),
    ('SCREENING_MULT_NAME_ORG_DESIGNATION', '1.0', 'DOUBLE', 'SCREENING', 'Multiplier when name, org, and designation match', false, NOW(), NOW(), 0);

-- SCREENING BONUSES
INSERT INTO system_config (config_key, config_value, config_type, category, description, is_sensitive, created_at, updated_at, version)
VALUES
    ('SCREENING_BONUS_CRIMINAL', '15.0', 'DOUBLE', 'SCREENING', 'Bonus points for Criminal category matches', false, NOW(), NOW(), 0),
    ('SCREENING_BONUS_PEP', '15.0', 'DOUBLE', 'SCREENING', 'Bonus points for PEP category matches', false, NOW(), NOW(), 0),
    ('SCREENING_BONUS_SEVERITY_HIGH', '10.0', 'DOUBLE', 'SCREENING', 'Bonus points for High Severity entries', false, NOW(), NOW(), 0);

-- FEATURE TOGGLES
INSERT INTO system_config (config_key, config_value, config_type, category, description, is_sensitive, created_at, updated_at, version)
VALUES
    ('ACTIVE_SCREENING_STRATEGY', 'ADVANCED', 'STRING', 'SCREENING', 'Current active screening engine strategy (BASIC/ADVANCED)', false, NOW(), NOW(), 0),
    ('SCREENING_AUTO_REJECT_ENABLED', 'false', 'BOOLEAN', 'SCREENING', 'Whether to automatically reject candidates with HIGH risk score', false, NOW(), NOW(), 0);

-- STORAGE & AUTH
INSERT INTO system_config (config_key, config_value, config_type, category, description, is_sensitive, created_at, updated_at, version)
VALUES
    ('STORAGE_PRESIGNED_URL_TTL_MINUTES', '15', 'INTEGER', 'STORAGE', 'TTL for Cloudinary/S3 presigned URLs in minutes', false, NOW(), NOW(), 0),
    ('AUTH_JWT_EXPIRATION_MS', '900000', 'INTEGER', 'SYSTEM', 'JWT token expiration time in milliseconds (default 15m)', false, NOW(), NOW(), 0);

-- SLA & BUSINESS RULES
INSERT INTO system_config (config_key, config_value, config_type, category, description, is_sensitive, created_at, updated_at, version)
VALUES
    ('SLA_HOURS', '48', 'INTEGER', 'BUSINESS', 'SLA for officer to review a flagged screening', false, NOW(), NOW(), 0);