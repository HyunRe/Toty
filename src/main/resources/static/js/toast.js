function showToast(message) {
    const toastContainer = document.getElementById("toastContainer");

    // 토스트 요소 생성
    const toastElement = document.createElement("div");
    toastElement.className = "toast align-items-center text-bg-primary border-0";
    toastElement.setAttribute("role", "alert");
    toastElement.setAttribute("aria-live", "assertive");
    toastElement.setAttribute("aria-atomic", "true");

    toastElement.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">
                ${message}
            </div>
            <button type="button" class="btn-close me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
    `;

    toastContainer.appendChild(toastElement);

    // Bootstrap Toast 객체 생성 및 표시
    const toast = new bootstrap.Toast(toastElement, { delay: 3000 });
    toast.show();

    // 3초 후 자동 삭제
    setTimeout(() => {
        toastElement.remove();
    }, 3500);
}

// SSE 연결 설정 (수정 필요)
const eventSource = new EventSource("/api/sse/subscribe");

eventSource.onmessage = function(event) {
    showToast(event.data); // 백엔드에서 받은 메시지를 토스트로 표시
};

// 연결 오류 처리
eventSource.onerror = function(event) {
    console.error("SSE 연결 오류:", event);
    eventSource.close(); // 필요 시 연결 종료
};
