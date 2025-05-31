# Web Fitness Tracker

A comprehensive web-based fitness tracking application built with Spring Boot that helps users monitor their fitness activities, set goals, and track progress.

## 🚀 Features

### Core Functionality
- **User Management**: Registration, authentication 
- **Workout Tracking**: Log and monitor various types of exercises with detailed metrics
- **Goal Setting & Monitoring**: Create, track, and manage fitness goals with progress updates
- **Advanced Statistics**: Comprehensive analytics with filtering by date, type, and period
- **Administrative Panel**: Full admin control over users, workouts, and goals
- **Data Export**: Export statistics and workout data in CSV format
- **Secure Access**: Role-based authentication and authorization (USER/ADMIN)

### Technical Features
- RESTful API architecture with comprehensive endpoints
- Responsive web interface with Thymeleaf templates
- Database persistence with JPA
- Advanced security implementation with role-based access
- Form handling and validation with custom exceptions
- Real-time progress tracking and goal updates
- Statistical analysis with multiple visualization options

## 🛠️ Technology Stack

- **Backend**: Spring Boot 3.5.0
- **Web Framework**: Spring MVC
- **Template Engine**: Thymeleaf
- **Security**: Spring Security with role-based access control
- **Database**: Spring Data JPA with H2 Database
- **Build Tool**: Apache Maven
- **Validation**: Apache Commons Validator
- **Java Version**: 21

## 📋 Prerequisites

- Java JDK 17 or higher
- Apache Maven 3.6+
- Database H2

## 🔧 Installation & Setup

1. **Clone the repository**
```bash
git clone https://github.com/Maksym-Kovbasa/Web_Fitness_Tracker.git
```

2. **Navigate to project directory**
```bash
cd Web_Fitness_Tracker
```

3. **Configure database settings**
   - Update `application.properties` with your database credentials

4. **Build the project**
```bash
mvn clean install
```

5. **Run the application**
```bash
mvn spring-boot:run
```

6. **Access the application**
   - Open browser and go to `http://localhost:8080`

## 📁 Project Structure

```
Web_Fitness_Tracker/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/fitness/tracker/
│   │   │       ├── Config/         # Configuration classes
│   │   │       ├── Controller/     # REST controllers
|   |   |       ├── Exception/      # User-defined exceptions for clear output
│   │   │       ├── Service/        # Business logic
│   │   │       ├── Repository/     # Data access layer
│   │   │       ├── Model/          # Entity classes
│   │   │       └── Application.java
│   │   └── resources/
│   │       ├── static/             # CSS, fon, background image
│   │       ├── templates/          # Thymeleaf templates
│   │       └── application.properties
│   └── test/                       # Unit tests (Maybe in the future there will be)
├── pom.xml
├── LICENSE
└── README.md
```

## 🔐 Security Features

- User authentication and authorization
- Role-based access control (USER/ADMIN)
- Password encryption with BCryptPasswordEncoder
- Session management
- CSRF protection
- Secure endpoints with @PreAuthorize annotations
- Access is only for administrators for full control

## 👥 User Roles

### Regular Users (USER)
- Create and manage personal workouts
- Set and track fitness goals
- View personal statistics and progress
- Export personal data

### Administrators (ADMIN)
- Full system overview and management
- User management (create, edit, delete users)
- Goal management across all users
- Workout management for all users
- Bulk operations (delete all user goals, etc.)

## 📊 API Endpoints

### Authentication & Web Interface
- `GET /` - Home page
- `GET /login` - Login page
- `GET /register` - Registration page
- `POST /logout` - User logout

### Authentication
- `POST /api/users/login` - User login
- `POST /api/users/register` - User registration
- `POST /api/users/logout` - User logout

### User Workout Management
- `GET /api/workouts` - Get user workouts
- `GET /api/workouts/{id}` - Get specific workout
- `GET /api/workouts/type/{type}` - Get workouts by type (example type/Running)
- `GET /api/workouts/date/{date}` - Get workouts by date (example date/2025-05-30)
- `POST /api/workouts` - Create new workout
- `PUT /api/workouts/{id}` - Update workout
- `DELETE /api/workouts/{id}` - Delete workout

### Goal Management
- `GET /api/goals` - Get user goals
- `GET /api/goals/{id}` - Get specific goal
- `POST /api/goals` - Create new goal
- `PUT /api/goals/{id}` - Update goal
- `DELETE /api/goals/{id}` - Delete goal
- `GET /api/goals/filter?status={status}` - Filter goals by status (Completed, In Progress, Not Started, Failed)

