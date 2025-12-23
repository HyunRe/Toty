let longPressTimer;
let currentSelectedComment = null;
let originalCommentContent = null;

document.addEventListener("DOMContentLoaded", () => {
    loadCommentList(1);
    createCommentModal();
});

// 모달 생성
function createCommentModal() {
    const modalHtml = `
        <div id="commentActionModal" class="custom-modal">
            <div class="custom-modal-content">
                <h5>댓글 관리</h5>
                <div class="custom-modal-buttons">
                    <button class="custom-modal-btn edit-btn" onclick="startEditComment()">수정</button>
                    <button class="custom-modal-btn delete-btn" onclick="handleCommentDelete()">삭제</button>
                    <button class="custom-modal-btn cancel-btn" onclick="closeCommentModal()">취소</button>
                </div>
            </div>
        </div>
    `;
    document.body.insertAdjacentHTML('beforeend', modalHtml);
}

// 모달 열기
function openCommentModal(comment, commentElement) {
    currentSelectedComment = comment;
    currentSelectedComment.element = commentElement;
    document.getElementById('commentActionModal').style.display = 'flex';
}

// 모달 닫기
function closeCommentModal() {
    document.getElementById('commentActionModal').style.display = 'none';
    currentSelectedComment = null;
}

// 댓글 수정 시작
function startEditComment() {
    if (!currentSelectedComment) return;

    // 모달 닫기 전에 element와 content를 변수에 저장
    const commentElement = currentSelectedComment.element;
    const commentContent = currentSelectedComment.content;
    const commentId = currentSelectedComment.id;

    closeCommentModal();

    const contentDiv = commentElement.querySelector('.comment-content-text');

    // 원본 내용 저장
    originalCommentContent = commentContent;

    // 수정 UI로 변경
    contentDiv.innerHTML = `
        <textarea class="form-control comment-edit-textarea" style="width: 100%; min-height: 80px;">${originalCommentContent}</textarea>
        <div style="margin-top: 10px;">
            <button class="btn btn-sm btn-primary" onclick="saveCommentEdit(${commentId})">확인</button>
            <button class="btn btn-sm btn-secondary" onclick="cancelCommentEdit(${commentId})">취소</button>
        </div>
    `;
}

// 댓글 수정 저장
function saveCommentEdit(commentId) {
    const textarea = document.querySelector('.comment-edit-textarea');
    const newContent = textarea.value.trim();

    if (!newContent) {
        alert('댓글 내용을 입력해주세요.');
        return;
    }

    fetch(`/api/comments/${commentId}`, {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ content: newContent })
    })
    .then(response => {
        if (response.ok) {
            alert('댓글이 수정되었습니다.');
            loadCommentList(1); // 목록 새로고침
        } else {
            alert('댓글 수정 실패');
        }
    })
    .catch(error => {
        console.error('댓글 수정 오류:', error);
        alert('댓글 수정 중 오류가 발생했습니다.');
    });
}

// 댓글 수정 취소
function cancelCommentEdit(commentId) {
    if (originalCommentContent !== null) {
        const commentElement = document.querySelector(`[data-comment-id="${commentId}"]`);
        const contentDiv = commentElement.querySelector('.comment-content-text');
        contentDiv.textContent = originalCommentContent;
        originalCommentContent = null;
    }
}

