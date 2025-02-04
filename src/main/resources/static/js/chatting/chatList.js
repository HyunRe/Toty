
async function fetchRoomList() {
    try {
        const loginId = $(".userId").val();
        const response = await fetch(`/v02/api/rooms`);
        if (response.ok) {
            const chatterList = await response.json();
            updateRoomList(chatterList, loginId);
        }
    } catch (error) {
        console.error('Failed to fetch roomList:', error);
    }
}

function updateRoomList(roomList, loginId) {

    const tableBody = document.querySelector('.table > tbody');
    tableBody.innerHTML = ''; // 기존 내용을 초기화

    roomList.forEach(room => {
        const row = document.createElement('tr');

        row.innerHTML = `
            <td style="text-align: center;">
                ${room.mentor.userName}
            </td>
            <td>
                <span style="font-weight: bold; font-size: 0.8rem">${room.roomName}</span>
            </td>
            <td style="text-align: center;">
                <span style="font-size: 0.8rem;">
                     ${room.userLimit}
                </span>
            </td>
            <td>
                <form action="/v02/api/participant/${room.id}/${loginId}" 
                    method="post">
                    <button type="submit"> 단톡 참석 </button>
                </form>
            </td>
        `;
        tableBody.appendChild(row);
    });
}

$(document).ready(function() {

    // $("form").on('submit', (e) => e.preventDefault());
    // $( "#connect" ).click(() => connect());
    // $( "#disconnect" ).click(() => disconnect());
    // $( "#send" ).click(() => sendMessage());

    $(".loginBtn").on("click", function() {
        var userId = $(this).data("user-id");
    
        $.ajax({
            type:"get",
            url:"/v02/api/login/" + userId,
            success:function(response) {
                // $(".userId").val(userId);
                alert(response);
            },
            error:function(xhr) {
                let response = xhr.responseJSON;
                console.log(response);
                alert("로그인 실패 \n" + response.message);
            }
        });
    });
    
    $(".createRoom").on("click", function() {
        var mId = $(this).data("user-id");
    
        var rr = $(".roomName-" + mId).val();
        var ul = $(".userLimit-" + mId).val();
        var params = {
                        "roomName": rr, "userLimit": ul
                      }
        $.ajax({
            type:"post",
            url:"/v02/api/room/" + mId,
            data: params,
            success:function(response) {
    
            },
            error:function(xhr) {
                let response = xhr.responseJSON;
                console.log(response);
                alert("단톡방 생성 실패 \n" + response.message);
            }
        });
    });
})
