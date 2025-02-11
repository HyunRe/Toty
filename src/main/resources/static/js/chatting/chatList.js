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
    const cardRows = chatRoomListBox.querySelectorAll(".rowDiv");
    let lastRow;
    if (cardRows.length > 0) {
        lastRow = cardRows[cardRows.length - 1];
        let roomComponents = lastRow.querySelectorAll(".roomUI");
        if (roomComponents.length === 4) { // 마지막row 가 다 차있는경우 
            withRow(chatRoom, chatRoomListBox); // row생성+ 안에 row넣음
        } else {
            inRow(chatRoom, lastRow); // 있는row안에 room넣음
        }
    } else { // row하나도 없는거
        withRow(chatRoom, chatRoomListBox);
    }
}

function withRow(chatRoom, chatRoomListBox) {
    let rowComponent = document.createElement('div');
    rowComponent.className = "row rowDiv";
    inRow(chatRoom, rowComponent);

    chatRoomListBox.appendChild(rowComponent);
}


function inRow(chatRoom, lastRow) {
    let roomComponent = document.createElement('div');
    roomComponent.className = `col-3 roomUI room-${chatRoom.id}`;
    roomComponent.innerHTML = `           
        <div class="card mb-2" >
            <div class="row">
                <div class="col-4">
                    <img src="/img/chatting/undraw_profile.svg" style="width: 50px; height: 50px;">
                </div>
                <div class="col-8">
                    <p>${chatRoom.mentor}</p>
                </div>
            </div>
            <h4>${chatRoom.roomName}</h4>
            <div>생성된 시간 : ${chatRoom.createdAt}</div>
            <div class="d-flex justify-content-between align-items-center">
                <div>인원 : 
                    <span class="roomUserCount-${chatRoom.id}"
                        >0</span>/<span>${chatRoom.userLimit}</span>  
                </div>
                
                <form action="/api/chatting/participant/${chatRoom.id}" 
                    method="post">
                    <button type="submit"> 단톡 참석 </button>
                </form>
            </div>
        </div>
    `; // 버튼 없애기위해 2개로 나눌수도
    lastRow.appendChild(roomComponent);
}

$(document).ready(function() {

    // 로그인한 사용자id
    loginId = $(".userId").val();  
    
    $(".createRoom").on("click", function() {
       
        var rr = $(".roomName").val();
        var ul = $(".userLimit").val();
        var params = {
                        "roomName": rr, "userLimit": ul
                      }
        $.ajax({
            type:"post",
            url:"/api/chatting/room",
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
