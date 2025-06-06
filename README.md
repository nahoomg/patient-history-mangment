# Hospital Management System

A JavaFX application for managing patient records and medical history, accessible by doctors.

## Features

- Doctor authentication system
- Patient management (Add, View, Update)
- Medical history tracking
- User-friendly interface
- SQLite database for data persistence

## Prerequisites

- Java Development Kit (JDK) 17 or later
- Maven 3.6 or later

## Setup

1. Clone the repository
2. Navigate to the project directory
3. Run the following Maven command to build the project:
```bash
mvn clean install
```

## Running the Application

To run the application, use the following command:
```bash
mvn javafx:run
```

## First Time Setup

When you first run the application, you'll need to create a doctor account. You can do this by running the following SQL commands in your SQLite database:

```sql
INSERT INTO doctors (firstName, lastName, specialization, contactNumber, email, username, password)
VALUES ('John', 'Doe', 'General Medicine', '123-456-7890', 'john.doe@hospital.com', 'jdoe', 'password123');
```

You can use these credentials to log in:
- Username: jdoe
- Password: password123

## Database

The application uses SQLite for data storage. The database file `hospital.db` will be created automatically in the project root directory when you first run the application.

## Project Structure

```
src/
├── main/
│   └── java/
│       └── com/
│           └── hospital/
│               ├── Main.java
│               ├── models/
│               │   ├── Doctor.java
│               │   └── Patient.java
│               └── utils/
│                   └── DatabaseManager.java
``` #   p a t i e n t - h i s t o r y - m a n g m e n t  
 