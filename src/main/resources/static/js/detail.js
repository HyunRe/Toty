// detail.js (수정된 버전)
// 서버 이벤트 이름: "connect", "comment"

let postId;
let commentEventSource = null;
let likeEventSource = null;

document.addEventListener('DOMContentLoaded', () => {
  const pathSegments = window.location.pathname.split('/');
  postId = pathSegments[2] === 'posts' ? pathSegments[3] : (pathSegments[3] || null);
  const page = parseInt(new URLSearchParams(window.location.search).get("page")) || 1;

  loadPosts(postId);
  loadComments(postId, page);

  // SSE 자동 연결 (댓글, 좋아요만)
  if (postId) {
    commentEventSource = connectCommentSSE(postId);
    likeEventSource = connectLikeSSE(postId);
  }

  // 알림 SSE는 header.html에서 전역으로 연결됨

  // 게시글 작성자 클릭 이벤트 추가
  setupPostAuthorClickHandlers();
});

// 페이지 떠날 때 EventSource 정리
window.addEventListener('beforeunload', () => {
  if (commentEventSource) commentEventSource.close();
  if (likeEventSource) likeEventSource.close();
  // 알림 SSE는 header.html에서 관리되므로 여기서 닫지 않음
});

/* ---------------------------
   공통 DOM 레퍼런스
   --------------------------- */
const commentList = document.getElementById("comment-list");
const currentPageElement = document.getElementById('currentPage');
const totalPagesElement = document.getElementById('totalPages');
const totalElementsElement = document.getElementById('totalElements');
const pagination = document.getElementById('pagination');

/* ---------------------------
   백오프 기반 EventSource 유틸
   --------------------------- */
function createEventSourceWithBackoff(url, handlers = {}) {
  let es = null;
  let backoff = 1000;
  const maxBackoff = 30000;
  let shouldReconnect = true;
  let reconnectTimeout = null;

  function connect() {
    es = new EventSource(url);

    if (handlers.onopen) es.onopen = handlers.onopen;
    if (handlers.onmessage) es.onmessage = handlers.onmessage;

    if (handlers.events) {
      Object.entries(handlers.events).forEach(([name, fn]) => {
        es.addEventListener(name, fn);
      });
    }

    es.onerror = (err) => {
      if (handlers.onerror) handlers.onerror(err);
      try { es.close(); } catch (e) {}

      if (!shouldReconnect) return;

      // 기존 재연결 타이머 취소
      if (reconnectTimeout) clearTimeout(reconnectTimeout);

      reconnectTimeout = setTimeout(() => {
        backoff = Math.min(backoff * 2, maxBackoff);
        console.log(`재연결 시도 (${backoff}ms 후)...`);
        connect();
      }, backoff);
    };
  }

  connect();

  return {
    close: () => {
      shouldReconnect = false;
      if (reconnectTimeout) clearTimeout(reconnectTimeout);
      try { if (es) es.close(); } catch (e) {}
    }
  };
}

/* ---------------------------
   게시글 정보 로드
   --------------------------- */
function loadPosts(postId) {
  const apiEndpoint = `/api/posts/${postId}/detail`;

  fetch(apiEndpoint)
    .then(response => {
      if (!response.ok) throw new Error('게시글 로드 실패: ' + response.status);
      return response.json();
    })
    .then(data => {
      const post = {
        postCategory: data.postCategory,
        title: data.title,
        nickname: data.nickname,
        profileImageUrl: data.profileImageUrl,
        authorId: data.authorId,
        viewCount: data.viewCount,
        likeCount: data.likeCount,
        earliestTime: data.earliestTime ? new Date(data.earliestTime) : null,
        content: data.content,
        tags: data.tags || [],
        isLiked: data.isLiked,
        isScraped: data.isScraped
      };

      // 제목 설정
      if (post.postCategory == "Qna") {
        document.getElementById("page-title").textContent = "질문 게시판";
      } else if (post.postCategory == "Knowledge") {
        document.getElementById("page-title").textContent = "지식 게시판";
      } else if (post.postCategory == "General") {
        document.getElementById("page-title").textContent = "자유 게시판";
      }

      document.getElementById("post-title").textContent = post.title;
      const nicknameElement = document.getElementById("nickname");
      nicknameElement.textContent = post.nickname;
      nicknameElement.dataset.authorId = post.authorId;

      const profileImageElement = document.getElementById("profile-image");
      profileImageElement.src = post.profileImageUrl || "/img/undraw_profile.svg";
      profileImageElement.dataset.authorId = post.authorId;

      document.getElementById("view-count").textContent = post.viewCount;
      document.getElementById("like-count").textContent = post.likeCount;
      document.getElementById("post-time").textContent = post.earliestTime ? post.earliestTime.toLocaleString() : '';

      // 태그
      post.tags = document.getElementById("postTags").value;
      updatePostTags(post.tags, post.postCategory);

      // 좋아요/스크랩 초기 상태
      fetch(`/api/posts/${postId}/like-status`)
        .then(r => r.json())
        .then(isLiked => {
          toggleButtonState(document.getElementById('like-btn'), document.getElementById('like-icon'), isLiked, "bi-heart-fill", "bi-heart");
        }).catch(()=>{});
      fetch(`/api/posts/${postId}/scrape-status`)
        .then(r => r.json())
        .then(isScraped => {
          toggleButtonState(document.getElementById('save-btn'), document.getElementById('save-icon'), isScraped, "bi-bookmark-fill", "bi-bookmark");
        }).catch(()=>{});

      attachLikeSaveHandlers(postId);
    })
    .catch(error => {
      console.error('게시글을 가져오는 중 오류가 발생했습니다:', error);
    });
}

