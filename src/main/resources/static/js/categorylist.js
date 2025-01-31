function loadPosts(page = 1) {
    const postList = document.getElementById('post-list');
    const pagination = document.getElementById('pagination');
    const sort = document.getElementById('sortSelect').value;
    const category = new URLSearchParams(window.location.search).get('category') || 'free';

    // Update page title based on category
    const pageTitle = document.querySelector('.page-title');
    if (category === 'free') {
        pageTitle.textContent = '자유 게시판입니다. 자유롭게 게시글을 작성해주세요.';
    } else if (category === 'knowledge') {
        pageTitle.textContent = '정보 게시판입니다. 멘토들의 유익한 글이 있는 곳입니다.';
    } else if (category === 'qna') {
        pageTitle.textContent = 'Q/A 게시판입니다. 자유롭게 질의 응답을 해주세요.';
    }

    // Update page info
    const currentPageElement = document.getElementById('currentPage');
    const totalPagesElement = document.getElementById('totalPages');
    const totalElementsElement = document.getElementById('totalElements');
    
    postList.innerHTML = ''; // Clear the list before adding new posts
    pagination.innerHTML = ''; // Clear the pagination before updating

    // Example API endpoint to fetch posts - replace with your actual API or data fetching method
    const apiEndpoint = `/api/posts?category=${category}&sort=${sort}&page=${page}`;

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
                    nickname.textContent = `닉네임: ${post.nickname}`;
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
                    a.setAttribute('href', '#');
                    a.textContent = text;
                    a.addEventListener('click', (e) => {
                        e.preventDefault();
                        loadPosts(pageNumber);
                    });

                    li.appendChild(a);
                    return li;
                };

                // Previous button
                if (currentPage > 1) {
                    pagination.appendChild(createPageLink(currentPage - 1, '이전', false));
                }

                // Page numbers
                for (let i = 1; i <= totalPages; i++) {
                    pagination.appendChild(createPageLink(i, i, i === currentPage));
                }

                // Next button
                if (currentPage < totalPages) {
                    pagination.appendChild(createPageLink(currentPage + 1, '다음', false));
                }
            }
        })
        .catch(error => {
            console.error('Error fetching posts:', error);
            postList.innerHTML = '<p>게시글을 불러오는 데 실패했습니다.</p>';
        });
}

// 초기 카테고리 로드
loadPosts();
