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
    showRoom(chatRoom);
});

// 멘토권한 사용자가 단톡방 생성 
function createRoom() {

    Swal.fire({
        title: "채팅방 생성",
        html: `
            <input class="roomName" placeholder="단톡방 주제">
            <select class="userLimit">
                <option value="5">제한인원</option>
                <option value="5">5</option>
                <option value="10">10</option>
                <option value="15">15</option>
            </select>
        `,
        showCancelButton: true,
        cancelButtonText: "생성 취소",
        confirmButtonText: "채팅방 개설",
        confirmButtonColor: "#1f9bcf",
        preConfirm: () => {
            return {
                roomName: document.querySelector(".roomName").value,
                userLimit: document.querySelector(".userLimit").value
            };
        }
      }).then((result) => {
        /* 예, 아니오 선택에 따라 조건문 로직 실행 */
        if (result.isConfirmed) {
            
            var rr = result.value.roomName;
            var ul = result.value.userLimit;
            
            var params = {
                            "roomName": rr, "userLimit": ul
                        }
            $.ajax({
                type:"post",
                url:"/api/chatting/room",
                data: params,
                success:function(response) {
                    Swal.fire({
                        text: "새 채팅방이 개설되었습니다.",
                        icon: "success",
                        confirmButtonColor: "#1f9bcf",
                      });
                },
                error:function(xhr) {
                    let response = xhr.responseJSON;
                    console.log(response);
                    alert("단톡방 생성 실패 \n" + response.errorMessage);
                }
            });
        } else { // 그냥 닫힘
            }
      });

    
}

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

// 마지막 row 확인해서 
function showRoom(chatRoom) {
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

function deleteRoom(roomId) {
    const chatRoomListBox = document.getElementById("chatRoomListBox");
    var target = chatRoomListBox.querySelector(".room-" + roomId);
    target.remove();
}



$(document).ready(function() {

    // 로그인한 사용자id
    loginId = $(".userId").val();  

})
