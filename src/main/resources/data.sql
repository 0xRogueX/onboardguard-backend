-- ==============================================================================
-- 1. SEED SYSTEM ADMINISTRATORS
-- ==============================================================================
-- Default Login Password for both accounts is: password123

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
    version
) VALUES (
             'vivekdadhaniya01@gmail.com',
             'Vivek Dadhaniya',
             '+911005550001',
             '$2a$12$PdA6xMe0kWghPV6BWF3K..Mtv0OvqbYHk1r.vbsJX.QxAf6YcbZqu',
             'ROLE_SUPER_ADMIN',
             true,
             false,
             CURRENT_TIMESTAMP,
             CURRENT_TIMESTAMP,
             0
         ),
         (
             'vrundachavda112@gmail.com',
             'Vrunda Chavda',
             '+911005550002',
             '$2a$12$PdA6xMe0kWghPV6BWF3K..Mtv0OvqbYHk1r.vbsJX.QxAf6YcbZqu',
             'ROLE_ADMIN',
             true,
             false,
             CURRENT_TIMESTAMP,
             CURRENT_TIMESTAMP,
             0
         )
    ON CONFLICT (email) DO NOTHING;


-- ==============================================================================
-- 2. SEED WATCHLIST SOURCES
-- ==============================================================================
INSERT INTO watchlist_sources (id, code, name, type, credibility_weight, active, created_at, updated_at, version)
VALUES
    (1, 'UN_SC', 'UN Security Council', 'OFFICIAL', 1.0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (2, 'INTERPOL', 'Interpol Red Notices', 'OFFICIAL', 1.0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (3, 'CBI_INDIA', 'CBI Wanted List', 'OFFICIAL', 1.0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (4, 'SEBI_DEBARRED', 'SEBI Debarred Entities', 'OFFICIAL', 1.0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (5, 'RBI_DEFAULTER', 'RBI Wilful Defaulters', 'OFFICIAL', 1.0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (6, 'NEWS_MEDIA', 'Global News Media', 'UNVERIFIED', 0.4, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (7, 'INTERNAL_HR', 'Internal HR Blacklist', 'INTERNAL', 0.8, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);


-- ==============================================================================
-- 3. SEED WATCHLIST CATEGORIES
-- ==============================================================================
INSERT INTO watchlist_categories (id, code, name, description, base_risk_score, is_active, created_at, updated_at, version)
VALUES
    (1, 'CRIMINAL', 'Criminal Records', 'Individuals with criminal background or global sanctions', 100, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (2, 'FRAUD', 'Known Fraudsters', 'Financial and cyber fraud offenders', 90, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (3, 'BLACKLIST', 'Blacklisted Entities', 'Vendors or individuals banned due to misconduct', 80, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (4, 'PEP', 'Politically Exposed Persons', 'Politicians and high-risk individuals', 70, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (5, 'PROFESSIONAL_MISCONDUCT', 'Debarred Professionals', 'Banned professionals with revoked licenses', 85, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (6, 'EMPLOYMENT_ISSUE', 'Employment Fraud', 'Fake employment or HR scams', 75, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);


-- ==============================================================================
-- 4. SEED WATCHLIST ENTRIES
-- ==============================================================================
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
(1030, 1, 3, 'Deepak Boxer', 'DEEPAKBOXER', 'HIGH', NULL, '888800001030', 'Indian', '{"crime":"Organized Crime"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

-- Vivek Dadhaniya (Additional Record 2): Criminal via CBI India
(1031, 1, 3, 'Vivek Dadhaniya', 'VIVEKDADHANIYA', 'HIGH', 'ABCDV1234E', '888800001009', 'Indian', '{"crime":"Money Laundering", "status":"Wanted"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

-- Vivek Dadhaniya (Additional Record 3): Fraud via SEBI Debarred Entities
(1032, 2, 4, 'Vivek Dadhaniya', 'VIVEKDADHANIYA', 'HIGH', 'ABCDV1234E', '888800001009', 'Indian', '{"case":"Insider Trading", "penalty":"Debarred for 5 years"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

-- Vivek Dadhaniya (Additional Record 4): Employment Issue via Internal HR
(1033, 6, 7, 'Vivek Dadhaniya', 'VIVEKDADHANIYA', 'MEDIUM', NULL, '888800001009', 'Indian', '{"issue":"Falsified Corporate Records"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);


-- ==============================================================================
-- 5. SEED ALIASES
-- ==============================================================================
INSERT INTO watchlist_aliases (id, entry_id, source_id, alias_name, alias_name_normalized, alias_type, created_at, updated_at, version)
VALUES
    (1, 1002, 6, 'The Wolf of Wall Street', 'THEWOLFOFWALLSTREET', 'AKA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (2, 1018, 2, 'Abu S', 'ABUS', 'SHORT_NAME', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (3, 1019, 2, 'Rajan N', 'RAJANN', 'SHORT_NAME', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (4, 1031, 3, 'Vicky Bhai', 'VIVEKBHAI', 'AKA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (5, 1032, 4, 'V. Dadhaniya', 'VDADHANIYA', 'SHORT_NAME', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);


-- ==============================================================================
-- 6. SEED EVIDENCE DOCUMENTS
-- ==============================================================================
INSERT INTO watchlist_evidence_documents (id, entry_id, source_id, cloud_storage_key, file_name, file_format, evidence_type, created_at, updated_at, version)
VALUES
    -- Global / Known
    (1, 1001, 1, 'watchlist/evidence/1001/REGULATORY_ORDER/un_resolution', 'un_resolution.jpg', 'JPG', 'REGULATORY_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (2, 1002, 6, 'watchlist/evidence/1002/NEWS_ARTICLE/wsj_article', 'wsj_article.jpg', 'JPG', 'NEWS_ARTICLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

    -- Indian High Profile
    (3, 1003, 5, 'watchlist/evidence/1003/REGULATORY_ORDER/rbi_wilful_defaulter_notice', 'rbi_wilful_defaulter_notice.jpg', 'JPG', 'REGULATORY_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (4, 1004, 5, 'watchlist/evidence/1004/COURT_ORDER/pnb_scam_fir', 'pnb_scam_fir.jpg', 'JPG', 'COURT_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (5, 1005, 5, 'watchlist/evidence/1005/REGULATORY_ORDER/choksi_ed_attachment', 'choksi_ed_attachment.jpg', 'JPG', 'REGULATORY_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

    -- USER PROVIDED NAMES
    (6, 1006, 7, 'watchlist/evidence/1006/INTERNAL_REPORT/hr_internal_memo_smit', 'hr_internal_memo_smit.jpg', 'JPG', 'INTERNAL_REPORT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (7, 1007, 7, 'watchlist/evidence/1007/INTERNAL_REPORT/cyber_fraud_internal_report', 'cyber_fraud_internal_report.jpg', 'JPG', 'INTERNAL_REPORT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (8, 1008, 4, 'watchlist/evidence/1008/REGULATORY_ORDER/sebi_debarment_order', 'sebi_debarment_order.jpg', 'JPG', 'REGULATORY_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (9, 1009, 6, 'watchlist/evidence/1009/NEWS_ARTICLE/pep_news_article', 'pep_news_article.jpg', 'JPG', 'NEWS_ARTICLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (10, 1010, 3, 'watchlist/evidence/1010/COURT_ORDER/cbi_chargesheet', 'cbi_chargesheet.jpg', 'JPG', 'COURT_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (11, 1011, 7, 'watchlist/evidence/1011/INTERNAL_REPORT/vendor_blacklisting_memo', 'vendor_blacklisting_memo.jpg', 'JPG', 'INTERNAL_REPORT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (12, 1012, 7, 'watchlist/evidence/1012/INTERNAL_REPORT/employment_fraud_investigation', 'employment_fraud_investigation.jpg', 'JPG', 'INTERNAL_REPORT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (13, 1013, 6, 'watchlist/evidence/1013/NEWS_ARTICLE/local_pep_coverage', 'local_pep_coverage.jpg', 'JPG', 'NEWS_ARTICLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (14, 1014, 7, 'watchlist/evidence/1014/INTERNAL_REPORT/suspicious_activity_log', 'suspicious_activity_log.jpg', 'JPG', 'INTERNAL_REPORT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

    -- Additional Indian Data
    (15, 1015, 4, 'watchlist/evidence/1015/REGULATORY_ORDER/sebi_fraud_reference', 'sebi_fraud_reference.jpg', 'JPG', 'REGULATORY_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (16, 1016, 3, 'watchlist/evidence/1016/COURT_ORDER/cbi_kidney_racket_fir', 'cbi_kidney_racket_fir.jpg', 'JPG', 'COURT_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (17, 1017, 3, 'watchlist/evidence/1017/COURT_ORDER/extortion_case_details', 'extortion_case_details.jpg', 'JPG', 'COURT_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (18, 1018, 2, 'watchlist/evidence/1018/OTHER/interpol_red_notice', 'interpol_red_notice.jpg', 'JPG', 'OTHER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (19, 1019, 2, 'watchlist/evidence/1019/OTHER/interpol_red_notice_rajan', 'interpol_red_notice_rajan.jpg', 'JPG', 'OTHER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

    -- Vendors / Fraud / Risk
    (20, 1020, 7, 'watchlist/evidence/1020/INTERNAL_REPORT/fake_billing_invoice', 'fake_billing_invoice.jpg', 'JPG', 'INTERNAL_REPORT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (21, 1021, 7, 'watchlist/evidence/1021/INTERNAL_REPORT/job_scam_complaint', 'job_scam_complaint.jpg', 'JPG', 'INTERNAL_REPORT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

    -- PEP
    (22, 1022, 6, 'watchlist/evidence/1022/NEWS_ARTICLE/political_scandal_news', 'political_scandal_news.jpg', 'JPG', 'NEWS_ARTICLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (23, 1023, 6, 'watchlist/evidence/1023/NEWS_ARTICLE/pep_declaration_form', 'pep_declaration_form.jpg', 'JPG', 'NEWS_ARTICLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

    -- Cyber / Fraud
    (24, 1024, 3, 'watchlist/evidence/1024/COURT_ORDER/financial_fraud_cbi_docket', 'financial_fraud_cbi_docket.jpg', 'JPG', 'COURT_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (25, 1025, 4, 'watchlist/evidence/1025/REGULATORY_ORDER/stock_market_scam_sebi', 'stock_market_scam_sebi.jpg', 'JPG', 'REGULATORY_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

    -- Misc Additional
    (26, 1026, 3, 'watchlist/evidence/1026/COURT_ORDER/extortion_fir', 'extortion_fir.jpg', 'JPG', 'COURT_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (27, 1027, 4, 'watchlist/evidence/1027/REGULATORY_ORDER/license_revocation_notice', 'license_revocation_notice.jpg', 'JPG', 'REGULATORY_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (28, 1028, 7, 'watchlist/evidence/1028/INTERNAL_REPORT/contract_violation_memo', 'contract_violation_memo.jpg', 'JPG', 'INTERNAL_REPORT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (29, 1029, 7, 'watchlist/evidence/1029/INTERNAL_REPORT/recruitment_scam_report', 'recruitment_scam_report.jpg', 'JPG', 'INTERNAL_REPORT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (30, 1030, 3, 'watchlist/evidence/1030/COURT_ORDER/organized_crime_chargesheet', 'organized_crime_chargesheet.jpg', 'JPG', 'COURT_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

    -- Vivek Dadhaniya Evidence
    (31, 1031, 3, 'watchlist/evidence/1031/COURT_ORDER/cbi_money_laundering_warrant', 'cbi_money_laundering_warrant.pdf', 'PDF', 'COURT_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (32, 1032, 4, 'watchlist/evidence/1032/REGULATORY_ORDER/sebi_insider_trading_ban', 'sebi_insider_trading_ban.pdf', 'PDF', 'REGULATORY_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (33, 1033, 7, 'watchlist/evidence/1033/INTERNAL_REPORT/hr_falsification_memo', 'hr_falsification_memo.jpg', 'JPG', 'INTERNAL_REPORT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);


-- ==============================================================================
-- 7. ADVANCE SEQUENCES
-- ==============================================================================
-- Advance sequences so manual IDs don't clash with future auto-inserts
SELECT setval(pg_get_serial_sequence('watchlist_entries', 'id'), coalesce(max(id), 1)) FROM watchlist_entries;
SELECT setval(pg_get_serial_sequence('watchlist_sources', 'id'), coalesce(max(id), 1)) FROM watchlist_sources;
SELECT setval(pg_get_serial_sequence('watchlist_categories', 'id'), coalesce(max(id), 1)) FROM watchlist_categories;
SELECT setval(pg_get_serial_sequence('watchlist_aliases', 'id'), coalesce(max(id), 1)) FROM watchlist_aliases;
SELECT setval(pg_get_serial_sequence('watchlist_evidence_documents', 'id'), coalesce(max(id), 1)) FROM watchlist_evidence_documents;


-- ==============================================================================
-- 8. SYSTEM CONFIGURATION INITIAL DATA
-- ==============================================================================
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