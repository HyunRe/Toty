$(document).ready(function () {
    const id = parseInt(document.getElementById("postId").value, 10);

    // 카테고리 값을 읽어옴
    const category = $("#postCategory span").text().trim(); // span의 텍스트 값 읽기
    console.log("Category: ", category); // 로그 찍기

    // Summernote 초기화
    $('#content').summernote({
        height: 300,
        placeholder: '여기에 내용을 작성하세요...',
        callbacks: {
            onImageUpload: function (files) {
                for (let i = 0; i < files.length; i++) {
                    uploadImage(files[i]);
                }
            }
        }
    });

    function uploadImage(file) {
        const formData = new FormData();
        formData.append("file", file);
        $.ajax({
            url: '/api/posts/upload-image', // 이미지 업로드 엔드포인트
            method: 'POST',
            data: formData,
            contentType: false,
            processData: false,
            success: function (url) {
                $('#content').summernote('insertImage', url);
            },
            error: function () {
                alert('이미지 업로드 실패');
            }
        });
    }

    // 게시글 수정
    $('#submitPost').click(function (event) {
        event.preventDefault();

        const postData = {
            title: $('#title').val(),
            postTags: $("input[name='postTags']:checked").map(function () {
                return this.value;
            }).get(),
            content: $('#content').val()
        };

        $.ajax({
            url: `/api/posts/${id}`,
            method: 'PATCH',
            contentType: 'application/json',
            data: JSON.stringify(postData),
            success: function (response) {
                alert("게시글이 수정되었습니다.");
                window.location.href = `/view/posts/myList?postCategory=${encodeURIComponent(category)}`;
            },
            error: function () {
                alert('게시글 수정 실패');
            }
        });
    });

    // 페이지 로드 시 postCategory 값에 따라 태그 활성화/비활성화
    if (category === "QnA") {
        $("#postTags").show(); // 태그 영역 표시
        $("#postTags input").prop("disabled", false); // 체크박스 활성화
    } else {
        $("#postTags").hide(); // 태그 영역 숨기기
        $("#postTags input").prop("disabled", true); // 체크박스 비활성화
    }
});
