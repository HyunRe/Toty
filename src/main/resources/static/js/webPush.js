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

// 알림 권한 요청
async function requestPermission() {
    try {
        const permission = await Notification.requestPermission();
        if (permission === "granted") {
            console.log("알림 권한이 허용되었습니다.");
            getToken();
        } else {
            console.log("알림 권한이 거부되었습니다.");
        }
    } catch (error) {
        console.error("알림 권한 요청 중 오류 발생:", error);
    }
}

// FCM 토큰 가져오기
async function getToken() {
    try {
        const currentToken = await messaging.getToken({ vapidKey: "BGORj8XCPGAZQRStC5eQq4I_c1JFd9QHwx0iKmMmL2QJbx4JAIRQH4uwba" });
        if (currentToken) {
            console.log("FCM 토큰:", currentToken);
            sendTokenToServer(currentToken); // 백엔드에 토큰 전송
        } else {
            console.log("토큰을 가져올 수 없습니다.");
        }
    } catch (error) {
        console.error("토큰 가져오기 오류:", error);
    }
}

// 백엔드로 FCM 토큰 전송 (url 수정 필요)
function sendTokenToServer(token) {
    fetch("http://localhost:8070/api/fcmToken/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ token })
    })
    .then(response => response.json())
    .then(data => console.log("토큰 저장 성공:", data))
    .catch(error => console.error("토큰 저장 오류:", error));
}

// 서비스 워커 등록
navigator.serviceWorker.register("firebase-messaging-sw.js")
    .then(registration => {
        console.log("Service Worker 등록 완료:", registration);
        messaging.useServiceWorker(registration);
    })
    .catch(error => console.error("Service Worker 등록 실패:", error));

// 메시지 수신 시 알림 표시
messaging.onMessage(payload => {
    console.log("푸시 알림 수신:", payload);
    showNotification(payload.notification);
});

// 알림 표시 함수
function showNotification(notification) {
    if (Notification.permission === "granted") {
        new Notification(notification.title, {
            body: notification.body,
            icon: notification.icon || "/default-icon.png"
        });
    }
}

// 알림 권한 요청 실행
requestPermission();