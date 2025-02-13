document.addEventListener('DOMContentLoaded', () => {
    // 게시글 정보와 댓글 로딩
    loadPosts();
    loadComments();
});

// 게시글 정보 로딩 함수
function loadPosts() {
    // API 엔드포인트 - 실제 API 주소로 교체 필요
    const postId = window.location.pathname.split('/')[3];
    const apiEndpoint = `/api/posts/${postId}/detail`;

    fetch(apiEndpoint)
        .then(response => response.json())
        .then(data => {
            const post = {
                postCategory: data.postCategory,
                title: data.title,
                nickname: data.nickname,
                profileImageUrl: data.profileImageUrl,
                viewCount: data.viewCount,
                likeCount: data.likeCount,
                earliestTime: new Date(data.earliestTime),
                content: data.content,
                tags: data.tags || [],
                isLiked: data.isLiked
            };

            // 게시글 내용 업데이트
            document.getElementById("page-title").textContent = post.postCategory + " 게시판";
            document.getElementById("post-title").textContent = post.title;
            document.getElementById("nickname").textContent = post.nickname;
            document.getElementById("profile-image").src = post.profileImageUrl;
            document.getElementById("view-count").textContent = post.viewCount;
            document.getElementById("like-count").textContent = post.likeCount;
            document.getElementById("post-time").textContent = post.earliestTime.toLocaleString();

            post.tags = document.getElementById("postTags").value;
            console.log("post.tags", post.tags)
            // 태그 영역 업데이트
            updatePostTags(post.tags, post.postCategory);

            const likeButton = document.getElementById('like-btn');
            const likeIcon = document.getElementById('like-icon');
            const likeCountElement = document.getElementById("like-count");

           // 좋아요 토글 버튼 처리
           likeButton.addEventListener('click', function () {
                const likeAction = post.isLiked ? 'unlike' : 'Like';
                post.isLiked = !post.isLiked; // 상태 토글
                likeButton.classList.toggle('active');
                if (post.isLiked) {
                    likeIcon.classList.remove('bi-heart');
                    likeIcon.classList.add('bi-heart-fill');
                } else {
                    likeIcon.classList.remove('bi-heart-fill');
                    likeIcon.classList.add('bi-heart');
                }

                const ApiEndpoint = `/api/posts/${postId}/like`;
                // 서버와의 연동: 좋아요 상태 업데이트
                fetch(ApiEndpoint, {
                    method: 'PATCH',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        likeAction: likeAction,
                    })
                })
                    .then(response => response.json())
                    .then(data => {
                        if (data.success) { // 서버에서 성공적으로 처리했는지 확인
                            post.isLiked = !post.isLiked; // 서버 응답을 받고 상태 변경
                            likeButton.classList.toggle('active');

                            if (post.isLiked) {
                                likeIcon.classList.remove('bi-heart');
                                likeIcon.classList.add('bi-heart-fill');
                            } else {
                                likeIcon.classList.remove('bi-heart-fill');
                                likeIcon.classList.add('bi-heart');
                            }

                            // 서버에서 받은 likeCount로 UI 업데이트
                            likeCountElement.textContent = data.likeCount;
                        }
                    })
                    .catch(error => {
                        console.error('서버와의 연동 중 오류가 발생했습니다:', error);
                    });
            });
        })
        .catch(error => {
            console.error('게시글을 가져오는 중 오류가 발생했습니다:', error);
        });
}

// 태그 영역 업데이트 함수
function updatePostTags(tags, postCategory) {
    const postTagsContainer = document.getElementById("post-tags");
    console.log("postTagsContainer", postTagsContainer)
    postTagsContainer.innerHTML = ""; // 기존 태그 초기화

    if (postCategory === "Qna" && tags.length > 0) {
        postTagsContainer.style.display = "flex"; // 태그 영역 표시

        if (typeof tags === "string") {
            try {
                tags = JSON.parse(tags);
            } catch (e) {
                console.error("태그 JSON 파싱 오류:", e);
                tags = [];
            }
        }

        tags.forEach(tag => {
            const tagElement = document.createElement("span");
            tagElement.classList.add("tag-item"); // 스타일 적용을 위한 클래스 추가
            tagElement.textContent = `#${tag.name}`;
            postTagsContainer.appendChild(tagElement);
        });
    } else {
        postTagsContainer.style.display = "none"; // 태그 영역 숨기기
    }
}

