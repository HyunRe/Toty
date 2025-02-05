// Firebase SDK 불러오기
import { initializeApp } from "https://www.gstatic.com/firebasejs/10.7.0/firebase-app.js";
import { getMessaging, getToken, onMessage } from "https://www.gstatic.com/firebasejs/10.7.0/firebase-messaging.js";

// Firebase 설정
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
const firebaseApp = initializeApp(firebaseConfig);
const messaging = getMessaging(firebaseApp);

// 알림 권한 요청 및 FCM 토큰 가져오기
async function requestPermission() {
    try {
        const permission = await Notification.requestPermission();
        if (permission === "granted") {
            const token = await getToken(messaging, { vapidKey: "BGORj8XCPGAZQRStC5eQq4I_c1JFd9QHwx0iKmMmL2QJbx4JAIRQH4uwba-KbZDpCWZOoPTZktgT02eF6BYYie0" });
            console.log("FCM Token:", token);
            sendTokenToServer(token);
        } else {
            console.log("알림 권한이 거부되었습니다.");
        }
    } catch (error) {
        console.error("FCM 토큰 가져오기 실패:", error);
    }
}

// 서버로 토큰 전송
function sendTokenToServer(token) {
    fetch("/api/fcm/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ fcmToken: token })
    });
}

// 포그라운드 알림 처리
onMessage(messaging, (payload) => {
    console.log("포그라운드 메시지 수신:", payload);
    new Notification(payload.notification.title, {
        body: payload.notification.body,
        icon: "/firebase-logo.png"
    });
});

// 실행
requestPermission();