function toggleButtonState(button, icon, isActive, iconOnClass, iconOffClass) {
  button.classList.toggle("active", isActive);
  icon.classList.remove(isActive ? iconOffClass : iconOnClass);
  icon.classList.add(isActive ? iconOnClass : iconOffClass);
}

/* ---------------------------
   좋아요 / 스크랩 핸들러
   --------------------------- */
function attachLikeSaveHandlers(postId) {
  const likeButton = document.getElementById('like-btn');
  const likeIcon = document.getElementById('like-icon');
  const likeCountElement = document.getElementById("like-count");

  const saveButton = document.getElementById('save-btn');
  const saveIcon = document.getElementById('save-icon');

  likeButton.addEventListener('click', () => {
    const isActive = likeButton.classList.contains("active");
    const action = isActive ? 'unlike' : 'Like';

    fetch(`/api/posts/${postId}/like`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ likeAction: action })
    })
    .then(res => {
      if (!res.ok) throw new Error('좋아요 처리 실패');
      return res.json();
    })
    .then(likeCount => {
      // 서버 응답 받은 후 UI 업데이트 (SSE에서도 업데이트하지만 즉각 반영 위해)
      toggleButtonState(likeButton, likeIcon, !isActive, "bi-heart-fill", "bi-heart");
      likeCountElement.textContent = likeCount;
    })
    .catch(err => {
      console.error('좋아요 토글 처리 중 오류 발생:', err);
      alert('좋아요 처리 중 오류가 발생했습니다.');
    });
  });

  saveButton.addEventListener('click', () => {
    const isActive = saveButton.classList.contains("active");
    const action = isActive ? 'cancel' : 'scrape';

    fetch(`/api/posts/${postId}/scrape`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ scrapeAction: action })
    })
    .then(res => {
      if (!res.ok) throw new Error('스크랩 처리 실패');
      return res.json();
    })
    .then(() => {
      // 서버 응답 받은 후 UI 업데이트
      toggleButtonState(saveButton, saveIcon, !isActive, "bi-bookmark-fill", "bi-bookmark");
    })
    .catch(err => {
      console.error('스크랩 처리 중 오류:', err);
      alert('스크랩 처리 중 오류가 발생했습니다.');
    });
  });
}

/* ---------------------------
   댓글 로딩 / 렌더링
   --------------------------- */
function loadComments(postId, page = 1, size = 10) {
  const apiEndpoint = `/api/comments/list?postId=${postId}&page=${page}`;

  fetch(apiEndpoint)
    .then(response => {
      if (!response.ok) throw new Error('댓글 로드 실패');
      return response.json();
    })
    .then(data => {
      const comments = data.content || [];
      currentPageElement.textContent = data.currentPage || page;
      totalPagesElement.textContent = data.totalPages || 1;
      totalElementsElement.textContent = data.totalElements || comments.length;

      // 댓글 리스트 초기화 (페이지네이션 클릭 시)
      commentList.innerHTML = '';
      pagination.innerHTML = '';

      if (comments.length === 0) {
        commentList.innerHTML = '<p>댓글이 없습니다.</p>';
      } else {
        comments.forEach(renderComment);
        renderPagination(data.currentPage || page, data.totalPages || 1);
      }
    })
    .catch(err => {
      console.error('댓글 로드 오류:', err);
    });
}

function renderPagination(currentPage, totalPages) {
  pagination.innerHTML = '';
  for (let i = 1; i <= totalPages; i++) {
    const pageBtn = document.createElement('button');
    pageBtn.textContent = i;
    pageBtn.disabled = (i === currentPage);
    pageBtn.addEventListener('click', () => loadComments(postId, i));
    pagination.appendChild(pageBtn);
  }
}