// 댓글 삭제 처리
function handleCommentDelete() {
    if (!currentSelectedComment) return;

    if (confirm('정말로 삭제하시겠습니까?')) {
        fetch(`/api/comments/${currentSelectedComment.id}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (response.ok) {
                alert('댓글이 삭제되었습니다.');
                closeCommentModal();
                loadCommentList(1);
            } else {
                alert('댓글 삭제 실패');
            }
        })
        .catch(error => {
            console.error('삭제 요청 실패:', error);
            alert('댓글 삭제 중 오류가 발생했습니다.');
        });
    }
}

function loadCommentList(page = 1) {
    const commentList = document.getElementById('comment-list');
    const currentPageElement = document.getElementById('currentPage');
    const totalPagesElement = document.getElementById('totalPages');
    const totalElementsElement = document.getElementById('totalElements');
    const pagination = document.getElementById('pagination');

    commentList.innerHTML = ''; // 새로운 댓글을 추가하기 전에 기존 목록을 초기화
    pagination.innerHTML = ''; // 페이지네이션을 업데이트하기 전에 초기화

    // API 엔드포인트
    const apiEndpoint = `/api/comments/myList?page=${page}`;

    // 댓글 목록 가져오기
    fetch(apiEndpoint)
        .then(response => response.json())
        .then(data => {
            const comments = data.content;  // API 응답에서 "content" 필드에 댓글이 담겨 있다고 가정
            const totalElements = data.totalElements;
            const totalPages = data.totalPages;
            const currentPage = data.currentPage;

            // 페이지 정보 업데이트
            currentPageElement.textContent = currentPage;
            totalPagesElement.textContent = totalPages;
            totalElementsElement.textContent = totalElements;

            if (comments.length === 0) {
                commentList.innerHTML = '<p>댓글이 없습니다.</p>';
            } else {
                comments.forEach(comment => {
                    const commentItem = document.createElement('div');
                    commentItem.classList.add('post-list-item');
                    commentItem.setAttribute('data-comment-id', comment.id);
                    commentItem.style.cursor = 'pointer';

                    // 롱클릭 이벤트
                    commentItem.addEventListener('mousedown', (e) => {
                        longPressTimer = setTimeout(() => {
                            e.preventDefault();
                            openCommentModal(comment, commentItem);
                        }, 500);
                    });

                    commentItem.addEventListener('mouseup', () => {
                        clearTimeout(longPressTimer);
                    });

                    commentItem.addEventListener('mouseleave', () => {
                        clearTimeout(longPressTimer);
                    });

                    // 터치 이벤트 (모바일)
                    commentItem.addEventListener('touchstart', (e) => {
                        longPressTimer = setTimeout(() => {
                            e.preventDefault();
                            openCommentModal(comment, commentItem);
                        }, 500);
                    });

                    commentItem.addEventListener('touchend', () => {
                        clearTimeout(longPressTimer);
                    });

                    commentItem.addEventListener('touchmove', () => {
                        clearTimeout(longPressTimer);
                    });

                    // 프로필 이미지
                    const img = document.createElement('img');
                    img.setAttribute('src', comment.authorProfileImageUrl || '/img/undraw_profile.svg');
                    img.setAttribute('alt', '프로필 이미지');
                    commentItem.appendChild(img);

                    // 세부 사항 (댓글 내용, 닉네임)
                    const details = document.createElement('div');
                    details.classList.add('details');

                    const contentDiv = document.createElement('div');
                    contentDiv.className = 'comment-content-text';
                    contentDiv.textContent = comment.content;
                    contentDiv.style.fontWeight = 'bold';
                    contentDiv.style.marginBottom = '8px';
                    details.appendChild(contentDiv);

                    const nickname = document.createElement('p');
                    nickname.classList.add('nickname');
                    nickname.textContent = `작성자: ${comment.authorNickname}`;
                    details.appendChild(nickname);

                    commentItem.appendChild(details);

                    // 정보 (작성일, 수정일)
                    const info = document.createElement('div');
                    info.classList.add('info');

                    const formattedCreatedDate = formatDate(comment.createdAt);
                    const createdDate = document.createElement('p');
                    createdDate.textContent = `작성일: ${formattedCreatedDate}`;
                    info.appendChild(createdDate);

                    if (comment.updatedAt !== comment.createdAt) {
                        const formattedUpdatedDate = formatDate(comment.updatedAt);
                        const updatedDate = document.createElement('p');
                        updatedDate.textContent = `수정일: ${formattedUpdatedDate}`;
                        info.appendChild(updatedDate);
                    }

                    commentItem.appendChild(info);

                    // 댓글 항목을 리스트에 추가
                    commentList.appendChild(commentItem);
                });

                // 페이지네이션: 이전, 페이지 번호, 다음 버튼 추가
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
                        loadCommentList(pageNumber);
                    };
                    li.appendChild(a);
                    return li;
                };

                // 이전 버튼
                if (currentPage > 1) {
                    pagination.append(createPageLink(currentPage - 1, "이전", false));
                } else {
                    pagination.append(createPageLink(1, "이전", false));
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
                    pagination.append(createPageLink(totalPages, "다음", false));
                }
            }
        })
        .catch(error => {
            console.error('댓글 목록 조회 실패:', error);
            commentList.innerHTML = '<p>댓글 목록을 불러오는 중 오류가 발생했습니다.</p>';
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