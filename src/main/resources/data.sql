-- Description: Seeds the initial system administrators and exhaustive watchlist data
-- Default Login Password for both accounts is: password123

INSERT INTO users (
    email, full_name, phone, password_hash, role, is_active, is_locked, created_at, updated_at, version
) VALUES (
             'vivekdadhaniya01@gmail.com', 'Vivek Dadhaniya', '+911005550001',
             '$2a$12$PdA6xMe0kWghPV6BWF3K..Mtv0OvqbYHk1r.vbsJX.QxAf6YcbZqu', 'ROLE_SUPER_ADMIN', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
         ), (
             'vrundachavda112@gmail.com', 'Vrunda Chavda', '+911005550002',
             '$2a$12$PdA6xMe0kWghPV6BWF3K..Mtv0OvqbYHk1r.vbsJX.QxAf6YcbZqu', 'ROLE_ADMIN', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
         ) ON CONFLICT (email) DO NOTHING;

-- 1. Seed Sources
INSERT INTO watchlist_sources (id, code, name, type, credibility_weight, active, created_at, updated_at, version)
VALUES
      (1, 'UN_SC', 'UN Security Council', 'OFFICIAL', 1.0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (2, 'INTERPOL', 'Interpol Red Notices', 'OFFICIAL', 1.0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (3, 'CBI_INDIA', 'CBI Wanted List', 'OFFICIAL', 1.0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (4, 'SEBI_DEBARRED', 'SEBI Debarred Entities', 'OFFICIAL', 1.0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (5, 'RBI_DEFAULTER', 'RBI Wilful Defaulters', 'OFFICIAL', 1.0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (6, 'NEWS_MEDIA', 'Global News Media', 'UNVERIFIED', 0.4, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (7, 'INTERNAL_HR', 'Internal HR Blacklist', 'INTERNAL', 0.8, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (8, 'ED_INDIA', 'Enforcement Directorate', 'OFFICIAL', 1.0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (9, 'CUSTOMS_INDIA', 'Indian Customs', 'OFFICIAL', 0.9, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (10, 'MCA_INDIA', 'MCA Disqualified Directors', 'OFFICIAL', 0.9, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 2. Seed Categories
INSERT INTO watchlist_categories (id, code, name, description, base_risk_score, is_active, created_at, updated_at, version)
VALUES
    (1, 'CRIMINAL', 'Criminal Records', 'Individuals with criminal background or global sanctions', 100, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (2, 'FRAUD', 'Known Fraudsters', 'Financial and cyber fraud offenders', 90, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (3, 'BLACKLIST', 'Blacklisted Entities', 'Vendors or individuals banned due to misconduct', 80, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (4, 'PEP', 'Politically Exposed Persons', 'Politicians and high-risk individuals', 70, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (5, 'PROFESSIONAL_MISCONDUCT', 'Debarred Professionals', 'Banned professionals with revoked licenses', 85, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (6, 'EMPLOYMENT_ISSUE', 'Employment Fraud', 'Fake employment or HR scams', 75, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 3. Seed Watchlist Entries
INSERT INTO watchlist_entries (id, category_id, source_id, primary_name, primary_name_normalized, severity, pan_number, aadhaar_number, nationality, organization_name, designation, category_specific_data, is_active, created_at, updated_at, version) VALUES

-- === SCENARIO 1: EXACT MATCHES & BASIC VARIATIONS ===
(1001, 1, 1, 'Osama Bin Laden', 'OSAMABINLADEN', 'HIGH', NULL, '888800001001', 'Afghan', NULL, NULL, '{"program":"Al-Qaida"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1002, 2, 6, 'Jordan Belfort', 'JORDANBELFORT', 'HIGH', 'ABCDE1234F', '888800001002', 'American', 'Stratton Oakmont', 'CEO', '{"crime":"Securities Fraud"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

-- === SCENARIO 2: INITIALS MATCHING EXAMPLES ===
-- V. Dadhaniya vs Vivek Dadhaniya
(1003, 2, 5, 'V. Dadhaniya', 'VDADHANIYA', 'HIGH', 'ABCDV1234E', '888800001003', 'Indian', 'Tech Corp', 'Director', '{"case":"Bank Fraud"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
-- R. Dave vs Rahul Dave
(1004, 3, 7, 'R. Dave', 'RDAVE', 'MEDIUM', NULL, '888800001004', 'Indian', 'Vendor Ltd', 'Manager', '{"status":"Blacklisted"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
-- S. Karbathiya vs Smit Karbathiya
(1005, 1, 3, 'S. Karbathiya', 'SKARBATHIYA', 'HIGH', 'XYZAQ1234B', '888800001005', 'Indian', NULL, NULL, '{"crime":"Theft"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

-- === SCENARIO 3: FUZZY MATCHING EXAMPLES ===
-- Divayrajsinh Sindhav vs Divayrajsinh Sindhav (Typo versions)
(1006, 2, 7, 'Divayraj Sindhav', 'DIVAYRAJSINDHAV', 'HIGH', NULL, '888800001006', 'Indian', 'Cyber Solutions', 'Analyst', '{"type":"Cyber Fraud"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1007, 2, 8, 'Divyrajsinh Sindhav', 'DIVYRAJSINHSINDHAV', 'HIGH', NULL, '888800001007', 'Indian', 'Cyber Solutions', 'Analyst', '{"case":"Money Laundering"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
-- Neel Chhantbar vs Neel Chhatbar
(1008, 5, 4, 'Neel Chhatbar', 'NEELCHHATBAR', 'MEDIUM', 'NEELC1234N', '888800001008', 'Indian', 'Accounting Firm', 'CA', '{"status":"Debarred"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

-- === SCENARIO 4: MULTIPLE SOURCES, ONE CATEGORY ===
-- Tanmay Jotangia (CRIMINAL) listed by CBI, INTERPOL, and INTERNAL
(1009, 1, 3, 'Tanmay Jotangia', 'TANMAYJOTANGIA', 'HIGH', 'TANMY2345J', '888800001009', 'Indian', 'Syndicate', 'Leader', '{"record":"Criminal"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1010, 1, 2, 'Tanmay Jotangia', 'TANMAYJOTANGIA', 'HIGH', 'TANMY2345J', '888800001009', 'Indian', 'Syndicate', 'Leader', '{"record":"Interpol Red Notice"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1011, 1, 7, 'Tanmay Jotangia', 'TANMAYJOTANGIA', 'HIGH', 'TANMY2345J', '888800001009', 'Indian', 'Syndicate', 'Leader', '{"record":"Internal HR flagged"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

-- === SCENARIO 5: ONE SOURCE, MULTIPLE CATEGORIES ===
-- Vrunda Chavda flagged by INTERNAL HR for Fraud, Employment Issue, Blacklist
(1012, 2, 7, 'Vrunda Chavda', 'VRUNDACHAVDA', 'HIGH', 'VRUND1234C', '888800001012', 'Indian', 'Tech Mahindra', 'Developer', '{"issue":"Financial Fraud"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1013, 6, 7, 'Vrunda Chavda', 'VRUNDACHAVDA', 'MEDIUM', 'VRUND1234C', '888800001012', 'Indian', 'Tech Mahindra', 'Developer', '{"issue":"Faked Degree"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1014, 3, 7, 'Vrunda Chavda', 'VRUNDACHAVDA', 'LOW', 'VRUND1234C', '888800001012', 'Indian', 'Tech Mahindra', 'Developer', '{"issue":"Blacklisted Employee"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

-- === SCENARIO 6: MULTIPLE SOURCES, MULTIPLE CATEGORIES ===
-- Vivek Dadhaniya (Complex case - PEP, Fraud, Criminal from different sources)
(1015, 4, 6, 'Vivek Dadhaniya', 'VIVEKDADHANIYA', 'MEDIUM', 'VIVKD1234D', '888800001015', 'Indian', 'Govt of India', 'Minister', '{"role":"PEP"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1016, 1, 3, 'Vivek Dadhaniya', 'VIVEKDADHANIYA', 'HIGH', 'VIVKD1234D', '888800001015', 'Indian', 'Govt of India', 'Minister', '{"crime":"Money Laundering"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1017, 2, 4, 'Vivek Dadhaniya', 'VIVEKDADHANIYA', 'HIGH', 'VIVKD1234D', '888800001015', 'Indian', 'Govt of India', 'Minister', '{"case":"Insider Trading"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1018, 6, 7, 'Vivek Dadhaniya', 'VIVEKDADHANIYA', 'MEDIUM', 'VIVKD1234D', '888800001015', 'Indian', 'Govt of India', 'Minister', '{"issue":"Falsified Records"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

-- === SCENARIO 7: NAME & ORG & DESIGNATION MATCHES ===
-- Bhavika Chhatbar matches with Org and Designation
(1019, 6, 7, 'Bhavika Chhatbar', 'BHAVIKACHHATBAR', 'MEDIUM', 'BHAVI2345C', '888800001019', 'Indian', 'QuickHire Solutions', 'HR Manager', '{"type":"Employment Fraud"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1020, 2, 3, 'Bhavika Chhatbar', 'BHAVIKACHHATBAR', 'HIGH', 'BHAVI2345C', '888800001019', 'Indian', 'QuickHire Solutions', 'HR Manager', '{"case":"Recruitment Scam"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

-- === SCENARIO 8: ONLY ID MATCHES (NAME DIFFERENT/ALIAS) ===
-- Darshit Dhaduk (Using an alias D. Dhaduk, but exact PAN match)
(1021, 4, 6, 'D. Dhaduk', 'DDHADUK', 'LOW', 'DARSI1234D', '888800001021', 'Indian', 'Local Party', 'Member', '{"role":"PEP"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1022, 5, 10, 'Darshit Dhaduk', 'DARSHITDHADUK', 'MEDIUM', 'DARSI1234D', '888800001021', 'Indian', 'Defunct Corp', 'Director', '{"status":"Disqualified"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

-- === SCENARIO 9: MORE ALIAS CASES ===
(1023, 1, 2, 'Dawood Ibrahim', 'DAWOODIBRAHIM', 'HIGH', NULL, NULL, 'Indian', 'D-Company', 'Leader', '{"crime":"Terrorism"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1024, 2, 5, 'Vijay Mallya', 'VIJAYMALLYA', 'HIGH', 'AAACV1234A', '888800001024', 'Indian', 'Kingfisher', 'Chairman', '{"case":"Bank Fraud"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1025, 2, 5, 'Nirav Modi', 'NIRAVMODI', 'HIGH', 'AAACN2345B', '888800001025', 'Indian', 'Firestar', 'Founder', '{"case":"PNB Scam"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

-- Add more diverse testing records (Total > 30 distinct scenarios to cover all)
(1026, 3, 7, 'Shree Traders Pvt Ltd', 'SHREETRADERS', 'MEDIUM', 'SHREE2345T', '888800001026', 'Indian', 'Shree Traders Pvt Ltd', 'Vendor', '{"issue":"Fake billing"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1027, 6, 7, 'QuickHire Solutions', 'QUICKHIRESOLUTIONS', 'MEDIUM', 'QUICE2345H', '888800001027', 'Indian', 'QuickHire Solutions', 'Agency', '{"issue":"Job Scam"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1028, 4, 6, 'Lalu Prasad Yadav', 'LALUPRASADYADAV', 'MEDIUM', 'LALUP2345P', '888800001028', 'Indian', 'RJD', 'Politician', '{"position":"Politician"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1029, 2, 4, 'Ketan Parekh', 'KETANPAREKH', 'HIGH', 'KETAN2345P', '888800001029', 'Indian', 'KP Group', 'Broker', '{"case":"Stock Market Scam"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1030, 5, 4, 'Suresh Accountant', 'SURESHACCOUNTANT', 'MEDIUM', 'SURES2345A', '888800001030', 'Indian', 'Suresh Associates', 'CA', '{"status":"License Revoked"}'::jsonb, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 4. Seed Aliases
INSERT INTO watchlist_aliases (id, entry_id, source_id, alias_name, alias_name_normalized, alias_type, created_at, updated_at, version)
VALUES
    (1, 1002, 6, 'The Wolf of Wall Street', 'THEWOLFOFWALLSTREET', 'AKA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (2, 1023, 2, 'Dawood Hassan', 'DAWOODHASSAN', 'AKA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (3, 1023, 2, 'Bhai', 'BHAI', 'NICKNAME', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (4, 1003, 5, 'Vivek D', 'VIVEKD', 'SHORT_NAME', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (5, 1004, 7, 'Rahul D', 'RAHULD', 'SHORT_NAME', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (6, 1005, 3, 'Smit K', 'SMITK', 'SHORT_NAME', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (7, 1006, 7, 'Divya Sindhav', 'DIVYASINDHAV', 'NICKNAME', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (8, 1008, 4, 'Neel C', 'NEELC', 'SHORT_NAME', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (9, 1009, 3, 'Tanmay J', 'TANMAYJ', 'SHORT_NAME', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (10, 1012, 7, 'Vrunda C', 'VRUNDAC', 'SHORT_NAME', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (11, 1015, 6, 'VD', 'VD', 'SHORT_NAME', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (12, 1019, 7, 'Bhavika C', 'BHAVIKAC', 'SHORT_NAME', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 5. Seed Watchlist Evidence Documents
INSERT INTO watchlist_evidence_documents (id, entry_id, source_id, cloud_storage_key, file_name, file_format, evidence_type, created_at, updated_at, version)
VALUES
      (1, 1001, 1, 'watchlist/evidence/1001/REGULATORY_ORDER/un_resolution', 'un_resolution.jpg', 'JPG', 'REGULATORY_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (2, 1002, 6, 'watchlist/evidence/1002/NEWS_ARTICLE/wsj_article', 'wsj_article.jpg', 'JPG', 'NEWS_ARTICLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (3, 1003, 5, 'watchlist/evidence/1003/REGULATORY_ORDER/rbi_notice', 'rbi_notice.jpg', 'JPG', 'REGULATORY_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (4, 1004, 7, 'watchlist/evidence/1004/INTERNAL_REPORT/vendor_ban', 'vendor_ban.jpg', 'JPG', 'INTERNAL_REPORT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (5, 1005, 3, 'watchlist/evidence/1005/COURT_ORDER/cbi_fir', 'cbi_fir.jpg', 'JPG', 'COURT_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (6, 1006, 7, 'watchlist/evidence/1006/INTERNAL_REPORT/hr_report', 'hr_report.jpg', 'JPG', 'INTERNAL_REPORT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (7, 1007, 8, 'watchlist/evidence/1007/REGULATORY_ORDER/ed_notice', 'ed_notice.jpg', 'JPG', 'REGULATORY_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (8, 1008, 4, 'watchlist/evidence/1008/REGULATORY_ORDER/sebi_debar', 'sebi_debar.jpg', 'JPG', 'REGULATORY_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (9, 1009, 3, 'watchlist/evidence/1009/COURT_ORDER/cbi_wanted', 'cbi_wanted.jpg', 'JPG', 'COURT_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (10, 1010, 2, 'watchlist/evidence/1010/OTHER/interpol_notice', 'interpol_notice.jpg', 'JPG', 'OTHER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (11, 1011, 7, 'watchlist/evidence/1011/INTERNAL_REPORT/hr_flag', 'hr_flag.jpg', 'JPG', 'INTERNAL_REPORT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (12, 1012, 7, 'watchlist/evidence/1012/INTERNAL_REPORT/fraud_report', 'fraud_report.jpg', 'JPG', 'INTERNAL_REPORT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (13, 1015, 6, 'watchlist/evidence/1015/NEWS_ARTICLE/pep_news', 'pep_news.jpg', 'JPG', 'NEWS_ARTICLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (14, 1016, 3, 'watchlist/evidence/1016/COURT_ORDER/cbi_chargesheet', 'cbi_chargesheet.jpg', 'JPG', 'COURT_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (15, 1017, 4, 'watchlist/evidence/1017/REGULATORY_ORDER/sebi_order', 'sebi_order.jpg', 'JPG', 'REGULATORY_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (16, 1018, 7, 'watchlist/evidence/1018/INTERNAL_REPORT/falsified_records', 'falsified_records.jpg', 'JPG', 'INTERNAL_REPORT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (17, 1019, 7, 'watchlist/evidence/1019/INTERNAL_REPORT/employment_fraud', 'employment_fraud.jpg', 'JPG', 'INTERNAL_REPORT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (18, 1020, 3, 'watchlist/evidence/1020/COURT_ORDER/recruitment_scam', 'recruitment_scam.jpg', 'JPG', 'COURT_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (19, 1021, 6, 'watchlist/evidence/1021/NEWS_ARTICLE/party_member', 'party_member.jpg', 'JPG', 'NEWS_ARTICLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (20, 1022, 10, 'watchlist/evidence/1022/REGULATORY_ORDER/mca_disqualified', 'mca_disqualified.jpg', 'JPG', 'REGULATORY_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (21, 1023, 2, 'watchlist/evidence/1023/OTHER/dawood_interpol', 'dawood_interpol.jpg', 'JPG', 'OTHER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (22, 1024, 5, 'watchlist/evidence/1024/REGULATORY_ORDER/mallya_rbi', 'mallya_rbi.jpg', 'JPG', 'REGULATORY_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (23, 1025, 5, 'watchlist/evidence/1025/REGULATORY_ORDER/nirav_rbi', 'nirav_rbi.jpg', 'JPG', 'REGULATORY_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (24, 1026, 7, 'watchlist/evidence/1026/INTERNAL_REPORT/fake_billing', 'fake_billing.jpg', 'JPG', 'INTERNAL_REPORT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (25, 1027, 7, 'watchlist/evidence/1027/INTERNAL_REPORT/job_scam', 'job_scam.jpg', 'JPG', 'INTERNAL_REPORT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (26, 1028, 6, 'watchlist/evidence/1028/NEWS_ARTICLE/lalu_news', 'lalu_news.jpg', 'JPG', 'NEWS_ARTICLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (27, 1029, 4, 'watchlist/evidence/1029/REGULATORY_ORDER/ketan_sebi', 'ketan_sebi.jpg', 'JPG', 'REGULATORY_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
      (28, 1030, 4, 'watchlist/evidence/1030/REGULATORY_ORDER/suresh_debar', 'suresh_debar.jpg', 'JPG', 'REGULATORY_ORDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);


-- 6. Advance sequences so manual IDs don't clash with future auto-inserts
SELECT setval(pg_get_serial_sequence('watchlist_entries', 'id'), coalesce(max(id), 1)) FROM watchlist_entries;
SELECT setval(pg_get_serial_sequence('watchlist_sources', 'id'), coalesce(max(id), 1)) FROM watchlist_sources;
SELECT setval(pg_get_serial_sequence('watchlist_categories', 'id'), coalesce(max(id), 1)) FROM watchlist_categories;
SELECT setval(pg_get_serial_sequence('watchlist_aliases', 'id'), coalesce(max(id), 1)) FROM watchlist_aliases;
SELECT setval(pg_get_serial_sequence('watchlist_evidence_documents', 'id'), coalesce(max(id), 1)) FROM watchlist_evidence_documents;


-- ==============================================================================
-- SYSTEM CONFIGURATION INITIAL DATA
-- -- ==============================================================================
-- SCREENING THRESHOLDS
INSERT INTO system_config (
    config_key, config_value, config_type, category,
    description, is_sensitive, created_at, updated_at, version
) VALUES
      ('SCREENING_THRESHOLD_FUZZY', '0.80', 'DOUBLE', 'SCREENING', 'Threshold for Jaro-Winkler similarity (0.0 to 1.0)', false, NOW(), NOW(), 0),
      ('SCREENING_THRESHOLD_MEDIUM', '31.0', 'DOUBLE', 'SCREENING', 'Score threshold to classify as MEDIUM risk', false, NOW(), NOW(), 0),
      ('SCREENING_THRESHOLD_HIGH', '61.0', 'DOUBLE', 'SCREENING', 'Score threshold to classify as HIGH risk', false, NOW(), NOW(), 0)
    ON CONFLICT (config_key) DO NOTHING;


-- SCREENING MULTIPLIERS
INSERT INTO system_config (
    config_key, config_value, config_type, category,
    description, is_sensitive, created_at, updated_at, version
) VALUES
      ('SCREENING_MULT_NAME_ONLY', '0.5', 'DOUBLE', 'SCREENING', 'Multiplier when only name matches', false, NOW(), NOW(), 0),
      ('SCREENING_MULT_NAME_ONE_ID', '0.8', 'DOUBLE', 'SCREENING', 'Multiplier when name and one ID match', false, NOW(), NOW(), 0),
      ('SCREENING_MULT_NAME_TWO_IDS', '1.0', 'DOUBLE', 'SCREENING', 'Multiplier when name and two IDs match', false, NOW(), NOW(), 0),
      ('SCREENING_MULT_NAME_ORG', '0.75', 'DOUBLE', 'SCREENING', 'Multiplier when name and organization match', false, NOW(), NOW(), 0),
      ('SCREENING_MULT_NAME_ORG_DESIGNATION', '1.0', 'DOUBLE', 'SCREENING', 'Multiplier when name, org, and designation match', false, NOW(), NOW(), 0)
    ON CONFLICT (config_key) DO NOTHING;


-- SCREENING BONUSES
INSERT INTO system_config (
    config_key, config_value, config_type, category,
    description, is_sensitive, created_at, updated_at, version
) VALUES
      ('SCREENING_BONUS_CRIMINAL', '15.0', 'DOUBLE', 'SCREENING', 'Bonus points for Criminal category matches', false, NOW(), NOW(), 0),
      ('SCREENING_BONUS_PEP', '15.0', 'DOUBLE', 'SCREENING', 'Bonus points for PEP category matches', false, NOW(), NOW(), 0),
      ('SCREENING_BONUS_SEVERITY_HIGH', '10.0', 'DOUBLE', 'SCREENING', 'Bonus points for High Severity entries', false, NOW(), NOW(), 0)
    ON CONFLICT (config_key) DO NOTHING;


-- FEATURE TOGGLES + STORAGE + SLA (merged into ONE clean query)
INSERT INTO system_config (
    config_key, config_value, config_type, category,
    description, is_sensitive, created_at, updated_at, version
) VALUES
      ('ACTIVE_SCREENING_STRATEGY', 'ADVANCED', 'STRING', 'SCREENING', 'Current active screening engine strategy (BASIC/ADVANCED)', false, NOW(), NOW(), 0),
      ('STORAGE_PRESIGNED_URL_TTL_MINUTES', '15', 'INTEGER', 'STORAGE', 'TTL for Cloudinary/S3 presigned URLs in minutes', false, NOW(), NOW(), 0),
      ('SLA_HOURS', '48', 'INTEGER', 'BUSINESS', 'SLA for officer to review a flagged screening', false, NOW(), NOW(), 0)
    ON CONFLICT (config_key) DO NOTHING;