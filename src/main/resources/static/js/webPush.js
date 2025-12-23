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

// VAPID Key
const VAPID_KEY = "BGORj8XCPGAZQRStC5eQq4I_c1JFd9QHwx0iKmMmL2QJbx4JAIRQH4uwba-KbZDpCWZOoPTZktgT02eF6BYYie0";

// Firebase 초기화
const firebaseApp = initializeApp(firebaseConfig);
const messaging = getMessaging(firebaseApp);

// 현재 FCM 토큰을 저장할 변수
let currentFcmToken = null;
let swRegistration = null;

// 서비스 워커 등록
async function registerServiceWorker() {
    if ('serviceWorker' in navigator) {
        try {
            // 기존 Service Worker 제거
            const existingRegistrations = await navigator.serviceWorker.getRegistrations();
            console.log("[FCM] 기존 Service Worker 개수:", existingRegistrations.length);

            for (let registration of existingRegistrations) {
                if (registration.active && registration.active.scriptURL.includes('firebase-messaging-sw.js')) {
                    console.log("[FCM] 기존 Service Worker 제거 중:", registration.active.scriptURL);
                    await registration.unregister();
                    console.log("[FCM] 기존 Service Worker 제거 완료");
                }
            }

            // 새로운 Service Worker 등록
            swRegistration = await navigator.serviceWorker.register("/firebase-messaging-sw.js");
            console.log("[FCM] Service Worker 등록 완료:", swRegistration);

            // Service Worker가 활성화될 때까지 대기
            await navigator.serviceWorker.ready;
            console.log("[FCM] Service Worker 활성화 완료");

            return swRegistration;
        } catch (error) {
            console.error("[FCM] Service Worker 등록 실패:", error);
            return null;
        }
    }
    return null;
}

// 알림 권한 요청 및 토큰 등록
async function requestPermissionAndRegisterToken() {
    try {
        // Service Worker 먼저 등록
        if (!swRegistration) {
            swRegistration = await registerServiceWorker();
        }

        if (!swRegistration) {
            console.error("[FCM] Service Worker 등록 실패로 인해 토큰 발급 불가");
            return;
        }

        console.log("[FCM] 현재 알림 권한 상태:", Notification.permission);

        const permission = await Notification.requestPermission();
        console.log("[FCM] 알림 권한 요청 결과:", permission);

        if (permission === "granted") {
            console.log("[FCM] ✅ 알림 권한이 허용되었습니다.");
            await registerFcmToken();
        } else if (permission === "denied") {
            console.warn("[FCM] ❌ 알림 권한이 거부되었습니다. 브라우저 설정에서 허용해주세요.");
            console.warn("[FCM] Chrome: 주소창 왼쪽 자물쇠 아이콘 클릭 → 알림 → 허용");
        } else {
            console.log("[FCM] ⚠️ 알림 권한 요청이 무시되었습니다.");
        }
    } catch (error) {
        console.error("[FCM] 알림 권한 요청 중 오류 발생:", error);
    }
}

// FCM 토큰 가져오기 및 서버에 등록
async function registerFcmToken() {
    try {
        const token = await getToken(messaging, {
            vapidKey: VAPID_KEY,
            serviceWorkerRegistration: swRegistration
        });

        if (token) {
            console.log("[FCM] 토큰 발급 성공:", token);
            currentFcmToken = token;
            await sendTokenToServer(token);
        } else {
            console.log("[FCM] 토큰을 가져올 수 없습니다. 알림 권한을 확인하세요.");
        }
    } catch (error) {
        console.error("[FCM] 토큰 가져오기 오류:", error);
    }
}

// 백엔드로 FCM 토큰 전송 (등록 또는 활성화)
async function sendTokenToServer(token) {
    try {
        const response = await fetch("/api/fcm/token", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ token })
        });

        const data = await response.json();

        if (response.ok) {
            console.log("[FCM] 토큰 등록/활성화 성공:", data);
        } else {
            console.error("[FCM] 토큰 등록/활성화 실패:", data);
        }
    } catch (error) {
        console.error("[FCM] 토큰 전송 오류:", error);
    }
}

// 로그아웃 시 FCM 토큰 비활성화
async function deactivateFcmToken() {
    try {
        if (!currentFcmToken) {
            // 로컬에 저장된 토큰이 없으면 새로 가져오기
            if (!swRegistration) {
                swRegistration = await registerServiceWorker();
            }

            if (swRegistration) {
                currentFcmToken = await getToken(messaging, {
                    vapidKey: VAPID_KEY,
                    serviceWorkerRegistration: swRegistration
                });
            }
        }

        if (!currentFcmToken) {
            console.log("[FCM] 비활성화할 토큰이 없습니다.");
            return;
        }

        const response = await fetch("/api/fcm/token", {
            method: "DELETE",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ token: currentFcmToken })
        });

        const data = await response.json();

        if (response.ok) {
            console.log("[FCM] 토큰 비활성화 성공:", data);
            currentFcmToken = null;
        } else {
            console.error("[FCM] 토큰 비활성화 실패:", data);
        }
    } catch (error) {
        console.error("[FCM] 토큰 비활성화 오류:", error);
    }
}

