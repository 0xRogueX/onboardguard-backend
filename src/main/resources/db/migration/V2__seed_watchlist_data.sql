-- 1. Seed Categories
INSERT INTO watchlist_categories (id, category_code, category_name, description, base_score_multiplier, is_active, created_at)
VALUES
(1, 'SANCTIONS_LIST', 'Global Sanctions', 'UN and OFAC Sanctions', 1.0, true, CURRENT_TIMESTAMP),
(2, 'FRAUD_REGISTRY', 'Known Fraudsters', 'Financial crimes', 0.8, true, CURRENT_TIMESTAMP);

-- 2. Seed Watchlist Entries (Notice the JSONB format)
INSERT INTO watchlist_entries (id, category_id, primary_name, primary_name_normalized, severity, pan_number, category_specific_data, is_active, created_at)
VALUES
(1001, 1, 'Osama Bin Laden', 'OSAMABINLADEN', 'CRITICAL', NULL, '{"country": "Afghanistan", "program": "Al-Qaida"}', true, CURRENT_TIMESTAMP),
(1002, 2, 'Jordan Belfort', 'JORDANBELFORT', 'HIGH', 'ABCDE1234F', '{"crime": "Securities Fraud"}', true, CURRENT_TIMESTAMP);

-- 3. Seed Aliases
INSERT INTO watchlist_aliases (id, entry_id, alias_name, alias_name_normalized, alias_type, created_at)
VALUES
(1, 1002, 'The Wolf of Wall Street', 'THEWOLFOFWALLSTREET', 'AKA', CURRENT_TIMESTAMP);