/* ---------------------------
   SSE: 댓글 연결
   --------------------------- */
function connectCommentSSE(postId) {
  const url = `/api/sse/posts/${postId}/comments`;
  const handlers = {
    onopen: (e) => {
      console.log('댓글 SSE 연결됨', e);
    },
    onerror: (err) => {
      console.warn('댓글 SSE 오류', err);
    },
    events: {
      "connect": (e) => {
        console.log('댓글 SSE 초기화:', e.data);
      },
      "comment": (e) => {
        try {
          if (e.data === "ping") return;

          const eventData = JSON.parse(e.data);
          console.log('댓글 이벤트 수신:', eventData);

          // 서버: CommentEvent { type: "CREATE", commentDto: CommentDto }
          const commentData = eventData.commentDto || eventData.comment || eventData;

          if (eventData.type === "CREATE") {
            renderComment(commentData);
          } else if (eventData.type === "UPDATE") {
            updateComment(commentData);
          } else if (eventData.type === "DELETE") {
            removeComment(commentData.id);
          }
        } catch (err) {
          console.error('댓글 SSE 데이터 파싱 오류:', err, e.data);
        }
      }
    }
  };

  return createEventSourceWithBackoff(url, handlers);
}

/* ---------------------------
   SSE: 좋아요 연결
   --------------------------- */
function connectLikeSSE(postId) {
  const url = `/api/sse/posts/${postId}/likes`;
  const handlers = {
    onopen: () => console.log('좋아요 SSE 연결됨'),
    onerror: (err) => console.warn('좋아요 SSE 오류', err),
    events: {
      "connect": (e) => {
        console.log('좋아요 SSE 초기화:', e.data);
      },
      "like": (e) => {
        try {
          if (e.data === "ping") return;

          const likeEvent = JSON.parse(e.data);
          console.log('좋아요 이벤트 수신:', likeEvent);

          // 서버: PostLikeEvent { type: "LIKE"|"UNLIKE", postLikeDto: {...}, currentLikeCount: 10 }
          const likeCountElement = document.getElementById("like-count");
          if (likeCountElement && likeEvent.currentLikeCount != null) {
            likeCountElement.textContent = likeEvent.currentLikeCount;
          }

          // 현재 사용자가 좋아요를 눌렀는지 확인하여 버튼 상태 업데이트
          const currentUserId = document.body.dataset.currentUserId;
          if (likeEvent.postLikeDto && String(likeEvent.postLikeDto.userId) === String(currentUserId)) {
            const likeButton = document.getElementById('like-btn');
            const likeIcon = document.getElementById('like-icon');
            const isLiked = likeEvent.type === "LIKE";
            toggleButtonState(likeButton, likeIcon, isLiked, "bi-heart-fill", "bi-heart");
          }
        } catch (err) {
          console.error('좋아요 SSE 데이터 파싱 오류:', err, e.data);
        }
      }
    }
  };

  return createEventSourceWithBackoff(url, handlers);
}

/* ---------------------------
   SSE: 알림 연결
   --------------------------- */
// 알림 SSE는 header.html에서 전역으로 연결됨 (모든 페이지에서 사용)

/* ---------------------------
   댓글 렌더/수정/삭제 도우미
   --------------------------- */
function updateComment(comment) {
  const existing = document.querySelector(`.comment-item[data-id='${comment.id}']`);
  if (existing) {
    const contentText = existing.querySelector('.content-text');
    if (contentText) contentText.textContent = comment.content;
    const timeText = existing.querySelector('.comment-time');
    if (timeText) {
      const dateStr = comment.updatedAt || comment.createdAt;
      timeText.textContent = dateStr ? formatDateTime(dateStr) : '';
    }
  }
}

function formatDateTime(dateStr) {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: true
  });
}

function removeComment(commentId) {
  const commentElement = document.querySelector(`.comment-item[data-id='${commentId}']`);
  if (commentElement) {
    commentElement.remove();
  }
}

