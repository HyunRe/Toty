$(document).ready(function () {
    loadPosts();

    $("#sortSelect").on("change", function () {
        loadPosts(1);
    });
});

function loadPosts(page = 1) {
    const postList = $("#post-list");
    const pagination = $("#pagination");
    const sort = $("#sortSelect").val();
    
    // Update page info
    const currentPageElement = $("#currentPage");
    const totalPagesElement = $("#totalPages");
    const totalElementsElement = $("#totalElements");

    postList.html(""); // Clear the list before adding new posts
    pagination.html(""); // Clear the pagination before updating

    // Example API endpoint to fetch posts - replace with your actual API or data fetching method
    const apiEndpoint = `/api/posts?sort=${sort}&page=${page}`;

    // Fetch posts based on the selected category and page
    $.getJSON(apiEndpoint, function (data) {
        const posts = data.content;
        const totalElements = data.totalElements;
        const totalPages = data.totalPages;
        const currentPage = data.number + 1;

        // Update the page info
        currentPageElement.text(currentPage);
        totalPagesElement.text(totalPages);
        totalElementsElement.text(totalElements);

        if (posts.length === 0) {
            postList.html("<p>게시글이 없습니다.</p>");
        } else {
            posts.forEach(post => {
                const postItem = $(`
                    <div class="post-list-item" onclick="location.href='/post/detail/${post.id}'">
                        <img src="${post.profileImageUrl || 'profile.jpg'}" alt="프로필 이미지">
                        <div class="details">
                            <a href="/post/detail/${post.id}">${post.title}</a>
                            <p>닉네임: ${post.nickname} ${post.role === 'MENTOR' ? '<span class="badge badge-primary">MENTOR</span>' : ''}</p>
                        </div>
                        <div class="info">
                            <p>조회수: ${post.viewCount} / 좋아요: ${post.likeCount}</p>
                            <p>작성일: ${new Date(post.earliestTime).toLocaleString()}</p>
                        </div>
                    </div>
                `);
                postList.append(postItem);
            });

            // Pagination: Add previous, page numbers, and next
            const createPageLink = (pageNumber, text, isActive) => {
                const li = $(`<li class="page-item ${isActive ? 'active' : ''}"></li>`);
                const a = $(`<a class="page-link" href="#">${text}</a>`);
                a.on("click", (e) => {
                    e.preventDefault();
                    loadPosts(pageNumber);
                });
                li.append(a);
                return li;
            };

            // Previous button
            if (currentPage > 1) {
                pagination.append(createPageLink(currentPage - 1, "이전", false));
            }

            // Page numbers
            for (let i = 1; i <= totalPages; i++) {
                pagination.append(createPageLink(i, i, i === currentPage));
            }

            // Next button
            if (currentPage < totalPages) {
                pagination.append(createPageLink(currentPage + 1, "다음", false));
            }
        }
    }).fail(function () {
        postList.html("<p>게시글을 불러오는 데 실패했습니다.</p>");
    });
}
