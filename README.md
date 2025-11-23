# E-Voting System

This is a simple **E-Voting System** built with **Spring Boot**, **JDBC**, and **Thymeleaf**. The application allows users to vote in elections, check election results, and provides an **admin dashboard** to manage voters, candidates, and elections.

## Features

- **Voter Dashboard**:
  - View available elections and candidates.
  - Cast votes in elections.
  - View personal voting status.
  
- **Admin Dashboard**:
  - Add, remove, and view elections.
  - Manage candidates: Add and remove candidates for elections.
  - Manage voters: Add and remove voters.
  
- **Election Results**:
  - View vote counts for each candidate.
  - View results by constituency.
  
- **Database Integration**:
  - SQL queries for retrieving and updating election data.
  - Full database interaction using **JDBC** and **NamedParameterJdbcTemplate**.

## Technologies Used

- **Backend**: Java, Spring Boot
- **Frontend**: HTML, Thymeleaf
- **Database**: MySQL (for storing voters, candidates, and election data)
- **SQL Queries**: Direct SQL queries for database operations
- **Build Tool**: Maven

## Prerequisites

Before you begin, ensure you have met the following requirements:

- **Java 17+**: You need to have Java 17 or higher installed on your machine.
- **Maven**: Maven should be installed for building and running the project.
- **MySQL Database**: A MySQL database should be set up to store the voting data.

### Setting Up the MySQL Database

1. **Create a database** in MySQL (e.g., `evoting`).

2. **Import the schema** for creating the necessary tables (`elections`, `candidates`, `voters`, `votes`).
   
   Example schema:
   ```sql
   CREATE DATABASE evoting;

   USE evoting;

   CREATE TABLE elections (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       title VARCHAR(255) NOT NULL,
       status VARCHAR(50) NOT NULL
   );

   CREATE TABLE candidates (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       full_name VARCHAR(255) NOT NULL,
       party_name VARCHAR(255),
       constituency_id BIGINT NOT NULL,
       election_id BIGINT NOT NULL,
       FOREIGN KEY (election_id) REFERENCES elections(id)
   );

   CREATE TABLE voters (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       full_name VARCHAR(255) NOT NULL,
       voter_id VARCHAR(255) NOT NULL,
       nid_number VARCHAR(255),
       gender VARCHAR(50),
       date_of_birth DATE,
       password VARCHAR(255)
   );

   CREATE TABLE votes (
       vote_id BIGINT AUTO_INCREMENT PRIMARY KEY,
       election_id BIGINT NOT NULL,
       voter_id BIGINT NOT NULL,
       candidate_id BIGINT NOT NULL,
       FOREIGN KEY (election_id) REFERENCES elections(id),
       FOREIGN KEY (voter_id) REFERENCES voters(id),
       FOREIGN KEY (candidate_id) REFERENCES candidates(id)
   );