// 포그라운드 메시지 수신 시 알림 표시
onMessage(messaging, (payload) => {
    console.log("==============================================");
    console.log("[FCM] 🔔 푸시 알림 수신!!!");
    console.log("[FCM] payload 전체:", payload);
    console.log("[FCM] payload.notification:", payload.notification);
    console.log("[FCM] payload.data:", payload.data);
    console.log("[FCM] Notification.permission:", Notification.permission);
    console.log("==============================================");

    // notification 객체가 있으면 사용, 없으면 data에서 생성
    if (payload.notification) {
        // payload.data에 있는 url을 notification에 추가
        const notificationWithUrl = {
            ...payload.notification,
            url: payload.data?.url || '/',
            notificationId: payload.data?.notificationId,
            type: payload.data?.type
        };
        console.log("[FCM] notification에 URL 추가:", notificationWithUrl);
        showNotification(notificationWithUrl);
    } else if (payload.data) {
        // data만 있는 경우 notification 구조 생성
        const notification = {
            title: payload.data.title || 'TOTY 알림',
            body: payload.data.body || payload.data.message || '새로운 알림이 도착했습니다',
            icon: payload.data.icon || '/img/logo.png',
            url: payload.data.url || '/',
            notificationId: payload.data.notificationId,
            type: payload.data.type
        };
        console.log("[FCM] data에서 notification 생성:", notification);
        showNotification(notification);
    } else {
        console.error("[FCM] ❌ notification과 data 모두 없음!");
    }
});

// 알림 표시 함수
function showNotification(notification) {
    console.log("[FCM] showNotification 호출됨:", notification);
    console.log("[FCM] 알림 권한:", Notification.permission);

    if (Notification.permission !== "granted") {
        console.error("[FCM] ❌ 알림 권한 없음:", Notification.permission);
        alert("❌ 알림 권한이 없습니다!\n브라우저 주소창 왼쪽 아이콘을 클릭하여 알림을 허용해주세요.");
        return;
    }

    if (!notification) {
        console.error("[FCM] ❌ notification 객체가 null/undefined");
        return;
    }

    try {
        const title = notification.title || 'TOTY 알림';
        const body = notification.body || '새로운 알림';

        console.log("[FCM] 알림 생성 시도:", { title, body });

        const notif = new Notification(title, {
            body: body,
            icon: notification.icon || "/img/logo.png",
            badge: "/img/logo.png",
            tag: 'toty-notification-' + Date.now(),
            requireInteraction: true,  // 사용자가 클릭할 때까지 사라지지 않음
            silent: false,
            vibrate: [200, 100, 200]   // 진동 패턴 (모바일용)
        });

        console.log("==============================================");
        console.log("[FCM] ✅✅✅ 알림 생성 성공!!! ✅✅✅");
        console.log("[FCM] 알림 객체:", notif);
        console.log("[FCM] 화면에 알림이 보이나요? 우측 상단 또는 하단을 확인하세요!");
        console.log("==============================================");

        // 3초 후 자동으로 알림이 보이는지 확인 메시지
        setTimeout(() => {
            const userSawIt = confirm("알림이 화면에 보였나요?\n(우측 상단이나 하단에 팝업으로 떴어야 합니다)\n\n보였으면 '확인', 안 보였으면 '취소'를 눌러주세요.");
            if (userSawIt) {
                console.log("[FCM] ✅ 사용자가 알림을 봤습니다!");
            } else {
                console.error("[FCM] ❌ 사용자가 알림을 못 봤습니다. 시스템 알림 설정을 확인하세요.");
                alert("시스템 알림 설정 확인:\n\n" +
                      "Windows: 설정 > 시스템 > 알림 및 작업 > Chrome 알림 켜기\n" +
                      "Mac: 시스템 환경설정 > 알림 > Chrome 알림 허용\n" +
                      "Linux: 시스템 설정 > 알림");
            }
        }, 3000);

        // 알림 클릭 시 URL 이동
        notif.onclick = function(event) {
            console.log("[FCM] 🖱️ 알림 클릭됨!");
            event.preventDefault();

            const url = notification.url || '/';
            const fullUrl = url.startsWith('http') ? url : `http://localhost:8070${url}`;

            console.log("[FCM] 원본 URL:", url);
            console.log("[FCM] 최종 URL:", fullUrl);
            console.log("[FCM] 알림 ID:", notification.notificationId);
            console.log("[FCM] 알림 타입:", notification.type);

            // 같은 창에서 열기
            window.location.href = fullUrl;
            notif.close();
        };

        // 알림이 표시될 때
        notif.onshow = function() {
            console.log("[FCM] 📢 알림이 화면에 표시되었습니다!");
        };

        // 알림이 닫힐 때
        notif.onclose = function() {
            console.log("[FCM] 🚪 알림이 닫혔습니다");
        };

        // 알림 에러
        notif.onerror = function(err) {
            console.error("[FCM] ⚠️ 알림 에러:", err);
        };

    } catch (error) {
        console.error("==============================================");
        console.error("[FCM] ❌❌❌ 알림 생성 실패!!! ❌❌❌");
        console.error("[FCM] 에러:", error);
        console.error("[FCM] 에러 이름:", error.name);
        console.error("[FCM] 에러 메시지:", error.message);
        console.error("[FCM] 에러 스택:", error.stack);
        console.error("==============================================");
        alert("❌ 알림 생성 실패!\n\n에러: " + error.message + "\n\n브라우저 콘솔을 확인하세요.");
    }
}

// 전역에서 사용 가능하도록 함수 export
window.FcmManager = {
    requestPermissionAndRegisterToken,
    registerFcmToken,
    deactivateFcmToken
};

// 로그인 상태인 경우 자동으로 권한 요청 및 토큰 등록
// (로그인 페이지가 아닌 경우에만 실행)
if (!window.location.pathname.includes('/login') &&
    !window.location.pathname.includes('/signup') &&
    !window.location.pathname.includes('/sign-in')) {
    requestPermissionAndRegisterToken();
}