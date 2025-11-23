-- Seed Elections
INSERT INTO election (title, status) VALUES ('National Election 2025', 'active');

-- Seed Constituencies
INSERT INTO constituency (name) VALUES ('Dhaka-1'), ('Dhaka-2'), ('Chittagong-1');

-- Seed Candidates
INSERT INTO candidate (full_name, party_name, constituency_id, election_id) VALUES 
('Sheikh Hasina', 'Awami League', 1, 1),
('Khaleda Zia', 'BNP', 1, 1),
('GM Quader', 'Jatiya Party', 1, 1),
('Dr. Yunus', 'Independent', 2, 1),
('Barrister Andaleeve', 'BJP', 2, 1),
('Mohiuddin Chowdhury', 'Awami League', 3, 1);

-- Seed Voters (matching the user's NID format 19981234xx)
INSERT INTO voter (full_name, nid_number, date_of_birth, gender, address, constituency_id, password) VALUES 
('Abdul Karim', '1998123401', '1985-03-12', 'Male', 'Mirpur, Dhaka', 1, 'password'),
('Farhana Akter', '1998123402', '1997-11-02', 'Female', 'Uttara, Dhaka', 1, 'password'),
('Rafiul Islam', '1998123403', '1990-05-25', 'Male', 'Dhanmondi, Dhaka', 1, 'password'),
('Jannatul Ferdous', '1998123404', '1998-08-18', 'Female', 'Banani, Dhaka', 1, 'password'),
('Nusrat Jahan', '1998123406', '1994-06-19', 'Female', 'Agrabad, Chittagong', 2, 'password');

-- Seed Admin
INSERT INTO admin (username, password) VALUES ('admin', 'admin123');
