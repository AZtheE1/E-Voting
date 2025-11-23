-- Flyway migration: add UNIQUE constraint to enforce one vote per voter per election
-- and add password columns to voter and admin for storing bcrypt hashes

-- Ensure we're using the correct database
USE e_voting;

-- Add unique constraint on vote(voter_id, election_id) if not already present
ALTER TABLE vote
  ADD CONSTRAINT uq_vote_voter_election UNIQUE (voter_id, election_id);

-- Add password columns (if not exists) so we can store bcrypt hashed passwords
-- Note: IF NOT EXISTS is supported in modern MySQL/Postgres versions. If your DB doesn't support it,
-- run these the first time only or adapt manually.

ALTER TABLE voter
  ADD COLUMN IF NOT EXISTS password VARCHAR(255);

ALTER TABLE admin
  ADD COLUMN IF NOT EXISTS password VARCHAR(255);

-- Optionally you can add other demo rows via application startup (we will seed via DataInitializer)
