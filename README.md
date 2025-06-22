# FitHive 🏋️‍♀️

## 📋 Overview

**FitHive** is a hybrid Android fitness and social media app that enables users to share workouts, explore exercises, and engage with a fitness-focused community. It supports real-time social interactions, offline caching, and a rich set of personalized features.

### 🔧 Tech Stack & Core Components

- **Cloud Firestore**: Stores dynamic, structured data such as users, posts, comments, workouts, likes, and hashtags with real-time syncing.
- **Firebase Authentication**: Manages user sign-up, sign-in, and session tracking.
- **Firebase Storage**: Handles media uploads, including profile pictures and workout-related images.
- **Room Database**: Provides local persistence to:
  - Cache large initial datasets (e.g., exercises, body parts, equipment).
  - Store user-specific flags (e.g., saved workouts/exercises).
  - Support optimistic UI updates (e.g., likes via `CachedLike` table).
- **ExerciseDB API**: Fetches an extensive exercise list on first app launch via the `ExerciseRepository`.

> 🔗 For Firebase setup instructions, refer to [Firebase Project Setup](PROJECT_SETUP.md).  
> 📁 For app structure details, see [App Structure](APP_STRUCTURE.md).

---

## 🚀 App Feature Highlights

### 1. 🔐 User Authentication
- Secure login and registration using Firebase Authentication.

### 2. 🧍 Discover New Users
- Browse and follow new users within the fitness community.

### 3. 📚 Browse Exercises
- Access a comprehensive list of exercises categorized by body part, equipment, and target muscle.

### 4. 📋 Workout Details
- View in-depth workout breakdowns including exercises and descriptions.

### 5. 💪 Exercise Details
- Get detailed info about each exercise including purpose, category, and targeted muscles.

### 6. 👤 User Profiles
- Access personal and other users' profiles with lists of posts, workouts, and social stats.

### 7. 🏋️ Explore Workouts
- Discover new and trending workouts created by the community.

### 8. 📰 Post Feed
- View a scrollable, paginated feed of workout posts from followed users.

### 9. ❤️ Interact with Posts
- Like, comment, and engage with posts using real-time Firestore sync and optimistic UI.

---

## 🧠 Architectural Highlights

- **MVVM Architecture**: Clean separation of UI, business logic, and data layers.
- **Repository Pattern**: Abstracts data handling across local and remote sources.
- **Unidirectional Data Flow**: ViewModels expose state using `StateFlow` and collect from Room and Firestore.
- **Offline-First Design**: Ensures smooth user experience with Room caching and synchronization logic.

---

## 📦 Project Highlights

- Real-time features using **Firestore listeners**
- Offline persistence with **Room**
- Media handling via **Firebase Storage**
- External API consumption via **Retrofit**
- Reactive UI with **Jetpack Compose** and **StateFlow**
- Scalable modular design following **Clean Architecture**

---


