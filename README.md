# Learning Management System (LMS)

The LMS (Learning Management System) is a web-based platform for managing and organizing online courses and assessments. It caters to the needs of different user roles, including Admins, Instructors, and Students, by offering specific functionalities like user management, course management, assessments, notifications, and performance tracking.

---

## Table of Contents
- [Features](#features)
  - [User Management](#user-management)
  - [Course Management](#course-management)
  - [Assessments and Grading](#assessments-and-grading)
  - [Performance Tracking](#performance-tracking)
  - [Notifications](#notifications)
  - [Bonus Features](#bonus-features)
- [Technologies Used](#technologies-used)
- [Setup and Installation](#setup-and-installation)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

---

## Features

### User Management
- **User Roles**: Admin, Instructor, Student
- **Admin Features**:
  - Manage overall system settings.
  - Create and manage users.
  - Manage courses.
- **Instructor Features**:
  - Create courses and upload content (e.g., videos, PDFs, audio).
  - Add quizzes, assignments, and grades.
  - Manage enrolled students and attendance.
- **Student Features**:
  - Enroll in courses, access materials, and submit assignments.
  - View grades for quizzes and assignments.
  - Track personal progress and notifications.

### Course Management
- **Course Creation**:
  - Create courses with details like title, description, and duration.
  - Organize courses into lessons with media files (videos, PDFs, etc.).
- **Enrollment Management**:
  - Students can browse and enroll in available courses.
  - Admins and Instructors can monitor enrollments.
- **Attendance Management**:
  - OTP-based lesson attendance for validation.

### Assessments and Grading
- **Quiz Creation**:
  - Instructors create quizzes with multiple question types (MCQ, true/false, short answers).
  - Maintain a question bank for each course.
  - Support randomized question selection.
- **Assignments**:
  - Students upload assignment submissions.
  - Instructors review and provide manual grading.
- **Feedback**:
  - Automated feedback for quizzes and manual feedback for assignments.

### Performance Tracking
- Track student progress, including quiz scores, assignments, and attendance.
- Monitor progress via admin/instructor dashboards.

### Notifications
- **System Notifications**:
  - Notifications for course updates, grades, and enrollment confirmations.
  - Options to view unread or all notifications.
- **Email Notifications**:
  - Send email updates for important activities.

### Bonus Features
- **Role-Based Access Control**:
  - Authentication and authorization with Spring Security.
  - Permissions restricted based on user roles.
- **Performance Analytics**:
  - Generate Excel reports for grades and attendance.
  - Visual representations (charts) of progress and performance.
- **Email Notifications**:
  - Configurable email alerts for course activities and updates.

---

## Technologies Used
- **Backend**: Spring Boot
- **Database**: MySQL
- **Security**: Spring Security for authentication and authorization.
- **Other Libraries**: Excel reporting library for data export, charting libraries for visual analytics.

---

## Setup and Installation

### Prerequisites
- Install Java JDK 11 or higher.
- Install MySQL Server.
- Gradle (for dependency management).

### Steps to Install
1. Clone the repository:
   ```bash
   git clone https://github.com/your-repository-name.git
   ```
2. Build the Project:
To build and run the project using Gradle, you can use the following commands:
```bash
./gradlew build
```
Run the Application:
```bash
./gradlew bootRun
```
Make sure you have Gradle installed or use the provided Gradle wrapper (./gradlew for Linux/Mac or gradlew.bat for Windows).
