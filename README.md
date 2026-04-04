# Hotel Management System

This is a Java desktop project for managing the day-to-day work of a hotel. It was built using Java, JavaFX, and MySQL. The system covers the main tasks you would expect in a small hotel setup, like handling reservations, rooms, payments, service requests, user accounts, reports, and backups.

The idea behind the project was to make the workflow simple and practical instead of overly complicated. Different users get access to different parts of the system depending on their role.

## What the system does

The system includes:

- user login with role-based access
- room and reservation management
- payment tracking
- service request handling
- report generation
- user account management
- audit logging
- database backup support

It also now supports password hashing using BCrypt, so passwords are no longer stored as plain text.

## User roles

There are three roles in the system:

### Admin
The admin has full access to the system. This includes managing users, viewing reports, and handling backups.
admin:admin_passoword123

### Manager
The manager can work with reservations, payments, service requests, and reports.

manager:manager_password123

### Receptionist
The receptionist can manage reservations, payments, service requests, and room searches.
reception:reception_password123


### Database Credentials
root:dacs

## Tools and technologies used

This project was built with:

- Java
- JavaFX (JavaFX is a framework used to build desktop user interfaces in Java)
- MySQL (MySQL is a relational database management system)
- JDBC (Java Database Connectivity)
- FXML (FXML is an XML-based format used to design JavaFX screens)
- BCrypt (Blowfish-based password hashing function)

## Project layout

```text
src/
├── controller/
├── db/
├── model/
├── security/
├── service/
└── view/