function renderComment(comment) {
  // 이미 존재하는 댓글인지 확인 (중복 방지)
  const existing = document.querySelector(`.comment-item[data-id='${comment.id}']`);
  if (existing) {
    console.log('이미 존재하는 댓글:', comment.id);
    return;
  }

  const commentItem = document.createElement('div');
  commentItem.classList.add('comment-item');
  commentItem.dataset.id = comment.id;
  // 정렬을 위해 timestamp 저장
  commentItem.dataset.timestamp = new Date(comment.createdAt).getTime();

  const img = document.createElement('img');
  img.src = comment.authorProfileImageUrl || '/img/undraw_profile.svg';
  img.alt = '프로필 이미지';
  img.style.width = '40px';
  img.style.height = '40px';
  img.style.borderRadius = '50%';
  commentItem.appendChild(img);

  const commentDetails = document.createElement('div');
  commentDetails.classList.add('comment-details');
  commentDetails.style.flex = '1';
  commentDetails.style.width = '100%';

  const nickname = document.createElement('p');
  nickname.style.fontWeight = 'bold';
  nickname.style.marginBottom = '5px';
  nickname.style.margin = '0 0 5px 0';
  nickname.textContent = comment.authorNickname || '익명';
  commentDetails.appendChild(nickname);

  const content = document.createElement('p');
  content.classList.add('content-text');
  content.style.margin = '0 0 8px 0';
  content.textContent = comment.content;
  commentDetails.appendChild(content);

  const commentMeta = document.createElement('div');
  commentMeta.classList.add('comment-meta');
  commentMeta.style.display = 'flex';
  commentMeta.style.alignItems = 'center';
  commentMeta.style.gap = '10px';
  commentMeta.style.width = '100%';

  const time = document.createElement('span');
  time.classList.add('comment-time');
  time.style.fontSize = '13px';
  time.style.color = '#666';
  const dateStr = comment.createdAt || comment.updatedAt;
  const formattedTime = dateStr ? formatDateTime(dateStr) : '시간 정보 없음';
  time.textContent = formattedTime;
  console.log('댓글 ID:', comment.id, '시간:', formattedTime, '원본:', dateStr, 'comment 객체:', comment);
  commentMeta.appendChild(time);

  // 수정/삭제 버튼 (내 댓글인 경우만)
  const currentUserId = document.body.dataset.currentUserId;
  if (comment.authorId && String(comment.authorId) === String(currentUserId)) {
    const settingsMenu = document.createElement('div');
    settingsMenu.style.display = 'flex';
    settingsMenu.style.gap = '8px';
    settingsMenu.style.marginLeft = 'auto';

    // 편집 버튼 (연필 아이콘)
    const editBtn = document.createElement('button');
    editBtn.innerHTML = '✏️';
    editBtn.title = '수정';
    editBtn.style.cssText = `
      background: none;
      border: none;
      cursor: pointer;
      font-size: 16px;
      padding: 4px;
      opacity: 0.7;
      transition: opacity 0.2s;
    `;
    editBtn.onmouseenter = () => editBtn.style.opacity = '1';
    editBtn.onmouseleave = () => editBtn.style.opacity = '0.7';
    editBtn.onclick = () => {
      const original = content.textContent;
      const editForm = document.createElement('div');
      editForm.classList.add('edit-form');

      const textarea = document.createElement('textarea');
      textarea.value = original;
      editForm.appendChild(textarea);

      const submitBtn = document.createElement('button');
      submitBtn.textContent = '확인';
      submitBtn.onclick = () => {
        const updatedContent = textarea.value;
        fetch(`/api/comments/${comment.id}`, {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ content: updatedContent })
        })
        .then(res => {
          if (!res.ok) throw new Error('수정 실패');
          return res.json();
        })
        .then(data => {
          content.textContent = data.content;
          editForm.remove();
        })
        .catch(err => {
          console.error('댓글 수정 실패:', err);
          alert('댓글 수정 중 오류가 발생했습니다.');
        });
      };

      const cancelBtn = document.createElement('button');
      cancelBtn.textContent = '취소';
      cancelBtn.onclick = () => editForm.remove();

      editForm.appendChild(submitBtn);
      editForm.appendChild(cancelBtn);
      commentItem.appendChild(editForm);
    };

    // 삭제 버튼 (X 아이콘)
    const deleteBtn = document.createElement('button');
    deleteBtn.innerHTML = '❌';
    deleteBtn.title = '삭제';
    deleteBtn.style.cssText = `
      background: none;
      border: none;
      cursor: pointer;
      font-size: 16px;
      padding: 4px;
      opacity: 0.7;
      transition: opacity 0.2s;
    `;
    deleteBtn.onmouseenter = () => deleteBtn.style.opacity = '1';
    deleteBtn.onmouseleave = () => deleteBtn.style.opacity = '0.7';
    deleteBtn.onclick = () => showDeleteModal(comment.id);

    settingsMenu.appendChild(editBtn);
    settingsMenu.appendChild(deleteBtn);
    commentMeta.appendChild(settingsMenu);
  }

  commentDetails.appendChild(commentMeta);

  commentItem.appendChild(commentDetails);

  // "댓글이 없습니다" 메시지 제거
  const emptyMessage = commentList.querySelector('p');
  if (emptyMessage && emptyMessage.textContent === '댓글이 없습니다.') {
    emptyMessage.remove();
  }

  // 최신순 정렬하여 삽입
  const existingComments = Array.from(commentList.querySelectorAll('.comment-item'));
  const newTimestamp = parseInt(commentItem.dataset.timestamp);

  let inserted = false;
  for (let i = 0; i < existingComments.length; i++) {
    const existingTimestamp = parseInt(existingComments[i].dataset.timestamp);
    // 새 댓글이 더 최신이면 그 앞에 삽입
    if (newTimestamp > existingTimestamp) {
      commentList.insertBefore(commentItem, existingComments[i]);
      inserted = true;
      break;
    }
  }

  // 가장 오래된 댓글이거나 리스트가 비어있으면 맨 뒤에 추가
  if (!inserted) {
    commentList.appendChild(commentItem);
  }

  // 댓글 작성자 클릭 이벤트 추가
  if (comment.authorId) {
    setupCommentAuthorClickHandler(commentItem, comment.authorId);
  }
}

