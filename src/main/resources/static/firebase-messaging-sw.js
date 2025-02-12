importScripts("https://www.gstatic.com/firebasejs/10.7.1/firebase-app.js");
importScripts("https://www.gstatic.com/firebasejs/10.7.1/firebase-messaging.js");

// Import the functions you need from the SDKs you need
import { initializeApp } from "firebase/app";
import { getAnalytics } from "firebase/analytics";
// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries

// Your web app's Firebase configuration
// For Firebase JS SDK v7.20.0 and later, measurementId is optional
const firebaseConfig = {
  apiKey: "AIzaSyDxyl3YRLT1FmO3Cjfv-W8PrrWZMYiq1no",
  authDomain: "toty-6d4b4.firebaseapp.com",
  projectId: "toty-6d4b4",
  storageBucket: "toty-6d4b4.firebasestorage.app",
  messagingSenderId: "846986079585",
  appId: "1:846986079585:web:59f9c7a2e75b48ca91a920",
  measurementId: "G-3TRGZCLRG8"
};

// Firebase 초기화
firebase.initializeApp(firebaseConfig);
const messaging = firebase.messaging();

// 백그라운드 푸시 알림 수신 처리
messaging.onBackgroundMessage(payload => {
    console.log("백그라운드 푸시 알림:", payload);

    self.registration.showNotification(payload.notification.title, {
        body: payload.notification.body,
        icon: payload.notification.icon || "/default-icon.png"
    });
});