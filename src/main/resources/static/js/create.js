$(document).ready(function () {
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

    // 카테고리 선택 시 태그 활성화/비활성화
    $("#postCategory").change(function () {
        if ($(this).val() === "QnA") {
            $("#postTags").show(); // 태그 영역 표시
            $("#postTags input").prop("disabled", false); // 체크박스 활성화
        } else {
            $("#postTags").hide(); // 태그 영역 숨기기
            $("#postTags input").prop("disabled", true); // 체크박스 비활성화
        }
    }).trigger("change"); // 페이지 로드 시 초기 상태 설정

    // 게시글 제출
    $('#submitPost').click(function (event) {
        event.preventDefault();

        const postData = {
            title: $('#title').val(),
            postCategory: $('#postCategory').val(),
            postTags: $("input[name='postTags']:checked").map(function () {
                return this.value;
            }).get(),
            content: $('#content').val()
        };

        $.ajax({
            url: '/api/posts/create',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(postData),
            success: function (response) {
                alert('게시글이 성공적으로 등록되었습니다.');
                const category = $('#postCategory').val();
                window.location.href = `/view/posts/categoryList?postCategory=${category}`;
            },
            error: function () {
                alert('게시글 등록 실패');
            }
        });
    });
});