/* ---------------------------
   댓글 작성
   --------------------------- */
document.getElementById("comment-submit-btn").addEventListener('click', () => {
  const commentContentEl = document.getElementById('comment-content');
  const content = commentContentEl.value.trim();
  if (!content) {
    alert("댓글 내용을 입력해주세요.");
    return;
  }

  fetch(`/api/comments/create?postId=${postId}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ content })
  })
  .then(response => {
    if (!response.ok) throw new Error('댓글 작성 실패');
    commentContentEl.value = '';
  })
  .catch(err => {
    console.error('댓글 작성 오류:', err);
    alert('댓글 작성 중 오류가 발생했습니다.');
  });
});

/* ---------------------------
   알림 토스트 표시
   --------------------------- */
function showToastNotification(data) {
  console.log('알림 표시:', data);

  // 간단한 토스트 (실제로는 커스텀 UI 라이브러리 사용 권장)
  const toast = document.createElement('div');
  toast.style.cssText = `
    position: fixed;
    top: 20px;
    right: 20px;
    background: #333;
    color: white;
    padding: 15px 20px;
    border-radius: 5px;
    z-index: 10000;
    max-width: 300px;
    box-shadow: 0 4px 6px rgba(0,0,0,0.3);
  `;
  toast.textContent = data.message || '새로운 알림이 도착했습니다.';
  document.body.appendChild(toast);

  // 3초 후 제거
  setTimeout(() => {
    toast.style.transition = 'opacity 0.3s';
    toast.style.opacity = '0';
    setTimeout(() => toast.remove(), 300);
  }, 3000);
}

/* ---------------------------
   태그 렌더링
   --------------------------- */
function updatePostTags(tags, postCategory) {
  const postTagsContainer = document.getElementById("post-tags");
  postTagsContainer.innerHTML = "";

  if (postCategory === "Qna" && tags) {
    postTagsContainer.style.display = "flex";
    if (typeof tags === "string") {
      try { tags = JSON.parse(tags); } catch(e) { tags = []; }
    }
    (tags || []).forEach(tag => {
      const tagElement = document.createElement("span");
      tagElement.classList.add("tag-item");
      tagElement.textContent = `#${tag.name || tag}`;
      postTagsContainer.appendChild(tagElement);
    });
  } else {
    postTagsContainer.style.display = "none";
  }
}

/* ---------------------------
   댓글 삭제 확인 모달
   --------------------------- */
