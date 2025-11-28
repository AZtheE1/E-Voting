-- Seed Elections
INSERT INTO election (title, start_date, end_date, status) VALUES ('National Election 2025', '2025-12-01', '2025-12-02', 'active');

-- Seed Constituencies
INSERT INTO constituency (name) VALUES ('Dhaka-1'), ('Dhaka-2'), ('Chittagong-1');

-- Seed Candidates (6 specific candidates with symbols)
INSERT INTO candidate (full_name, party_name, constituency_id, election_id, symbol) VALUES 
('Sheikh Hasina', 'Awami League', 1, 1, '/images/symbols/boat.png'),
('Khaleda Zia', 'BNP', 1, 1, '/images/symbols/paddy.png'),
('GM Quader', 'Jatiya Party', 1, 1, '/images/symbols/plough.png'),
('Dr. Yunus', 'Independent', 2, 1, '/images/symbols/ektara.png'),
('Barrister Andaleeve', 'BJP', 2, 1, '/images/symbols/lantern.png'),
('Mohiuddin Chowdhury', 'Awami League', 3, 1, '/images/symbols/boat.png');

-- Seed Voters (matching the user's NID format 19981234xx)
-- Using common password 'VOTER_SECRET' for all voters to enable NID-only login
INSERT INTO voter (full_name, nid_number, date_of_birth, gender, address, constituency_id, password) VALUES 
('Abdul Karim', '1998123401', '1985-03-12', 'Male', 'Mirpur, Dhaka', 1, 'VOTER_SECRET'),
('Farhana Akter', '1998123402', '1997-11-02', 'Female', 'Uttara, Dhaka', 1, 'VOTER_SECRET'),
('Rafiul Islam', '1998123403', '1990-05-25', 'Male', 'Dhanmondi, Dhaka', 1, 'VOTER_SECRET'),
('Jannatul Ferdous', '1998123404', '1998-08-18', 'Female', 'Banani, Dhaka', 1, 'VOTER_SECRET'),
('Nusrat Jahan', '1998123406', '1994-06-19', 'Female', 'Agrabad, Chittagong', 2, 'VOTER_SECRET');

-- Seed Admin
INSERT INTO admin (username, password) VALUES ('admin', 'admin123');
