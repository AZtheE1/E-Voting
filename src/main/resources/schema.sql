CREATE TABLE IF NOT EXISTS election (
    election_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    status VARCHAR(50) DEFAULT 'upcoming'
);

CREATE TABLE IF NOT EXISTS constituency (
    constituency_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS candidate (
    candidate_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    party_name VARCHAR(255) NOT NULL,
    constituency_id BIGINT,
    election_id BIGINT,
    FOREIGN KEY (election_id) REFERENCES election(election_id)
);

CREATE TABLE IF NOT EXISTS voter (
    voter_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    nid_number VARCHAR(50) UNIQUE NOT NULL,
    date_of_birth DATE,
    gender VARCHAR(20),
    address VARCHAR(255),
    constituency_id BIGINT,
    password VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS vote (
    vote_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    voter_id BIGINT,
    candidate_id BIGINT,
    election_id BIGINT,
    FOREIGN KEY (voter_id) REFERENCES voter(voter_id),
    FOREIGN KEY (candidate_id) REFERENCES candidate(candidate_id),
    FOREIGN KEY (election_id) REFERENCES election(election_id),
    UNIQUE(voter_id, election_id)
);

CREATE TABLE IF NOT EXISTS admin (
    admin_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);
