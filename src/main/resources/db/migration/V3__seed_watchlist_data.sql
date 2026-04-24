-- 1. Seed Categories
INSERT INTO watchlist_categories (id, category_code, category_name, description, base_score_multiplier, is_active, created_at)
VALUES
(1, 'SANCTIONS_LIST', 'Global Sanctions', 'UN, OFAC and international sanctions', 1.0, true, CURRENT_TIMESTAMP),
(2, 'FRAUD_REGISTRY', 'Known Fraudsters', 'Financial and cyber fraud offenders', 0.9, true, CURRENT_TIMESTAMP),
(3, 'CRIMINAL_RECORDS', 'Criminal Records', 'Individuals with criminal background', 0.95, true, CURRENT_TIMESTAMP),
(4, 'PEP_LIST', 'Politically Exposed Persons', 'Politicians and high-risk individuals', 0.7, true, CURRENT_TIMESTAMP),
(5, 'DEBARRED_PROFESSIONALS', 'Debarred Professionals', 'Banned professionals', 0.85, true, CURRENT_TIMESTAMP),
(6, 'BLACKLISTED_VENDORS', 'Blacklisted Vendors', 'Vendors banned due to misconduct', 0.8, true, CURRENT_TIMESTAMP),
(7, 'EMPLOYMENT_FRAUD', 'Employment Fraud', 'Fake employment or HR scams', 0.75, true, CURRENT_TIMESTAMP);


-- 2. Seed Watchlist Entries (Notice the JSONB format)
INSERT INTO watchlist_entries
(id, category_id, primary_name, primary_name_normalized, severity, pan_number, nationality, category_specific_data, is_active, created_at)
VALUES

-- Global / Known
(1001, 1, 'Osama Bin Laden', 'OSAMABINLADEN', 'HIGH', NULL, 'Afghan', '{"program":"Al-Qaida"}', true, CURRENT_TIMESTAMP),
(1002, 2, 'Jordan Belfort', 'JORDANBELFORT', 'HIGH', 'ABCDE1234F', 'American', '{"crime":"Securities Fraud"}', true, CURRENT_TIMESTAMP),

-- Indian High Profile
(1003, 2, 'Vijay Mallya', 'VIJAYMALLYA', 'HIGH', 'AAACV1234A', 'Indian', '{"case":"Bank Fraud"}', true, CURRENT_TIMESTAMP),
(1004, 2, 'Nirav Modi', 'NIRAVMODI', 'HIGH', 'AAACN2345B', 'Indian', '{"case":"PNB Scam"}', true, CURRENT_TIMESTAMP),
(1005, 2, 'Mehul Choksi', 'MEHULCHOKSI', 'HIGH', 'AAACM6789D', 'Indian', '{"case":"Bank Fraud"}', true, CURRENT_TIMESTAMP),

-- USER PROVIDED NAMES
(1006, 3, 'Smit Karbathiya', 'SMITKARBATHIYA', 'HIGH', NULL, 'Indian', '{"type":"Criminal"}', true, CURRENT_TIMESTAMP),
(1007, 2, 'Divayrajsinh Sindhav', 'DIVAYRAJSINHSINDHAV', 'HIGH', NULL, 'Indian', '{"type":"Cyber Fraud"}', true, CURRENT_TIMESTAMP),
(1008, 5, 'Neel Chhantbar', 'NEELCHHANTBAR', 'MEDIUM', NULL, 'Indian', '{"status":"Debarred Professional"}', true, CURRENT_TIMESTAMP),
(1009, 4, 'Vivek Dadhaniya', 'VIVEKDADHANIYA', 'MEDIUM', NULL, 'Indian', '{"role":"PEP"}', true, CURRENT_TIMESTAMP),
(1010, 3, 'Tanmay Jotangia', 'TANMAYJOTANGIA', 'HIGH', NULL, 'Indian', '{"record":"Criminal"}', true, CURRENT_TIMESTAMP),
(1011, 6, 'Rahul Dave', 'RAHULDAVE', 'MEDIUM', NULL, 'Indian', '{"status":"Blacklisted Vendor"}', true, CURRENT_TIMESTAMP),
(1012, 7, 'Bhavika Chhatbar', 'BHAVIKACHHATBAR', 'MEDIUM', NULL, 'Indian', '{"type":"Employment Fraud"}', true, CURRENT_TIMESTAMP),
(1013, 4, 'Darshit Dhaduk', 'DARSHITDHADUK', 'LOW', NULL, 'Indian', '{"role":"PEP"}', true, CURRENT_TIMESTAMP),
(1014, 3, 'Vrunda Chavda', 'VRUNDACHAVDA', 'MEDIUM', NULL, 'Indian', '{"note":"Under investigation"}', true, CURRENT_TIMESTAMP),