function showDeleteModal(commentId) {
  // 모달 오버레이
  const modalOverlay = document.createElement('div');
  modalOverlay.style.cssText = `
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 10000;
  `;

  // 모달 컨텐츠
  const modal = document.createElement('div');
  modal.style.cssText = `
    background: white;
    padding: 30px;
    border-radius: 10px;
    box-shadow: 0 4px 20px rgba(0,0,0,0.3);
    max-width: 400px;
    width: 90%;
  `;

  // 제목
  const title = document.createElement('h3');
  title.textContent = '댓글 삭제';
  title.style.cssText = `
    margin: 0 0 15px 0;
    font-size: 20px;
    color: #333;
  `;

  // 메시지
  const message = document.createElement('p');
  message.textContent = '정말로 이 댓글을 삭제하시겠습니까? 삭제 후에는 복구할 수 없습니다.';
  message.style.cssText = `
    margin: 0 0 25px 0;
    color: #666;
    line-height: 1.5;
  `;

  // 버튼 컨테이너
  const buttonContainer = document.createElement('div');
  buttonContainer.style.cssText = `
    display: flex;
    gap: 10px;
    justify-content: flex-end;
  `;

  // 취소 버튼
  const cancelBtn = document.createElement('button');
  cancelBtn.textContent = '취소';
  cancelBtn.style.cssText = `
    padding: 10px 20px;
    border: 1px solid #ddd;
    background: white;
    color: #333;
    border-radius: 5px;
    cursor: pointer;
    font-size: 14px;
    transition: background 0.2s;
  `;
  cancelBtn.onmouseenter = () => cancelBtn.style.background = '#f5f5f5';
  cancelBtn.onmouseleave = () => cancelBtn.style.background = 'white';
  cancelBtn.onclick = () => modalOverlay.remove();

  // 삭제 버튼
  const confirmBtn = document.createElement('button');
  confirmBtn.textContent = '삭제';
  confirmBtn.style.cssText = `
    padding: 10px 20px;
    border: none;
    background: #dc3545;
    color: white;
    border-radius: 5px;
    cursor: pointer;
    font-size: 14px;
    transition: background 0.2s;
  `;
  confirmBtn.onmouseenter = () => confirmBtn.style.background = '#c82333';
  confirmBtn.onmouseleave = () => confirmBtn.style.background = '#dc3545';
  confirmBtn.onclick = () => {
    // 삭제 API 호출
    fetch(`/api/comments/${commentId}`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' }
    })
    .then(res => {
      if (!res.ok) throw new Error('삭제 실패');
      modalOverlay.remove();
      // SSE로 삭제 이벤트가 오면 자동으로 제거됨
    })
    .catch(err => {
      console.error('댓글 삭제 실패:', err);
      alert('댓글 삭제 중 오류가 발생했습니다.');
      modalOverlay.remove();
    });
  };

  // 모달 구성
  buttonContainer.appendChild(cancelBtn);
  buttonContainer.appendChild(confirmBtn);
  modal.appendChild(title);
  modal.appendChild(message);
  modal.appendChild(buttonContainer);
  modalOverlay.appendChild(modal);

  // 오버레이 클릭 시 닫기
  modalOverlay.onclick = (e) => {
    if (e.target === modalOverlay) {
      modalOverlay.remove();
    }
  };

  document.body.appendChild(modalOverlay);
}

/* ---------------------------
   사용자 프로필 모달
   --------------------------- */
function setupPostAuthorClickHandlers() {
  // 게시글 로드 후 프로필 이미지와 닉네임에 클릭 이벤트 추가
  const profileImage = document.getElementById('profile-image');
  const nicknameElement = document.getElementById('nickname');

  if (profileImage) {
    profileImage.style.cursor = 'pointer';
    profileImage.addEventListener('click', () => {
      const authorId = profileImage.dataset.authorId;
      if (authorId) {
        showUserProfileModal(authorId);
      }
    });
  }

  if (nicknameElement) {
    nicknameElement.style.cursor = 'pointer';
    nicknameElement.addEventListener('click', () => {
      const authorId = nicknameElement.dataset.authorId;
      if (authorId) {
        showUserProfileModal(authorId);
      }
    });
  }
}

function setupCommentAuthorClickHandler(commentItem, authorId) {
  const img = commentItem.querySelector('img');
  const nickname = commentItem.querySelector('.comment-details > p:first-child');

  if (img) {
    img.style.cursor = 'pointer';
    img.onclick = () => showUserProfileModal(authorId);
  }

  if (nickname) {
    nickname.style.cursor = 'pointer';
    nickname.onclick = () => showUserProfileModal(authorId);
  }
}

