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
            url: '/upload-image', // 이미지 업로드 엔드포인트
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

    // postCategory가 변경되면 postTags 필드를 동적으로 활성화/비활성화합니다.
    $('#postCategory').on('change', function () {
        var category = $(this).val();
        if (category === 'Qna') {
            $('#tagsContainer').show();
            $('#postTags').prop('disabled', false);
        } else {
            $('#tagsContainer').hide();
            $('#postTags').prop('disabled', true);
        }
    });

    // 페이지 로딩 시 현재 카테고리에 맞는 상태로 태그를 설정
    var initialCategory = $('#postCategory').val();
    if (initialCategory === 'Qna') {
        $('#tagsContainer').show();
        $('#postTags').prop('disabled', false);
    } else {
        $('#tagsContainer').hide();
        $('#postTags').prop('disabled', true);
    }
});
