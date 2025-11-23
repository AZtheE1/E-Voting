# E-Voting System - Local Setup Instructions

## Database Configuration

Your database `e_voting` has been configured in `application.properties`.

**Important:** Verify that your database tables have these columns:

### Table: `voter`
- `voter_id` (primary key)
- `full_name`
- `nid`
- `date_of_birth`
- `gender`

### Table: `candidate`
- `candidate_id` (primary key)
- `name`
- `party`
- `constituency_id` (foreign key)
- `election_id` (foreign key)

### Table: `vote`
- `vote_id` (primary key)
- `voter_id` (foreign key)
- `candidate_id` (foreign key)
- `election_id` (foreign key)

### Table: `election`
- `election_id` (primary key)
- (other columns as needed)

### Table: `constituency`
- `constituency_id` (primary key)
- (other columns as needed)

## Prerequisites

1. **Java 17 or higher** installed
   - Check: `java -version`

2. **Maven** installed
   - Check: `mvn --version`

3. **MySQL Server** running
   - Your database `e_voting` should be accessible
   - Default port: 3306

4. **MySQL Credentials**
   - Username: `root` (or update in `application.properties`)
   - Password: (update in `application.properties` if you have one)

## Step-by-Step Setup

### Step 1: Update Database Password (if needed)

Edit `src/main/resources/application.properties`:

```properties
app.datasource.password=your_mysql_password_here
```

If your MySQL root user has no password, leave it empty (already configured).

### Step 2: Download Dependencies

Open terminal/command prompt in the project directory and run:

```bash
mvn clean install
```

This will:
- Download all required JAR files (Spring Boot, MySQL connector, etc.)
- Compile the Java code
- Create the application JAR file

### Step 3: Run the Application

Option A - Using Maven:
```bash
mvn spring-boot:run
```

Option B - Using the JAR file:
```bash
mvn package
java -jar target/evoting-0.0.1-SNAPSHOT.jar
```

### Step 4: Access the Application

Open your web browser and go to:
```
http://localhost:8081
```

### Login Credentials
- **Voter Login**: Use any valid NID from your database (e.g., `1234567890`).
- **Admin Login**: (If configured) Use admin credentials.

## Troubleshooting

### Connection Error
If you get a database connection error:
1. Check MySQL is running: `mysql -u root -p`
2. Verify database name: `e_voting` (with underscore)
3. Check username/password in `application.properties`
4. Verify MySQL port is 3306

### Port Already in Use
If port 8081 is busy, change it in `application.properties`:
```properties
server.port=8082
```
