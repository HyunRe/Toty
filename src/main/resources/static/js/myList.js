function changeCategory(category, page = 1) {
    // 왼쪽 메뉴 항목 초기화
    document.getElementById('profileMenu').classList.remove('active-menu');
    document.getElementById('myPostsMenu').classList.remove('active-menu');
    document.getElementById('savedPostsMenu').classList.remove('active-menu');

    // 카테고리에 따라 왼쪽 메뉴 활성화
    if (category === 'free') {
        document.getElementById('myPostsMenu').classList.add('active-menu');
    } else if (category === 'knowledge') {
        document.getElementById('myPostsMenu').classList.add('active-menu');
    } else if (category === 'qna') {
        document.getElementById('savedPostsMenu').classList.add('active-menu');
    }

    const postList = document.getElementById('post-list');
    const currentPageElement = document.getElementById('currentPage');
    const totalPagesElement = document.getElementById('totalPages');
    const totalElementsElement = document.getElementById('totalElements');
    const pagination = document.getElementById('pagination');

    postList.innerHTML = ''; // Clear the list before adding new posts
    pagination.innerHTML = ''; // Clear the pagination before updating

    // Example API endpoint to fetch posts - replace with your actual API or data fetching method
    const apiEndpoint = `/api/posts?category=${category}&page=${page}`;

    // Fetch posts based on the selected category and page
    fetch(apiEndpoint)
        .then(response => response.json())
        .then(data => {
            const posts = data.content;  // Assuming the API returns posts in "content" field
            const totalElements = data.totalElements;
            const totalPages = data.totalPages;
            const currentPage = data.number + 1;

            // Update the page info
            currentPageElement.textContent = currentPage;
            totalPagesElement.textContent = totalPages;
            totalElementsElement.textContent = totalElements;

            if (posts.length === 0) {
                postList.innerHTML = '<p>게시글이 없습니다.</p>';
            } else {
                posts.forEach(post => {
                    const postItem = document.createElement('div');
                    postItem.classList.add('post-list-item');
                    postItem.setAttribute('onclick', `location.href='/post/detail/${post.id}'`);
                    postItem.style.cursor = 'pointer';

                    // Profile Image
                    const img = document.createElement('img');
                    img.setAttribute('src', post.profileImageUrl || 'profile.jpg'); // Fallback to a default profile image
                    img.setAttribute('alt', '프로필 이미지');
                    postItem.appendChild(img);

                    // Details (Title, Nickname)
                    const details = document.createElement('div');
                    details.classList.add('details');
                    
                    const titleLink = document.createElement('a');
                    titleLink.setAttribute('href', `/post/detail/${post.id}`);
                    titleLink.textContent = post.title;
                    details.appendChild(titleLink);

                    const nickname = document.createElement('p');
                    nickname.classList.add('nickname');
                    nickname.textContent = `작성자: ${post.nickname}`;
                    details.appendChild(nickname);

                    // Add Mentor badge if the post author is a mentor
                    if (post.role === 'MENTOR') {
                        const badge = document.createElement('span');
                        badge.classList.add('badge', 'badge-primary');
                        badge.textContent = 'MENTOR';
                        details.appendChild(badge);
                    }

                    postItem.appendChild(details);

                    // Info (View Count, Likes, Date)
                    const info = document.createElement('div');
                    info.classList.add('info');
                    
                    const viewLike = document.createElement('p');
                    viewLike.textContent = `조회수: ${post.viewCount} / 좋아요: ${post.likeCount}`;
                    info.appendChild(viewLike);
                    
                    const date = document.createElement('p');
                    date.textContent = `작성일: ${new Date(post.earliestTime).toLocaleString()}`;
                    info.appendChild(date);

                    // 수정과 삭제 버튼 추가
                    const buttons = document.createElement('div');
                    buttons.classList.add('btn-group', 'btn-group-sm');

                    const editButton = document.createElement('button');
                    editButton.classList.add('btn', 'btn-warning');
                    editButton.textContent = '수정';
                    editButton.onclick = (e) => {
                        e.stopPropagation();
                        location.href = `/update.html?id=${post.id}`;
                    };

                    const deleteButton = document.createElement('button');
                    deleteButton.classList.add('btn', 'btn-danger');
                    deleteButton.textContent = '삭제';
                    deleteButton.onclick = (e) => {
                        e.stopPropagation();
                        if (confirm('정말로 삭제하시겠습니까?')) {
                            alert('게시글이 삭제되었습니다.');
                        }
                    };

                    buttons.appendChild(editButton);
                    buttons.appendChild(deleteButton);
                    postItem.appendChild(buttons);

                    postItem.appendChild(info);

                    // Append the post item to the post list
                    postList.appendChild(postItem);
                });

                // Pagination: Add previous, page numbers, and next
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
                        changeCategory(category, pageNumber);
                    };
                    li.appendChild(a);
                    return li;
                };

                // Adding previous, page numbers, and next buttons
                if (currentPage > 1) {
                    pagination.appendChild(createPageLink(currentPage - 1, '이전', false));
                }
                for (let i = 1; i <= totalPages; i++) {
                    pagination.appendChild(createPageLink(i, i, i === currentPage));
                }
                if (currentPage < totalPages) {
                    pagination.appendChild(createPageLink(currentPage + 1, '다음', false));
                }
            }
        });
}