async function showUserProfileModal(userId) {
  try {
    const currentUserId = document.body.dataset.currentUserId;
    const isMyProfile = currentUserId && (String(userId) === String(currentUserId));

    console.log('프로필 모달 열기:', {
      userId: userId,
      currentUserId: currentUserId,
      isMyProfile: isMyProfile,
      comparison: String(userId) === String(currentUserId)
    });

    // API 호출
    const endpoint = isMyProfile ? '/api/users/me' : `/api/users/${userId}`;
    const response = await fetch(endpoint);
    if (!response.ok) throw new Error('사용자 정보 로드 실패');

    const result = await response.json();
    const userInfo = result.data;

    // 모달 오버레이
    const modalOverlay = document.createElement('div');
    modalOverlay.style.cssText = `
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      justify-content: center;
      align-items: center;
      z-index: 10000;
    `;

    // 모달 컨텐츠
    const modal = document.createElement('div');
    modal.style.cssText = `
      background: white;
      padding: 30px;
      border-radius: 15px;
      box-shadow: 0 4px 20px rgba(0,0,0,0.3);
      max-width: 500px;
      width: 90%;
      max-height: 80vh;
      overflow-y: auto;
    `;

    // 프로필 이미지
    const profileImg = document.createElement('img');
    profileImg.src = userInfo.profileImgUrl || '/img/undraw_profile.svg';
    profileImg.alt = '프로필 이미지';
    profileImg.style.cssText = `
      width: 100px;
      height: 100px;
      border-radius: 50%;
      display: block;
      margin: 0 auto 20px auto;
      object-fit: cover;
    `;

    // 닉네임
    const nicknameEl = document.createElement('h2');
    nicknameEl.textContent = userInfo.nickname;
    nicknameEl.style.cssText = `
      text-align: center;
      margin: 0 0 10px 0;
      font-size: 24px;
      color: #333;
    `;

    // 팔로워/팔로잉 수
    const followStats = document.createElement('div');
    followStats.style.cssText = `
      display: flex;
      justify-content: center;
      gap: 30px;
      margin-bottom: 20px;
      padding-bottom: 20px;
      border-bottom: 1px solid #eee;
    `;

    const followerCount = document.createElement('div');
    followerCount.style.cssText = 'text-align: center;';
    followerCount.innerHTML = `
      <div style="font-size: 20px; font-weight: bold; color: #333;">${userInfo.followerCount || 0}</div>
      <div style="font-size: 14px; color: #666;">팔로워</div>
    `;

    const followingCount = document.createElement('div');
    followingCount.style.cssText = 'text-align: center;';
    followingCount.innerHTML = `
      <div style="font-size: 20px; font-weight: bold; color: #333;">${userInfo.followingCount || 0}</div>
      <div style="font-size: 14px; color: #666;">팔로잉</div>
    `;

    followStats.appendChild(followerCount);
    followStats.appendChild(followingCount);

    // 팔로우 버튼 (내 프로필이 아닌 경우만)
    let followBtn = null;
    let followStatusText = null;
    // 본인 프로필이 아닐 때만 팔로우 버튼 생성
    if (!isMyProfile && currentUserId) {
      console.log('✅ 팔로우 버튼 생성:', {
        isMyProfile,
        currentUserId,
        targetUserId: userId,
        isFollowing: userInfo.isFollowing  // 디버깅용
      });

      // 팔로우 상태 안내 문구
      if (userInfo.isFollowing) {
        console.log('🔵 팔로우 중이므로 안내 문구 생성');
        followStatusText = document.createElement('div');
        followStatusText.textContent = '이미 팔로우한 사용자입니다';
        followStatusText.style.cssText = `
          text-align: center;
          color: #6c757d;
          font-size: 13px;
          margin-bottom: 8px;
          font-weight: 500;
        `;
      } else {
        console.log('⚪ 팔로우 안 한 사용자');
      }

      followBtn = document.createElement('button');
      followBtn.textContent = userInfo.isFollowing ? '언팔로우' : '팔로우';
      followBtn.style.cssText = `
        width: 100%;
        padding: 12px;
        margin-bottom: 20px;
        border: none;
        border-radius: 8px;
        cursor: pointer;
        font-size: 16px;
        font-weight: bold;
        transition: background 0.2s;
        ${userInfo.isFollowing
          ? 'background: #6c757d; color: white;'
          : 'background: #007bff; color: white;'}
      `;

      followBtn.onclick = async () => {
        // 중복 클릭 방지
        if (followBtn.disabled) return;
        followBtn.disabled = true;
        const originalText = followBtn.textContent;
        followBtn.textContent = '처리중...';

        try {
          if (userInfo.isFollowing) {
            // 언팔로우
            const res = await fetch(`/api/follow/${userId}`, { method: 'DELETE' });
            if (!res.ok) throw new Error('언팔로우 실패');
            userInfo.isFollowing = false;
            userInfo.followerCount = Math.max(0, (userInfo.followerCount || 0) - 1);
            followBtn.textContent = '팔로우';
            followBtn.style.background = '#007bff';

            // 팔로우 상태 문구 제거
            if (followStatusText && followStatusText.parentNode) {
              followStatusText.remove();
              followStatusText = null;
            }
          } else {
            // 팔로우
            const res = await fetch('/api/follow/', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ id: userId })
            });
            if (!res.ok) throw new Error('팔로우 실패');
            userInfo.isFollowing = true;
            userInfo.followerCount = (userInfo.followerCount || 0) + 1;
            followBtn.textContent = '언팔로우';
            followBtn.style.background = '#6c757d';

            // 팔로우 상태 문구 추가
            if (!followStatusText) {
              followStatusText = document.createElement('div');
              followStatusText.textContent = '이미 팔로우한 사용자입니다';
              followStatusText.style.cssText = `
                text-align: center;
                color: #6c757d;
                font-size: 13px;
                margin-bottom: 8px;
                font-weight: 500;
              `;
              followBtn.parentNode.insertBefore(followStatusText, followBtn);
            }
          }

          // 팔로워 수 업데이트
          followerCount.innerHTML = `
            <div style="font-size: 20px; font-weight: bold; color: #333;">${userInfo.followerCount}</div>
            <div style="font-size: 14px; color: #666;">팔로워</div>
          `;
        } catch (err) {
          console.error('팔로우 처리 실패:', err);
          alert('팔로우 처리 중 오류가 발생했습니다.');
          followBtn.textContent = originalText;
        } finally {
          followBtn.disabled = false;
        }
      };

      followBtn.onmouseenter = () => {
        followBtn.style.opacity = '0.8';
      };
      followBtn.onmouseleave = () => {
        followBtn.style.opacity = '1';
      };
    } else {
      console.log('❌ 팔로우 버튼 생성 안함:', {
        isMyProfile: isMyProfile,
        currentUserId: currentUserId,
        reason: !currentUserId ? '로그인 안됨' : '본인 프로필'
      });
    }

    // 링크 섹션
    const linksSection = document.createElement('div');
    linksSection.style.cssText = 'margin-bottom: 20px;';

    if (userInfo.links && userInfo.links.length > 0) {
      const linksTitle = document.createElement('h4');
      linksTitle.textContent = '링크';
      linksTitle.style.cssText = `
        font-size: 16px;
        color: #333;
        margin-bottom: 10px;
      `;
      linksSection.appendChild(linksTitle);

      userInfo.links.forEach(link => {
        const linkItem = document.createElement('a');
        linkItem.href = link.url;
        linkItem.target = '_blank';
        linkItem.rel = 'noopener noreferrer';
        linkItem.textContent = `${link.site}`;
        linkItem.style.cssText = `
          display: block;
          padding: 10px;
          margin-bottom: 8px;
          background: #f8f9fa;
          border-radius: 5px;
          text-decoration: none;
          color: #007bff;
          transition: background 0.2s;
        `;
        linkItem.onmouseenter = () => linkItem.style.background = '#e9ecef';
        linkItem.onmouseleave = () => linkItem.style.background = '#f8f9fa';
        linksSection.appendChild(linkItem);
      });
    }

    // 기술 스택 섹션
    const tagsSection = document.createElement('div');
    tagsSection.style.cssText = 'margin-bottom: 20px;';

    if (userInfo.tags && userInfo.tags.length > 0) {
      const tagsTitle = document.createElement('h4');
      tagsTitle.textContent = '기술 스택';
      tagsTitle.style.cssText = `
        font-size: 16px;
        color: #333;
        margin-bottom: 10px;
      `;
      tagsSection.appendChild(tagsTitle);

      const tagsContainer = document.createElement('div');
      tagsContainer.style.cssText = `
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
      `;

      userInfo.tags.forEach(tag => {
        const tagEl = document.createElement('span');
        tagEl.textContent = `#${tag}`;
        tagEl.style.cssText = `
          padding: 6px 12px;
          background: #007bff;
          color: white;
          border-radius: 15px;
          font-size: 14px;
        `;
        tagsContainer.appendChild(tagEl);
      });

      tagsSection.appendChild(tagsContainer);
    }

    // 닫기 버튼
    const closeBtn = document.createElement('button');
    closeBtn.textContent = '닫기';
    closeBtn.style.cssText = `
      width: 100%;
      padding: 12px;
      border: 1px solid #ddd;
      background: white;
      color: #333;
      border-radius: 8px;
      cursor: pointer;
      font-size: 16px;
      transition: background 0.2s;
      margin-top: 10px;
    `;
    closeBtn.onmouseenter = () => closeBtn.style.background = '#f5f5f5';
    closeBtn.onmouseleave = () => closeBtn.style.background = 'white';
    closeBtn.onclick = () => modalOverlay.remove();

    // 모달 구성
    modal.appendChild(profileImg);
    modal.appendChild(nicknameEl);
    modal.appendChild(followStats);
    if (followStatusText) {
      console.log('📝 팔로우 상태 문구를 모달에 추가');
      modal.appendChild(followStatusText);
    } else {
      console.log('📝 팔로우 상태 문구 없음');
    }
    if (followBtn) modal.appendChild(followBtn);
    modal.appendChild(linksSection);
    modal.appendChild(tagsSection);
    modal.appendChild(closeBtn);
    modalOverlay.appendChild(modal);

    // 오버레이 클릭 시 닫기
    modalOverlay.onclick = (e) => {
      if (e.target === modalOverlay) {
        modalOverlay.remove();
      }
    };

    document.body.appendChild(modalOverlay);
  } catch (err) {
    console.error('사용자 프로필 로드 실패:', err);
    alert('사용자 정보를 불러오는 중 오류가 발생했습니다.');
  }
}