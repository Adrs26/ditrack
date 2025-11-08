# 🚍 Ditrack – Campus Bus Tracking App (Ongoing)

**Ditrack** is an Android application designed to track **Diponegoro University** campus buses in real time and provide accurate estimated arrival times for students. The app integrates IoT-based microcontroller data, machine learning models, and backend services to deliver reliable and efficient transportation tracking within campus areas.

---

## ✨ Features

- **Real-time Bus Tracking** – Displays live bus locations on a map using GPS data streamed from IoT devices.
- **Bus Stop Auto-Detection** – Automatically detects when the user is near a bus stop and displays a popup with relevant information. 
- **Estimated Arrival Time (ETA)** – Predicts arrival times using machine learning models trained on route and speed history.  
- **Passenger Detection** – Uses onboard camera input and ML-based object detection to estimate the number of passengers in the bus in real time.  
- **WebSocket Integration** – Consumes live data updates (bus position, ETA, passenger count) via WebSocket from the backend for faster, low-latency communication.  
- **Background Updates** – Runs a background service using `Android Service` to keep users informed even when the app is minimized.  
- **Modern UI** – Built with `Jetpack Compose` for a responsive, smooth, and user-friendly experience.  
- **Offline Fallback** – Displays the latest known bus data when network connectivity is poor.

---

## 🏗️ Tech Stack

| Category | Tools / Frameworks |
|-----------|--------------------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose |
| **Architecture** | MVVM + Clean Architecture |
| **Reactive Programming** | Coroutines & Flow |
| **Backend Communication** | Ktor, WebSocket |
| **Local Database** | Room |
| **Background Tasks** | Android Service |

---

## 📱 App Architecture

The app follows **MVVM with Clean Architecture** principles for scalability and maintainability.

---

## ⚙️ How It Works

1. **IoT Data Collection**  
   - The microcontroller (Raspberry Pi 5) captures real-time passenger images using a connected camera module.  
   - The images are processed by an onboard **YOLOv8** model to detect and count passengers.  
   - The resulting passenger count, along with bus **location** and **speed data**, is sent to the server through **MQTT** communication using a broker.  

2. **Backend Processing**  
   - The server receives and stores incoming data, then processes it further using **XGBoost** to calculate **Estimated Time of Arrival (ETA)**.  
   - After processing, the server pushes the results, such as bus location, ETA, and passenger count to both **web** and **Android clients** in real time via **WebSocket**.  

3. **Client Consumption**  
   - The Android app consumes this live data through the WebSocket connection and visualizes it on the UI, allowing users to view current bus positions, ETA, and passenger occupancy in real time.  

---

## 🚀 Future Plans

- Integrate with backend services to improve real-time data synchronization and ensure seamless communication between IoT devices, server, and client.
- Add smart notification features integrated with background services to notify users when a bus is approaching or has arrived at their selected bus stop.
- Implement an offline caching system that allows users to access the latest available bus data even without an internet connection.
- Optimize system performance by refining data flow, reducing latency in WebSocket connections, and improving overall app responsiveness.

---

## 📷 Screenshots

<p align="center">
  <img src="https://github.com/user-attachments/assets/8dd26643-6ec1-4ced-9d98-a0520a2e0dd5" width="200"/>
  <img src="https://github.com/user-attachments/assets/97863571-7bb4-448b-a50a-05eef087d2b8" width="200"/>
  <img src="https://github.com/user-attachments/assets/ff5567a9-b206-42ba-b037-e5376d3884a1" width="200"/>
  <img src="https://github.com/user-attachments/assets/b40de54e-8eac-42e3-98b3-078001f767ab" width="200"/>
  <img src="https://github.com/user-attachments/assets/a7b392f1-71ac-451e-ae24-7633950ca743" width="200"/>
  <img src="https://github.com/user-attachments/assets/5e423eee-60b8-48d2-ad37-78293eb428e5" width="200"/>
  <img src="https://github.com/user-attachments/assets/3d8ccaa7-4884-4c45-a9db-1dcb3bb8c700" width="200"/>
  <img src="https://github.com/user-attachments/assets/3a18187d-e621-42f7-b725-2875bf624e67" width="200"/>
  <img src="https://github.com/user-attachments/assets/9647509f-ab40-48f7-bc68-b425c7ecf369" width="200"/>
  <img src="https://github.com/user-attachments/assets/a02c5b73-d274-4a71-864c-0fa1dab52751" width="200"/>
  <img src="https://github.com/user-attachments/assets/f9494948-fd31-4a62-a593-be0aed70ee17" width="200"/>
  <img src="https://github.com/user-attachments/assets/7536be56-3117-4e1e-b5a2-2fffada4be85" width="200"/>
</p>

---

## 🔗 Repository

GitHub: [https://github.com/Adrs26/ditrack](https://github.com/Adrs26/ditrack)

---

## 👨‍💻 Android Developer

**Adrian Septiyadi**  
Final-year Computer Engineering Student, passionate about Android development.  
📧 [adrianseptiyadi@gmail.com](mailto:adrianseptiyadi@gmail.com)  
🔗 [LinkedIn](https://linkedin.com/in/adrian-septiyadi-9ba26b25a)