-- Additional Indian Data
(1015, 2, 'Rakesh Jhunjhunwala Fraud Case Ref', 'RAKESHJHUNJHUNWALA_REF', 'LOW', NULL, 'Indian', '{"note":"Reference flagged entity"}', true, CURRENT_TIMESTAMP),
(1016, 3, 'Amit Kumar', 'AMITKUMAR', 'HIGH', NULL, 'Indian', '{"crime":"Kidney Racket"}', true, CURRENT_TIMESTAMP),
(1017, 2, 'Sachin Waze', 'SACHINWAZE', 'HIGH', NULL, 'Indian', '{"case":"Extortion"}', true, CURRENT_TIMESTAMP),
(1018, 3, 'Abu Salem', 'ABUSALEM', 'HIGH', NULL, 'Indian', '{"crime":"Organized Crime"}', true, CURRENT_TIMESTAMP),
(1019, 3, 'Chhota Rajan', 'CHHOTARAJAN', 'HIGH', NULL, 'Indian', '{"crime":"Underworld"}', true, CURRENT_TIMESTAMP),

-- Vendors / Fraud / Risk
(1020, 6, 'Shree Traders Pvt Ltd', 'SHREETRADERS', 'MEDIUM', NULL, 'Indian', '{"issue":"Fake billing"}', true, CURRENT_TIMESTAMP),
(1021, 7, 'QuickHire Solutions', 'QUICKHIRESOLUTIONS', 'MEDIUM', NULL, 'Indian', '{"issue":"Job Scam"}', true, CURRENT_TIMESTAMP),

-- PEP
(1022, 4, 'Lalu Prasad Yadav', 'LALUPRASADYADAV', 'MEDIUM', NULL, 'Indian', '{"position":"Politician"}', true, CURRENT_TIMESTAMP),
(1023, 4, 'Sharad Pawar', 'SHARADPAWAR', 'LOW', NULL, 'Indian', '{"position":"Politician"}', true, CURRENT_TIMESTAMP),

-- Cyber / Fraud
(1024, 2, 'Naresh Goyal Case Ref', 'NARESHGOYALREF', 'HIGH', NULL, 'Indian', '{"case":"Financial Fraud"}', true, CURRENT_TIMESTAMP),
(1025, 2, 'Ketan Parekh', 'KETANPAREKH', 'HIGH', NULL, 'Indian', '{"case":"Stock Market Scam"}', true, CURRENT_TIMESTAMP),

-- Misc Additional
(1026, 3, 'Ravi Pujari', 'RAVIPUJARI', 'HIGH', NULL, 'Indian', '{"crime":"Extortion"}', true, CURRENT_TIMESTAMP),
(1027, 5, 'Suresh Accountant', 'SURESHACCOUNTANT', 'MEDIUM', NULL, 'Indian', '{"status":"License Revoked"}', true, CURRENT_TIMESTAMP),
(1028, 6, 'Global Infra Vendor', 'GLOBALINFRAVENDOR', 'LOW', NULL, 'Indian', '{"issue":"Contract Violation"}', true, CURRENT_TIMESTAMP),
(1029, 7, 'JobFast India', 'JOBFASTINDIA', 'MEDIUM', NULL, 'Indian', '{"issue":"Recruitment Scam"}', true, CURRENT_TIMESTAMP),
(1030, 3, 'Deepak Boxer', 'DEEPAKBOXER', 'HIGH', NULL, 'Indian', '{"crime":"Organized Crime"}', true, CURRENT_TIMESTAMP);


-- 3. Seed Aliases
INSERT INTO watchlist_aliases (id, entry_id, alias_name, alias_name_normalized, alias_type, created_at)
VALUES
(1, 1002, 'The Wolf of Wall Street', 'THEWOLFOFWALLSTREET', 'AKA', CURRENT_TIMESTAMP),
(2, 1018, 'Abu S', 'ABUS', 'SHORT_NAME', CURRENT_TIMESTAMP),
(3, 1019, 'Rajan N', 'RAJANN', 'SHORT_NAME', CURRENT_TIMESTAMP);