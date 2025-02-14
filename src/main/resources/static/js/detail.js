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
                isLiked: data.isLiked,
                isScraped: data.isScraped
            };

            // 게시글 내용 업데이트
            if (post.postCategory == "Qna") {
                document.getElementById("page-title").textContent = "질문 게시판";
            } else if (post.postCategory == "Knowledge") {
                document.getElementById("page-title").textContent = "지식 게시판";
            } else if (post.postCategory == "General") {
                document.getElementById("page-title").textContent = "자유 게시판";
            }
            document.getElementById("post-title").textContent = post.title;
            document.getElementById("nickname").textContent = post.nickname;
            const profileImageElement = document.getElementById("profile-image");
            profileImageElement.src = post.profileImageUrl || "/img/undraw_profile.svg";
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

            // 페이지 로드 시 좋아요 상태 확인
            fetch(`/api/posts/${postId}/like-status`)
                .then(response => response.json())
                .then(data => {
                    if (data.isLiked) {  // 서버에서 받은 좋아요 상태
                        post.isLiked = true;
                        likeIcon.classList.remove('bi-heart');
                        likeIcon.classList.add('bi-heart-fill');
                        likeButton.classList.add('active'); // 강조 효과 유지
                    } else {
                        post.isLiked = false;
                        likeIcon.classList.remove('bi-heart-fill');
                        likeIcon.classList.add('bi-heart');
                        likeButton.classList.remove('active');
                    }

                    // 서버에서 받은 좋아요 개수 반영
                    likeCountElement.textContent = data.likeCount;
                })
                .catch(error => console.error('좋아요 상태 가져오기 실패:', error));

            // 좋아요 토글 버튼 처리
            likeButton.addEventListener('click', function () {
                const likeAction = post.isLiked ? 'unlike' : 'Like';

                // 즉각적으로 UI 변경 (서버 응답 전에 미리 반영)
                post.isLiked = !post.isLiked; // 상태 토글
                likeButton.classList.toggle('active');

                if (post.isLiked) {
                    likeIcon.classList.remove('bi-heart');
                    likeIcon.classList.add('bi-heart-fill');
                } else {
                    likeIcon.classList.remove('bi-heart-fill');
                    likeIcon.classList.add('bi-heart');
                }

                // 서버와의 연동: 좋아요 상태 업데이트
                fetch(`/api/posts/${postId}/like`, {
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
                       likeCountElement.textContent = data.likeCount; // 서버에서 받은 좋아요 개수 업데이트
                   } else {
                       console.error('서버 오류: 좋아요 변경 실패');
                   }
                })
                .catch(error => {
                    console.error('서버와의 연동 중 오류가 발생했습니다:', error);
                });
            });

            const saveButton = document.getElementById('save-btn');
            const saveIcon = document.getElementById('save-icon');

            // 스크랩 상태를 서버에서 받아오기
            fetch(`/api/posts/${postId}/scrape-status`)
                .then(response => response.json())
                .then(data => {
                    if (data.isScraped) {  // 서버에서 받은 스크랩 상태
                        post.isScraped = true;
                        saveIcon.classList.add('bi-bookmark-fill');
                        saveIcon.classList.remove('bi-bookmark');
                        saveButton.classList.add('active'); // 강조 효과 유지
                    } else {
                        post.isScraped = false;
                        saveIcon.classList.add('bi-bookmark');
                        saveIcon.classList.remove('bi-bookmark-fill');
                        saveButton.classList.remove('active');
                    }
                })
                .catch(error => console.error('스크랩 상태 가져오기 실패:', error));

            // 스크랩 토글 버튼 처리
            saveButton.addEventListener('click', function () {
                const scrape = post.isScraped ? 'cancel' : 'scrape';
                post.isScraped = !post.isScraped; // 상태 토글
                saveButton.classList.toggle('active');
                if (post.isScraped) {
                    saveIcon.classList.remove('bi-bookmark');
                    saveIcon.classList.add('bi-bookmark-fill');
                } else {
                    saveIcon.classList.remove('bi-bookmark-fill');
                    saveIcon.classList.add('bi-bookmark');
                }

                // 서버와의 연동: 스크랩 상태 업데이트
                fetch(`/api/posts/${postId}/scrape`, {
                    method: 'PATCH',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        scrape: scrape,
                    })
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) { // 서버에서 성공적으로 처리했는지 확인
                        post.isScraped = !post.isScraped; // 서버 응답을 받고 상태 변경
                        saveButton.classList.toggle('active');

                        if (post.isScraped) {
                            saveIcon.classList.remove('bi-bookmark');
                            saveIcon.classList.add('bi-bookmark-fill');
                        } else {
                            saveIcon.classList.remove('bi-bookmark-fill');
                            saveIcon.classList.add('bi-bookmark');
                        }
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
    const postId = window.location.pathname.split('/')[3];

    const commentList = document.getElementById("comment-list");
    const currentPageElement = document.getElementById('currentPage');
    const totalPagesElement = document.getElementById('totalPages');
    const totalElementsElement = document.getElementById('totalElements');
    const pagination = document.getElementById('pagination');

    commentList.innerHTML = ''; // 새로운 게시글을 추가하기 전에 기존 목록을 초기화
    pagination.innerHTML = ''; // 페이지네이션을 업데이트하기 전에 초기화

    const apiEndpoint = `/api/comments/list?postId=${postId}`;

    fetch(apiEndpoint)
        .then(response => response.json())
        .then(data => {
            const comments = data.content;
            const totalElements = data.totalElements;
            const totalPages = data.totalPages;
            const currentPage = data.currentPage;

            currentPageElement.textContent = currentPage;
            totalPagesElement.textContent = totalPages;
            totalElementsElement.textContent = totalElements;

            if (comments.length === 0) {
                commentList.innerHTML = '<p>댓글이 없습니다.</p>';
            } else {
                comments.forEach(comment => {
                    const commentItem = document.createElement('div');
                    commentItem.classList.add('comment-item');

                    // 프로필 이미지
                    const img = document.createElement('img');
                    img.setAttribute('src', comment.profileImageUrl || '/img/undraw_profile.svg');
                    img.setAttribute('alt', '프로필 이미지');
                    commentItem.appendChild(img);

                    const commentDetails = document.createElement('div');
                    commentDetails.classList.add('comment-details');

                    const nickname = document.createElement('p');
                    nickname.textContent = comment.nickname;
                    commentDetails.appendChild(nickname);

                    const content = document.createElement('p');
                    content.textContent = comment.content;
                    content.classList.add('content-text');
                    commentDetails.appendChild(content);

                    const commentMeta = document.createElement('div');
                    commentMeta.classList.add('comment-meta');
                    const time = document.createElement('p');
                    time.textContent = new Date(comment.earliestTime).toLocaleString();
                    commentMeta.appendChild(time);
                    commentDetails.appendChild(commentMeta);

                    // 설정 메뉴 추가 (수정, 삭제 버튼)
                    const settingsMenu = document.createElement('div');
                    settingsMenu.classList.add('settings-menu');

                    // 수정 버튼
                    const editBtn = document.createElement('button');
                    editBtn.textContent = "수정";
                    editBtn.onclick = () => {
                        const originalContent = content.textContent;
                        const editForm = document.createElement('div');
                        editForm.classList.add('edit-form');

                        // 수정할 텍스트 영역
                        const textarea = document.createElement('textarea');
                        textarea.textContent = originalContent;
                        editForm.appendChild(textarea);

                        const submitBtn = document.createElement('button');
                        submitBtn.textContent = "확인";
                        submitBtn.onclick = () => {
                            const updatedContent = textarea.value;
                            fetch(`/api/comments/${comment.id}`, {
                                method: 'PATCH',
                                headers: {
                                    'Content-Type': 'application/json'
                                },
                                body: JSON.stringify({ content: updatedContent })
                            })
                            .then(response => response.json())
                            .then(data => {
                                content.textContent = data.content;  // 수정된 댓글 업데이트
                                editForm.remove();  // 수정 폼 제거
                            })
                            .catch(error => {
                                console.error('댓글 수정 실패:', error);
                                alert('댓글 수정 중 오류가 발생했습니다.');
                            });
                        };

                        const cancelBtn = document.createElement('button');
                        cancelBtn.textContent = "취소";
                        cancelBtn.onclick = () => {
                            editForm.remove();  // 수정 폼 취소
                        };

                        editForm.appendChild(submitBtn);
                        editForm.appendChild(cancelBtn);
                        commentItem.appendChild(editForm);
                    };

                    // 삭제 버튼
                    const deleteBtn = document.createElement('button');
                    deleteBtn.textContent = "삭제";
                    deleteBtn.onclick = () => {
                        if (confirm('정말로 삭제하시겠습니까?')) {
                            fetch(`/api/comments/${comment.id}`, {
                                method: 'DELETE',
                                headers: {
                                    'Content-Type': 'application/json'
                                }
                            })
                            .then(response => {
                                if (response.ok) {
                                    commentItem.remove();  // 댓글 삭제
                                    alert('댓글이 삭제되었습니다.');
                                } else {
                                    alert('댓글 삭제 실패');
                                }
                            })
                            .catch(error => {
                                console.error('삭제 요청 실패:', error);
                                alert('댓글 삭제 중 오류가 발생했습니다.');
                            });
                        }
                    };

                    settingsMenu.appendChild(editBtn);
                    settingsMenu.appendChild(deleteBtn);
                    commentDetails.appendChild(settingsMenu);

                    commentItem.appendChild(commentDetails);
                    commentList.appendChild(commentItem);
                });
            }
        });

    // 댓글 작성 버튼 클릭 이벤트
    const commentSubmitBtn = document.getElementById("comment-submit-btn");
    commentSubmitBtn.addEventListener('click', function () {
        const commentContent = document.getElementById('comment-content');
        const content = commentContent.value.trim();

        if (!content) {
            alert("댓글 내용을 입력해주세요.");
            return;
        }

        // 댓글 작성 요청
        const commentData = {
            content: content
        };

        fetch(`/api/comments/create?postId=${postId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(commentData)
        })
        .then(response => response.json())
        .then(data => {
            if (data) {
                // 댓글 리스트에 새로운 댓글 추가
                const newCommentItem = document.createElement('div');
                newCommentItem.classList.add('comment-item');

                // 새 댓글을 리스트에 추가
                commentList.prepend(newCommentItem);

                // 댓글 입력창 초기화
                commentContent.value = '';
            } else {
                alert("댓글 작성에 실패했습니다.");
            }
        })
        .catch(error => {
            console.error('댓글 작성 중 오류가 발생했습니다:', error);
        });
    });
}