// Firebase SDK 불러오기 (Service Worker용 - importScripts 사용)
importScripts("https://www.gstatic.com/firebasejs/10.7.0/firebase-app-compat.js");
importScripts("https://www.gstatic.com/firebasejs/10.7.0/firebase-messaging-compat.js");

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
firebase.initializeApp(firebaseConfig);
const messaging = firebase.messaging();

// 백그라운드 푸시 알림 수신 처리
messaging.onBackgroundMessage((payload) => {
  console.log('==============================================');
  console.log('[SW] 🔔 백그라운드 메시지 수신!!!');
  console.log('[SW] payload 전체:', payload);
  console.log('[SW] payload.notification:', payload.notification);
  console.log('[SW] payload.data:', payload.data);
  console.log('==============================================');

  let notificationTitle = 'TOTY 알림';
  let notificationBody = '새로운 알림이 도착했습니다';
  let notificationIcon = '/img/logo.png';
  let notificationData = {};

  // notification 객체가 있으면 사용
  if (payload.notification) {
    notificationTitle = payload.notification.title || notificationTitle;
    notificationBody = payload.notification.body || notificationBody;
    notificationIcon = payload.notification.icon || notificationIcon;
    console.log('[SW] notification에서 데이터 추출');
  }
  // notification 없으면 data에서 추출 시도
  else if (payload.data) {
    notificationTitle = payload.data.title || notificationTitle;
    notificationBody = payload.data.body || payload.data.message || notificationBody;
    notificationIcon = payload.data.icon || notificationIcon;
    console.log('[SW] data에서 데이터 추출');
  }

  // data 객체 병합
  if (payload.data) {
    notificationData = { ...payload.data };
  }
  if (payload.notification?.data) {
    notificationData = { ...notificationData, ...payload.notification.data };
  }

  const notificationOptions = {
    body: notificationBody,
    icon: notificationIcon,
    badge: '/img/logo.png',
    data: notificationData,
    tag: 'toty-notification-' + Date.now(),
    requireInteraction: true,  // 사용자가 클릭할 때까지 사라지지 않음
    vibrate: [200, 100, 200]   // 진동 패턴
  };

  console.log('[SW] 알림 표시:', notificationTitle, notificationOptions);

  self.registration.showNotification(notificationTitle, notificationOptions)
    .then(() => {
      console.log('[SW] ✅ 알림 표시 성공');
    })
    .catch(err => {
      console.error('[SW] ❌ 알림 표시 실패:', err);
    });
});

// 알림 클릭 이벤트 처리
self.addEventListener('notificationclick', (event) => {
  console.log('==============================================');
  console.log('[SW] 🖱️ 알림 클릭됨!');
  console.log('[SW] event.notification:', event.notification);
  console.log('[SW] event.notification.data:', event.notification.data);
  console.log('==============================================');

  event.notification.close();

  // 알림 데이터에서 URL 가져오기
  const url = event.notification.data?.url || '/';
  const fullUrl = url.startsWith('http') ? url : `http://localhost:8070${url}`;

  console.log('[SW] 원본 URL:', url);
  console.log('[SW] 최종 URL:', fullUrl);

  event.waitUntil(
    clients.matchAll({ type: 'window', includeUncontrolled: true })
      .then((clientList) => {
        console.log('[SW] 열린 창 개수:', clientList.length);

        // 이미 열린 창이 있으면 해당 URL로 이동 + 포커스
        for (let i = 0; i < clientList.length; i++) {
          const client = clientList[i];
          console.log('[SW] 창 ' + i + ' URL:', client.url);

          // 같은 origin의 창을 찾아서 이동
          if (client.url.includes('localhost:8070')) {
            console.log('[SW] ✅ 기존 창 찾음 - URL 이동 + 포커스');
            return client.navigate(fullUrl).then(client => client.focus());
          }
        }

        // 열린 창이 없으면 새 창 열기
        console.log('[SW] ❌ 기존 창 없음 - 새 창 열기');
        if (clients.openWindow) {
          return clients.openWindow(fullUrl);
        }
      })
      .catch(err => {
        console.error('[SW] ❌ 알림 클릭 처리 실패:', err);
      })
  );
});