document.addEventListener("DOMContentLoaded", function () {
    loadPosts();
});

function loadPosts(page = 1) {
    const postList = document.getElementById('post-list');
    const pagination = document.getElementById('pagination');
    //const sort = document.getElementById('sortSelect').value;
    const postCategory = new URLSearchParams(window.location.search).get('postCategory');

    console.log("postCategory" + postCategory);

     // 카테고리에 따른 페이지 제목 업데이트
     const pageTitle = document.querySelector('#page-title');
     if (pageTitle) {
        if (postCategory === 'GENERAL') {
            pageTitle.textContent = '자유 게시판입니다. 자유롭게 게시글을 작성해주세요.';
        } else if (postCategory === 'KNOWLEDGE') {
            pageTitle.textContent = '정보 게시판입니다. 멘토들의 유익한 글이 있는 곳입니다.';
        } else if (postCategory === 'QnA') {
            pageTitle.textContent = 'Q/A 게시판입니다. 자유롭게 질의 응답을 해주세요.';
        }
    } else {
        console.error("페이지 제목 요소를 찾을 수 없습니다.");
    }

    // 페이지 정보 업데이트
    const currentPageElement = document.getElementById('currentPage');
    const totalPagesElement = document.getElementById('totalPages');
    const totalElementsElement = document.getElementById('totalElements');

    postList.innerHTML = ''; // 새로운 게시글 목록을 추가하기 전에 기존 목록을 비웁니다.
    pagination.innerHTML = ''; // 페이지네이션을 업데이트 하기 전에 기존 항목을 비웁니다.

    // 예시 API 엔드포인트 - 실제 API 또는 데이터 가져오는 방법으로 교체
    const apiEndpoint = `/api/posts/categoryList?postCategory=${postCategory}&page=${page}`;

    // 선택된 카테고리와 페이지에 맞춰 게시글을 가져옵니다.
    fetch(apiEndpoint)
        .then(response => response.json())
        .then(data => {
            const posts = data.content;  // API가 "content" 필드에 게시글을 반환한다고 가정
            const totalElements = data.totalElements;
            const totalPages = data.totalPages;
            const currentPage = data.currentPage;

            // 페이지 정보 업데이트
            currentPageElement.textContent = currentPage;
            totalPagesElement.textContent = totalPages;
            totalElementsElement.textContent = totalElements;

            if (posts.length === 0) {
                postList.innerHTML = '<p>게시글이 없습니다.</p>';
            } else {
                posts.forEach(post => {
                    const postItem = document.createElement('div');
                    postItem.classList.add('post-list-item');
                    postItem.setAttribute('onclick', `location.href='/api/posts/${post.id}/detail'`);
                    postItem.style.cursor = 'pointer';

                    // 프로필 이미지
                    const img = document.createElement('img');
                    img.setAttribute('src', post.profileImageUrl || 'profile.jpg'); // 기본 프로필 이미지로 대체
                    img.setAttribute('alt', '프로필 이미지');
                    postItem.appendChild(img);

                    // 게시글 제목, 닉네임 등의 세부 사항
                    const details = document.createElement('div');
                    details.classList.add('details');

                    const titleLink = document.createElement('a');
                    titleLink.setAttribute('href', `/post/detail/${post.id}`);
                    titleLink.textContent = post.title;
                    details.appendChild(titleLink);

                    const nickname = document.createElement('p');
                    nickname.classList.add('nickname');
                    nickname.textContent = `닉네임: ${post.nickname}`;
                    details.appendChild(nickname);

                    // 작성자가 멘토일 경우 멘토 배지 추가
                    if (post.role === 'MENTOR') {
                        const badge = document.createElement('span');
                        badge.classList.add('badge', 'badge-primary');
                        badge.textContent = 'MENTOR';
                        details.appendChild(badge);
                    }

                    postItem.appendChild(details);

                    // 게시글 정보 (조회수, 좋아요, 작성일)
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

                    // 게시글 항목을 목록에 추가
                    postList.appendChild(postItem);
                });

                // 페이지네이션: 이전, 페이지 번호, 다음 추가
                const createPageLink = (pageNumber, text, isActive) => {
                    const li = document.createElement('li');
                    li.classList.add('page-item');
                    if (isActive) li.classList.add('active');

                    const a = document.createElement('a');
                    a.classList.add('page-link');
                    a.setAttribute('href', '#');
                    a.textContent = text;
                    a.addEventListener('click', (e) => {
                        e.preventDefault();
                        loadPosts(pageNumber);
                    });

                    li.appendChild(a);
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
        })
        .catch(error => {
            console.error('게시글을 가져오는 중 오류가 발생했습니다:', error);
            postList.innerHTML = '<p>게시글을 불러오는 데 실패했습니다.</p>';
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