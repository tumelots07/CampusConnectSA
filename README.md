# CampusConnect SA – Smart Student Life Assistant

## 1. Project Overview

CampusConnect SA is a mobile application developed by our group to make student life easier and more organised. The application brings useful student services and information together in one place.

Students can use the application to manage academic activities, access study resources, view announcements, receive notifications and find information about campus services, transport and safety.

Our aim is to make it easier for students to access important information without having to use different platforms for every service.

## 2. Problem Statement

Students often struggle to keep track of academic activities, campus announcements, study materials and other important student information. When this information is spread across different platforms, it can become difficult to manage daily responsibilities.

CampusConnect SA aims to address this problem by providing a central platform where students can access useful academic and campus-related services.

## 3. Technologies Used

The project uses the following technologies:

| Technology | Purpose |
|---|---|
| Kotlin | Android application development |
| XML | Designing application layouts |
| Android Studio | Development environment |
| Node.js | Backend development |
| Express.js | Backend API |
| MongoDB Atlas | Database management |
| Firebase Authentication | User authentication integration |
| Retrofit | Communication between Android and backend |
| Git and GitHub | Version control and project collaboration |

## 4. Main Application Features

The application includes the following sections:

- **User Registration and Login:** Allows students to register and access the application.
- **Dashboard:** Provides access to the main application sections.
- **Academic Planner:** Helps students organise their academic tasks.
- **Study Resources:** Provides access to learning materials.
- **Announcements:** Displays important student and campus updates.
- **Notifications:** Provides a section for student notifications.
- **Campus Services:** Gives students information about available campus services.
- **Transport:** Provides transport-related information.
- **Safety:** Includes a section for campus safety and incident reporting.
- **Settings:** Provides application settings.

## 5. System Architecture

CampusConnect SA consists of three main components:

1. **Android Application:** The user interface developed using Kotlin and XML.
2. **Backend API:** A Node.js and Express.js application that handles requests from the Android application.
3. **Database:** MongoDB Atlas stores the application's data.

The Android application communicates with the backend using API requests through Retrofit. The backend processes these requests and communicates with the database.

## 6. Project Folder Structure

```text
CampusConnectSA/
│
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── res/
│   └── build.gradle.kts
│
├── backend/
│   ├── config/
│   ├── middleware/
│   ├── models/
│   ├── routes/
│   └── server.js
│
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
├── package.json
├── package-lock.json
└── README.md
```

## 7. Installation and Setup

### Requirements

Before running the project, make sure the following software is installed:

- Android Studio
- JDK compatible with the project's Gradle configuration
- Node.js and npm
- Git
- A MongoDB Atlas database

### Step 1: Clone the Repository

Open a terminal and run:

```bash
git clone https://github.com/YOUR_USERNAME/CampusConnectSA.git
```

Open the downloaded `CampusConnectSA` folder in Android Studio.

Replace `YOUR_USERNAME` with the GitHub account that hosts the project.

### Step 2: Set Up the Backend

Open PowerShell or a terminal in the main project directory.

Install the Node.js dependencies:

```bash
npm install
```

Create a `.env` file inside the `backend` folder.

Add the environment variables required by the backend configuration, including the MongoDB connection string and authentication settings. Use the group's private configuration values.

**Important:** The `.env` file and Firebase Admin service-account credentials must not be uploaded to GitHub.

Start the backend from the main project directory:

```bash
node backend/server.js
```

The backend should start on its configured port.

### Step 3: Configure the Android Application

1. Open the project in Android Studio.
2. Allow Gradle to sync.
3. Make sure the Firebase Android configuration file is available at `app/google-services.json`.
4. Check that the API base URL in the Android application points to the correct backend address.
5. Connect an Android device or start an emulator.
6. Build and run the application.

For a physical Android device, the phone and computer must be able to communicate over the network when using a locally hosted backend.

## 8. Database

The project uses MongoDB Atlas as its database.

The backend includes database models for:

- Users
- Academic tasks
- Announcements
- Campus services
- Incident reports
- Notifications
- Resources
- Transport information

The MongoDB connection details are stored in the backend's private environment configuration.

## 9. Testing

The application should be tested to check that its main features work as expected.

Our testing activities include checking:

- User registration and login.
- Navigation between application screens.
- Academic task management.
- Access to study resources and announcements.
- Communication between the Android application and backend.
- Database connectivity.
- Campus services, transport and safety sections.
- Input validation and error handling.

Testing results should be recorded separately, showing which features passed, failed or still need attention.

## 10. Group Development

CampusConnect SA was developed as a group project. We worked together on planning, designing, implementing and improving the application.

We also used GitHub to manage our source code and keep the Android application and backend in one repository.

## 11. Known Limitations

Some features may still require further configuration, testing or improvement. For example, Google sign-in and other services that depend on external configurations must be verified in the final deployment environment.

The application also requires the correct backend and database configuration to operate as intended.

## 12. Future Improvements

Possible future improvements include:

- Completing and verifying Google Single Sign-On.
- Improving offline access and data synchronisation.
- Expanding notification functionality.
- Adding more student services.
- Preparing and testing a production release.

## 13. Repository

**GitHub:** https://github.com/tumelots07/CampusConnectSA

Group Members:
ST10452101_Tshiamo Tumelo Simango
ST10172741-KEOAGILE LETLAPE
ST10354054 - Thoriso Maenetja
ST10387277 Khothatso Qobolo


---

**CampusConnect SA – Smart Student Life Assistant**

*Developed as a group academic project.*