### Statistics & Analytics
- `GET /api/stats` - General user statistics
- `GET /api/stats/workouts/by-type` - Workout statistics grouped by type
- `GET /api/stats/progress/calories` - Calories progress with period filtering (daily/weekly/monthly) (example /calories?startDate=2025-04-01&endDate=2025-07-31&period=daily")
- `GET /api/stats/workouts/by-period` - Workout statistics for specific date range (example /by-period?startDate=2025-04-01&endDate=2025-07-31)


### Web Interface
- `GET /` - Home page
- `GET /login` - Login page
- `GET /register` - Registration page
- `GET /web/workouts` - Workouts management page
- `GET /web/goals` - Goals management page
- `GET /web/goals/filter` - Filter goals by status
- `GET /web/stats` - Statistics dashboard
- `GET /web/stats/export` - Export statistics as CSV


### Admin API Endpoints (ADMIN role required)
- `GET /api/admin/users` - Get all users
- `GET /api/admin/users/{id}` - Get specific user
- `PUT /api/admin/users/{id}` - Update user
- `DELETE /api/admin/users/{id}` - Delete user
- `DELETE /api/admin/users/{id}/goals` - Delete all user goals

- `GET /api/admin/goals` - Get all goals
- `GET /api/admin/goals/{id}` - Get specific goal
- `PUT /api/admin/goals/{id}` - Update goal
- `DELETE /api/admin/goals/{id}` - Delete goal

- `GET /api/admin/workouts` - Get all workouts
- `GET /api/admin/workouts/{id}` - Get specific workout
- `POST /api/admin/workouts` - Create workout
- `PUT /api/admin/workouts/{id}` - Update workout
- `DELETE /api/admin/workouts/{id}` - Delete workout

- `GET /api/admin/stats/overview` - System overview statistics
- `GET /api/admin/stats/users` - Detailed user statistics

## 🎯 Key Features Breakdown

### Workout Management
- **Types**: Support for various workout types (Running, Cycling, Swimming, etc.)
- **Metrics**: Duration (minutes), Calories burned, Date tracking
- **Filtering**: Filter by workout type, date, or date range
- **Validation**: Comprehensive data validation with custom exceptions

### Goal Setting & Tracking
- **Target Metrics**: Target calories and target number of workouts
- **Date Range**: Start and end date for goal periods
- **Status Tracking**: Automatic status updates (Not Started, In Progress, Completed, Failed)
- **Progress Monitoring**: Real-time progress updates when workouts are added/updated/deleted
- **Filtering**: Filter goals by status

### Statistics & Analytics
- **General Stats**: Total workouts, duration, calories, averages
- **Type Analysis**: Statistics grouped by workout type
- **Progress Tracking**: Daily, weekly, and monthly progress views
- **Period Filtering**: Custom date range analysis
- **Data Export**: CSV export functionality for external analysis


### Administrative Features
- **User Management**: Complete CRUD operations for user accounts (without create)
- **Data Oversight**: View and manage all users' workouts and goals
- **Bulk Operations**: Delete all goals for specific users
- **System Statistics**: Overview of total users, goals, workouts
- **Role Management**: Assign and modify user roles
- **Security**: Prevent admins from deleting their own accounts

## 🎯 Usage

### For Regular Users
1. **Register/Login**: Create an account or login with existing credentials
2. **Set Goals**: Define fitness objectives with target calories and workouts
3. **Log Workouts**: Record exercise sessions with type, duration, and calories
4. **Track Progress**: Monitor goal achievement and view statistics
5. **Analyze Data**: Use filtering and export features for detailed analysis

### For Administrators
1. **Access Admin Panel**: Navigate to `/web/admin` (requires ADMIN role)
2. **Manage Users**: Edit, or delete user accounts
3. **Oversee Goals**: Monitor and modify user goals across the system
4. **Workout Management**: View and manage all workout entries
5. **System Analytics**: Access comprehensive system statistics

## 🧪 Testing

Run tests using Maven:
```bash
mvn test
```

## 📝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/new-feature`)
3. Commit changes (`git commit -am 'Add new feature'`)
4. Push to branch (`git push origin feature/new-feature`)
5. Create Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👨‍💻 Author

**Maksym Kovbasa**
- GitHub: [@Maksym-Kovbasa](https://github.com/Maksym-Kovbasa)

## 🔗 References

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/)
- [Spring Security Guide](https://spring.io/guides/gs/securing-web/)
- [Thymeleaf Documentation](https://www.thymeleaf.org/documentation.html)
- [Spring Data JPA Guide](https://spring.io/guides/gs/accessing-data-jpa/)
- [Spring Boot @ControllerAdvice](https://zetcode.com/springboot/controlleradvice/)