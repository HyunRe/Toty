document.addEventListener("DOMContentLoaded", function () {
    fetch("/left-menu.html")
        .then(response => response.text())
        .then(html => {
            document.getElementById("left-menu-container").innerHTML = html;
        })
        .catch(error => console.error("왼쪽 메뉴 로딩 실패:", error));
});
