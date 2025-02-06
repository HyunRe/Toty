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

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const analytics = getAnalytics(app);