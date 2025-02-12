$(document).ready(function () {
    loadPosts();  // 게시글 로드

    $("#sortSelect").on("change", function () {
        loadPosts(1);  // 정렬 선택 시 첫 번째 페이지 로드
    });
});

function loadPosts(page = 1) {
    const postList = $("#post-list");
    const pagination = $("#pagination");
    const sort = $("#sortSelect").val();

    // 페이지 정보 업데이트
    const currentPageElement = $("#currentPage");
    const totalPagesElement = $("#totalPages");
    const totalElementsElement = $("#totalElements");

    postList.html(""); // 새로운 게시글 추가 전에 기존 게시글을 지움
    pagination.html(""); // 페이지네이션을 업데이트하기 전에 기존 페이지네이션을 지움

    // API 엔드포인트 - 실제 API 주소로 교체 필요
    const apiEndpoint = `/api/posts/list?sort=${sort}&page=${page}`;

    // 선택된 카테고리와 페이지에 맞는 게시글을 가져옴
    $.getJSON(apiEndpoint, function (data) {
        const posts = data.content;
        const totalElements = data.totalElements;
        const totalPages = data.totalPages;
        const currentPage = data.currentPage;
        console.log("data", data);

        // 페이지 정보 업데이트
        currentPageElement.text(currentPage);
        totalPagesElement.text(totalPages);
        totalElementsElement.text(totalElements);

        if (posts.length === 0) {
            postList.html("<p>게시글이 없습니다.</p>");
        } else {
            posts.forEach(post => {
                const formattedDate = formatDate(post.earliestTime);
                console.log("formattedDate", post.earliestTime);
                const postItem = $(`
                    <div class="post-list-item" onclick="location.href='/api/posts/${post.id}/detail'">
                        <img src="${post.profileImageUrl || 'profile.jpg'}" alt="프로필 이미지">
                        <div class="details">
                            <a href="/post/detail/${post.id}">${post.title}</a>
                            <p>닉네임: ${post.nickname} ${post.role === 'MENTOR' ? '<span class="badge badge-primary">MENTOR</span>' : ''}</p>
                        </div>
                        <div class="info">
                            <p>조회수: ${post.viewCount} / 좋아요: ${post.likeCount}</p>
                            <p>작성일: ${formattedDate}</p>
                        </div>
                    </div>
                `);
                postList.append(postItem);
            });

            // 페이지네이션: 이전, 페이지 번호, 다음 버튼 추가
            const createPageLink = (pageNumber, text, isActive) => {
                const li = $(`<li class="page-item ${isActive ? 'active' : ''}"></li>`);
                const a = $(`<a class="page-link" href="#">${text}</a>`);
                a.on("click", (e) => {
                    e.preventDefault();
                    loadPosts(pageNumber);  // 페이지 번호 클릭 시 해당 페이지 로드
                });
                li.append(a);
                return li;
            };

            // 이전 버튼
            if (currentPage > 1) {
                 pagination.append(createPageLink(currentPage - 1, "이전", false, false));
            } else {
                pagination.append(createPageLink(1, "이전", false, true));  // 비활성화된 "이전" 버튼
            }

            // 페이지 번호 (동적으로 생성)
            const pageRange = 10; // 한 번에 표시할 페이지 번호의 범위
            let startPage = Math.max(currentPage - Math.floor(pageRange / 2), 1);
            let endPage = Math.min(startPage + pageRange - 1, totalPages);

            for (let i = startPage; i <= endPage; i++) {
                pagination.append(createPageLink(i, i, i === currentPage, false));
            }

            // 다음 버튼
            if (currentPage < totalPages) {
                pagination.append(createPageLink(currentPage + 1, "다음", false, false));
            } else {
                pagination.append(createPageLink(totalPages, "다음", false, true));  // 비활성화된 "다음" 버튼
            }
         }
    }).fail(function () {
        postList.html("<p>게시글을 불러오는 데 실패했습니다.</p>");
    });
}

// 날짜를 yyyy-mm-dd 형식으로 변환하는 함수
function formatDate(dateString) {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}