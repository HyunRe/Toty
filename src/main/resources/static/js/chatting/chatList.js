let loginId;

let eventSource = new EventSource("/sse/chatList"); // controller 경로

eventSource.addEventListener("countUp", (event) => {
    upUserCount(event.data);
});

eventSource.addEventListener("countDown", (event) => {
    downUserCount(event.data);
});

eventSource.addEventListener("roomEnd", (event) => {
    deleteRoom(event.data);
});

eventSource.addEventListener("roomCreation", (event) => {
    const chatRoom = JSON.parse(event.data); // JSON으로 파싱
    createRoom(chatRoom);
});

function upUserCount(roomId) {
    $(".roomUserCount-" + roomId).text(
        Number($(".roomUserCount-" + roomId).text()) + 1
    );
}

function downUserCount(roomId) {
    $(".roomUserCount-" + roomId).text(
        Number($(".roomUserCount-" + roomId).text()) - 1
    );
}

function deleteRoom(roomId) {
    const chatRoomListBox = document.getElementById("chatRoomListBox");
    var target = chatRoomListBox.querySelector(".room-" + roomId);
    target.remove();
}

// 마지막 row 확인해서 
function createRoom(chatRoom) {
    const chatRoomListBox = document.getElementById("chatRoomListBox");

    // 일단 마지막 row에 넣는거
    const cardRows = chatRoomListBox.querySelectorAll(".row");
    let lastRow;
    if (cardRows.length > 0) {
        lastRow = cardRows[cardRows.length - 1];
    } else {
        // row하나도 없는거?
    }

    let roomComponent = document.createElement('div');
    roomComponent.className = "col-3";
    roomComponent.innerHTML = `           
        <div class="card mb-2" >
            <div class="row">
                <div class="col-4">
                    <img alt="IMG">
                </div>
                <div class="col-8">
                    <p>${chatRoom.mentor}</p>
                </div>
            </div>
            <h4>${chatRoom.roomName}</h4>
            <div>${chatRoom.createdAt}</div>
            <div class="d-flex justify-content-between align-items-center">
                <span>현재인원/${chatRoom.userLimit} </span>
                <form action="/api/chatting/participant/${chatRoom.id}/${loginId}" 
                    method="post">
                    <button type="submit"> 단톡 참석 </button>
                </form>
            </div>
        </div>
    `;

    lastRow.appendChild(roomComponent);
}

$(document).ready(function() {

    // 로그인한 사용자id
    loginId = $(".userId").val();  

    $(".loginBtn").on("click", function() {
        var userId = $(this).data("user-id");
    
        $.ajax({
            type:"get",
            url:"/api/chatting/login/" + userId,
            success:function(response) {
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
            url:"/api/chatting/room/" + mId,
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
