document.addEventListener('DOMContentLoaded', () => {
    // 게시글 정보와 댓글 로딩
    loadPostDetails();
    loadComments();
});

// 게시글 정보 로딩 함수
function loadPostDetails() {
    // 게시글 정보를 서버에서 가져오는 부분 (예시 데이터)
    const post = {
        title: "게시글 제목",
        nickname: "작성자 닉네임",
        profileImageUrl: "profile.jpg",
        viewCount: 100,
        likeCount: 50,
        earliestTime: new Date(),
        content: "게시글 내용이 들어갑니다.",
    };

    // 게시글 내용 추가
    document.getElementById("post-title").textContent = post.title;
    document.getElementById("nickname").textContent = post.nickname;
    document.getElementById("profile-image").src = post.profileImageUrl;
    document.getElementById("view-count").textContent = post.viewCount;
    document.getElementById("like-count").textContent = post.likeCount;
    document.getElementById("post-time").textContent = post.earliestTime.toLocaleString();
    document.getElementById("post-content").textContent = post.content;
}

// 댓글 로딩 함수
function loadComments() {
    // 예시 댓글 데이터
    const comments = [
        { profileImageUrl: "commenter1.jpg", nickname: "댓글작성자1", content: "댓글 내용1", earliestTime: new Date() },
        { profileImageUrl: "commenter2.jpg", nickname: "댓글작성자2", content: "댓글 내용2", earliestTime: new Date() }
    ];

    const commentList = document.getElementById("comment-list");
    comments.forEach(comment => {
        const commentItem = document.createElement('div');
        commentItem.classList.add('comment-item');

        // 프로필 이미지
        const img = document.createElement('img');
        img.setAttribute('src', comment.profileImageUrl);
        commentItem.appendChild(img);

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
            }
        };

        actions.appendChild(editBtn);
        actions.appendChild(deleteBtn);
        commentDetails.appendChild(actions);

        commentItem.appendChild(commentDetails);
        commentList.appendChild(commentItem);
    });
}

// myScrape.html로 이동하는 함수
function goToMyScrapePage() {
    window.location.href = "../user/myScrape.html";
}
