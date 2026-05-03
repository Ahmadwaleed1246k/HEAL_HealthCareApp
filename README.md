# 🏥 HEAL – Smart Healthcare Management System

A comprehensive healthcare management mobile application that connects patients with doctors, enables online appointments, lab test bookings, blood donation services, and complete medical record management.

---

## 🚀 FEATURES

### 👤 Patient Features

* **User Authentication** – Secure signup/login with email & password
* **Doctor Search & Filter** – Find doctors by specialization
* **Appointment Booking** – Schedule appointments with preferred time slots
* **Online Payments** – Secure payment for consultation fees
* **Prescription Management** – View and manage digital prescriptions
* **Lab Test Booking** – Book diagnostic tests with AI-powered recommendations
* **Medical History** – Complete record of past appointments and prescriptions
* **Heart Rate Monitor** – Real-time PPG-based heart rate using camera
* **Blood Donation** – Find nearby donation centers & schedule donations
* **Hospital Locator** – Locate nearby hospitals using GPS
* **Emergency Helplines** – Quick access to emergency contacts
* **Room Booking** – Book hospital rooms with advance payment

---

### 👨‍⚕️ Doctor Features

* **Profile Setup** – Add specialization, experience, and details
* **Appointment Management** – Accept/reject/reschedule appointments
* **Digital Prescriptions** – Issue electronic prescriptions
* **Revenue Dashboard** – Track earnings and appointments
* **Availability Management** – Manage time slots

---

### 🤖 AI-Powered Features

* **Smart Lab Test Recommendations** – AI-based suggestions
* **Automated Result Analysis** – AI-generated interpretations

---

## 🛠️ TECH STACK

* **Frontend:** Android (Java), Material Design
* **Backend:** Firebase Authentication, Firebase Realtime Database
* **APIs:** OpenStreetMap, Camera2 API, Glide, OkHttp, Gson
* **Architecture:** MVC with Fragments

---

## 🗄️ DATABASE STRUCTURE

```
Firebase Realtime Database
├── users/{userId}
├── doctors/{doctorId}
├── appointments/{apptId}
├── prescriptions/{prescId}
├── lab_tests/{testId}
├── test_bookings/{bookingId}
├── blood_donation/
├── hospitals/{hospitalId}
├── room_bookings/{bookingId}
├── medical_history/
└── ratings/{ratingId}
```

---

## ⚙️ INSTALLATION

1. Clone the repository:

   ```
   git clone https://github.com/yourusername/heal-healthcare-app.git
   ```

2. Open in Android Studio

3. Configure Firebase:

   * Create a Firebase project
   * Add Android app (`com.example.heal`)
   * Download `google-services.json` into `/app`
   * Enable Email/Password Authentication
   * Enable Realtime Database

4. Build & Run

---

## 🔐 REQUIRED PERMISSIONS

* INTERNET
* CAMERA (for heart rate monitoring)
* ACCESS_FINE_LOCATION
* ACCESS_COARSE_LOCATION

---

## 📦 KEY DEPENDENCIES

* Firebase Auth: 22.3.0
* Firebase Database: 20.3.0
* Material Design: 1.11.0
* RecyclerView: 1.3.2
* Glide: 4.16.0
* OSMDroid: 6.1.18
* OkHttp: 4.12.0
* Gson: 2.10.1
* Camera2: 1.3.0

---

## 🔄 WORKFLOWS

### Patient Journey

Sign up → Complete profile → Browse doctors → Book appointment → Pay → Receive prescription → Rate doctor

### Doctor Journey

Register → Complete profile → Set availability → Manage appointments → Issue prescriptions → View earnings

### Appointment Lifecycle

Request → Accept/Reject → Consultation → Prescription → Medical History

---

## 📱 APK GENERATION

* Debug APK:

  ```
  ./gradlew assembleDebug
  ```

* Release APK:

  ```
  ./gradlew assembleRelease
  ```

---

## 📜 LICENSE

Licensed under the Apache License, Version 2.0

---

## 📧 SUPPORT

* Email: [aw0412626644@gmail.com](mailto:aw0412626644@gmail.com)
* GitHub: Open an issue in the repository

