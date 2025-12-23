document.addEventListener("DOMContentLoaded", () => {
  loadLikedPosts(1);
});

function loadLikedPosts(page = 1) {
    const postList = document.getElementById('post-list');
    const currentPageElement = document.getElementById('currentPage');
    const totalPagesElement = document.getElementById('totalPages');
    const totalElementsElement = document.getElementById('totalElements');
    const pagination = document.getElementById('pagination');

    postList.innerHTML = ''; // 새로운 게시글을 추가하기 전에 기존 목록을 초기화
    pagination.innerHTML = ''; // 페이지네이션을 업데이트하기 전에 초기화

    // 좋아요한 게시글 API 엔드포인트
    const apiEndpoint = `/api/posts/myLikeList?page=${page}`;

    // 좋아요한 게시글 가져오기
    fetch(apiEndpoint)
        .then(response => response.json())
        .then(data => {
            const posts = data.content;  // API 응답에서 "content" 필드에 게시글이 담겨 있다고 가정
            const totalElements = data.totalElements;
            const totalPages = data.totalPages;
            const currentPage = data.currentPage;

            // 페이지 정보 업데이트
            currentPageElement.textContent = currentPage;
            totalPagesElement.textContent = totalPages;
            totalElementsElement.textContent = totalElements;

            if (posts.length === 0) {
                postList.innerHTML = '<p>좋아요한 게시글이 없습니다.</p>';
            } else {
                posts.forEach(post => {
                    const postItem = document.createElement('div');
                    postItem.classList.add('post-list-item');
                    postItem.setAttribute('onclick', `location.href='/view/posts/${post.id}/detail'`);
                    postItem.style.cursor = 'pointer';

                    // 프로필 이미지
                    const img = document.createElement('img');
                    img.setAttribute('src', post.profileImageUrl || '/img/undraw_profile.svg');
                    img.setAttribute('alt', '프로필 이미지');
                    postItem.appendChild(img);

                    // 세부 사항 (제목, 닉네임)
                    const details = document.createElement('div');
                    details.classList.add('details');

                    const titleLink = document.createElement('a');
                    titleLink.textContent = post.title;
                    details.appendChild(titleLink);

                    const nickname = document.createElement('p');
                    nickname.classList.add('nickname');
                    nickname.textContent = `작성자: ${post.nickname}`;
                    details.appendChild(nickname);

                    // 멘토 배지 추가 (작성자가 멘토인 경우)
                    if (post.role === 'MENTOR') {
                        const badge = document.createElement('span');
                        badge.classList.add('badge', 'badge-primary');
                        badge.textContent = 'MENTOR';
                        details.appendChild(badge);
                    }

                    postItem.appendChild(details);

                    // 정보 (조회수, 좋아요, 작성일)
                    const info = document.createElement('div');
                    info.classList.add('info');

                    const viewLike = document.createElement('p');
                    viewLike.textContent = `조회수: ${post.viewCount} / 좋아요: ${post.likeCount}`;
                    info.appendChild(viewLike);

                    const formattedDate = formatDate(post.earliestTime);
                    const date = document.createElement('p');
                    date.textContent = `작성일: ${formattedDate}`;
                    info.appendChild(date);

                    postItem.appendChild(info);

                    // 게시글 항목을 리스트에 추가
                    postList.appendChild(postItem);
                });

                // 페이지네이션 생성
                createPagination(currentPage, totalPages, page);
            }
        })
        .catch(error => {
            console.error('Error fetching liked posts:', error);
            postList.innerHTML = '<p>게시글을 불러오는 중 오류가 발생했습니다.</p>';
        });
}

// 페이지네이션 버튼 생성
function createPagination(currentPage, totalPages, page) {
    const pagination = document.getElementById('pagination');
    pagination.innerHTML = '';

    const createPageLink = (pageNumber, text, isActive) => {
        const li = document.createElement('li');
        li.classList.add('page-item');
        if (isActive) li.classList.add('active');
        const a = document.createElement('a');
        a.classList.add('page-link');
        a.textContent = text;
        a.href = '#';
        a.onclick = (e) => {
            e.preventDefault();
            loadLikedPosts(pageNumber);
        };
        li.appendChild(a);
        return li;
    };

    // 이전 버튼
    if (currentPage > 1) {
        pagination.append(createPageLink(currentPage - 1, "이전", false));
    } else {
        pagination.append(createPageLink(1, "이전", false));  // 비활성화된 "이전" 버튼
    }

    // 페이지 번호 (동적으로 생성)
    const pageRange = 10; // 한 번에 표시할 페이지 번호의 범위
    let startPage = Math.max(currentPage - Math.floor(pageRange / 2), 1);
    let endPage = Math.min(startPage + pageRange - 1, totalPages);

    for (let i = startPage; i <= endPage; i++) {
        pagination.append(createPageLink(i, i, i === currentPage));
    }

    // 다음 버튼
    if (currentPage < totalPages) {
        pagination.append(createPageLink(currentPage + 1, "다음", false));
    } else {
        pagination.append(createPageLink(totalPages, "다음", false));  // 비활성화된 "다음" 버튼
    }
}

// 날짜를 yyyy-mm-dd 형식으로 변환하는 함수
function formatDate(dateString) {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}
