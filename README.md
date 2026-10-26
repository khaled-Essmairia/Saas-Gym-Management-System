# 🏋️ Gym Management System

A full-stack gym management web application built with **Spring Boot**, **Spring Security** and **Thymeleaf**. It handles members, staff, payments, attendance, equipment and reporting, with separate dashboards for admins, staff and customers.

## ✨ Features

- **Role-based access** – Admin, Staff and Customer portals with dedicated dashboards
- **Member management** – registration, status tracking, activation and deactivation
- **Payments** – payment recording, printable receipts, deactivated-member tracking
- **Attendance** – member check-in and attendance history
- **Equipment inventory** – add, edit and list gym equipment
- **Progress tracking** – record and view member progress
- **Announcements** – admins publish announcements to customers
- **Customer tools** – to-do list, reminders and personal reports
- **Reports and charts** – member and progress reports with visual statistics
- **License activation** – built-in license validation system
- **License tied to a computer** – a key made with the generator's *Computer ID* field only works on the computer that shows that Machine ID (License Management page); set `gym.license.require-machine-id=true` to refuse keys without one
- **CSV import** – *Import Members (CSV)* and *Import Equipment (CSV)* in the admin sidebar, each with a template download; the whole file is checked first and nothing is saved if a line has a problem
- **Reporting** – export the members / staff / attendance data and every database table (one CSV, or all tables in one ZIP); password columns are never exported
- **System Administration > Admin Profile** – the logged-in admin changes his password (old password, new password, confirmation); old MD5 passwords are accepted as the old password and replaced by a BCrypt hash
- **Manage Services** – add, update and remove the services (Fitness, Sauna ...) and their monthly price; renaming a service renames it for its members, and a service that still has members cannot be removed
- **Legacy-compatible authentication** – password encoder that supports migrating older hashes

## 🛠️ Tech Stack

| Layer      | Technology                              |
|------------|-----------------------------------------|
| Backend    | Java, Spring Boot, Spring Data JPA      |
| Security   | Spring Security                         |
| Frontend   | Thymeleaf, Bootstrap, jQuery, Font Awesome |
| Database   | MySQL                                   |
| Build tool | Maven                                   |
| Testing    | JUnit                                   |

## 📁 Project Structure

```
src/main/java/com/gym
├── config        # Security, web and license configuration
├── controller    # Admin, Auth, Customer, Staff, Management, License
├── entity        # JPA entities
├── repository    # Spring Data repositories
├── security      # UserDetailsServices and password encoder
├── service       # Business logic
└── web           # Form objects and global model advice
src/main/resources
├── db            # SQL schema scripts
├── static        # CSS, JS, images, fonts
└── templates     # Thymeleaf views
```

## 🚀 Getting Started

### Prerequisites
- JDK 17+ (match the version in `pom.xml`)
- Maven 3.8+
- MySQL 8+

### Installation

```bash
git clone <YOUR_REPO_URL>
cd gym-system-springboot
```

1. Create the database and import the schema:
```sql
   CREATE DATABASE gymnsb;
```
   Then run `src/main/resources/db/gymnsb-original.sql` followed by `schema-fix.sql`.

2. Configure `src/main/resources/application.properties`:
```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/gymnsb
   spring.datasource.username=YOUR_USER
   spring.datasource.password=YOUR_PASSWORD
```

3. Run the app:
```bash
   mvn spring-boot:run
```

4. Open http://localhost:8080 and sign in.

### Run tests
```bash
mvn test
```

## 👥 Roles

| Role     | Access                                                        |
|----------|---------------------------------------------------------------|
| Admin    | Staff, announcements, reports, attendance, license management |
| Staff    | Members, payments, equipment, check-in                        |
| Customer | Personal dashboard, to-do, reminders, announcements, reports  |

## 📸 Screenshots

_Add screenshots here (e.g. `docs/screenshots/dashboard.png`)._

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feat/my-feature`
3. Commit your changes: `git commit -m "feat: add my feature"`
4. Push and open a Pull Request

## 📄 License

Distributed under the MIT License. See [`LICENSE`](LICENSE) for details.