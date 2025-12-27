// 백엔드에서 url 받아서 처리
document.addEventListener("DOMContentLoaded", async function () {
    try {
        // API 요청 (실제 엔드포인트로 변경 필요)
        let response = await fetch("/view/notifications/unread", {
            method: "GET",
            headers: {
                "Content-Type": "application/json"
            }
        });

        if (!response.ok) {
            throw new Error("서버에서 데이터를 가져오는데 실패했습니다.");
        }

        // JSON 데이터 파싱
        let data = await response.json();

        // API 응답 데이터 예시
        // data = { unReadCount: "5", notificationListUrl: "https://yourwebsite.com/knowledge-board" }

        // senderNickname 요소 업데이트
        let senderNicknameElement = document.getElementById("unReadCount");
        if (senderNicknameElement && data.unReadCount) {
            senderNicknameElement.textContent = data.unReadCount;
        }

        // 버튼
        let button = document.querySelector(".button");
        if (button) {
            // 로컬
//            button.setAttribute("href", "https://localhost:8070/main");

            // 서버
            button.setAttribute("href", "/view/users/home");
        }

    } catch (error) {
        console.error("데이터 로딩 오류:", error);
    }
});