// 댓글 로딩 함수
function loadComments(page = 1) {
    const commentList = document.getElementById("comment-list");
    const currentPageElement = document.getElementById('currentPage');
    const totalPagesElement = document.getElementById('totalPages');
    const totalElementsElement = document.getElementById('totalElements');
    const pagination = document.getElementById('pagination');

    commentList.innerHTML = ''; // 새로운 게시글을 추가하기 전에 기존 목록을 초기화
    pagination.innerHTML = ''; // 페이지네이션을 업데이트하기 전에 초기화

    // 예시 API 엔드포인트 - 실제 API나 데이터 가져오는 방법으로 변경해야 함
    const apiEndpoint = `/api/comments/list`;

    // 댓글 가져오기
    fetch(apiEndpoint)
        .then(response => response.json())
        .then(data => {
            const comments = data.content;  // API 응답에서 "content" 필드에 게시글이 담겨 있다고 가정
            const totalElements = data.totalElements;
            const totalPages = data.totalPages;
            const currentPage = data.currentPage;

            // 페이지 정보 업데이트
            currentPageElement.textContent = currentPage;
            totalPagesElement.textContent = totalPages;
            totalElementsElement.textContent = totalElements;

            if (comments.length === 0) {
                commentList.innerHTML = '<p>게시글이 없습니다.</p>';
            } else {
                comments.forEach(comment => {
                    const commentItem = document.createElement('div');
                    commentItem.classList.add('comment-item');

                   // 프로필 이미지
                   const img = document.createElement('img');
                   img.setAttribute('src', comment.profileImageUrl || 'profile.jpg'); // 기본 프로필 이미지로 대체
                   img.setAttribute('alt', '프로필 이미지');
                   postItem.appendChild(img);

                    // 댓글 내용
                    const commentDetails = document.createElement('div');
                    commentDetails.classList.add('comment-details');

                    const nickname = document.createElement('p');
                    nickname.textContent = comment.nickname;
                    commentDetails.appendChild(nickname);

                    const content = document.createElement('p');
                    content.textContent = comment.content;
                    commentDetails.appendChild(content);

                    const commentMeta = document.createElement('div');
                    commentMeta.classList.add('comment-meta');
                    const time = document.createElement('p');
                    time.textContent = new Date(comment.earliestTime).toLocaleString();
                    commentMeta.appendChild(time);
                    commentDetails.appendChild(commentMeta);

                    // 댓글 수정 및 삭제 버튼 (자신의 댓글일 경우에만)
                    const actions = document.createElement('div');
                    actions.classList.add('comment-actions');
                    const editBtn = document.createElement('button');
                    editBtn.textContent = "수정";
                    editBtn.onclick = () => {
                        // 댓글 수정 처리
                        const commentForm = document.createElement('div');
                        commentForm.classList.add('comment-form');

                        const textarea = document.createElement('textarea');
                        textarea.textContent = content.textContent;  // 기존 댓글 내용으로 초기화
                        commentForm.appendChild(textarea);

                        const submitBtn = document.createElement('button');
                        submitBtn.textContent = "완료";
                        submitBtn.onclick = () => {
                            content.textContent = textarea.value;  // 댓글 내용 업데이트
                            commentForm.style.display = 'none';  // 수정 영역 숨김
                        };

                        commentForm.appendChild(submitBtn);
                        commentItem.appendChild(commentForm);
                    };

                    const deleteBtn = document.createElement('button');
                    deleteBtn.textContent = "삭제";
                    deleteBtn.onclick = () => {
                        if (confirm("정말 삭제하시겠습니까?")) {
                            commentItem.remove();  // 댓글 삭제
                            window.location.reload();  // 페이지 새로고침
                        }
                    };

                    actions.appendChild(editBtn);
                    actions.appendChild(deleteBtn);
                    commentDetails.appendChild(actions);

                    commentItem.appendChild(commentDetails);
                    commentList.appendChild(commentItem);
                }
            )
        }
    });
}

// myScrape로 이동
function goToMyScrapePage() {
    window.location.href = "/view/posts/myScrape";
}
