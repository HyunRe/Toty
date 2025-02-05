
async function fetchRoomList() {
    try {
        const loginId = $(".userId").val();
        const response = await fetch(`/api/chatting/rooms`);
        if (response.ok) {
            const roomList = await response.json();
            // updateRoomListTb(chatterList, loginId); 
            updateRoomListCd(roomList, loginId);
        }
    } catch (error) {
        console.error('Failed to fetch roomList:', error);
    }
}

function updateRoomListCd(roomList, loginId) {
    var roomCount = roomList.length;
    // 한줄에 몇개 넣을건지에 따라 결정되는 row수
    var rowCount = Math.ceil(roomCount/4); // 수 올림

    const cardBox = document.querySelector('#chatRoomListBox');
    cardBox.innerHTML = ''; // 기존 내용을 초기화

    // 일단 하나만 해보는거
    let cardRow = document.createElement('div');
    cardRow.className = "row";

    let partList = roomList.slice(0,4);

    partList.forEach(room => {
        let roomComponent = document.createElement('div');
        roomComponent.className = "col-3";
        roomComponent.innerHTML = `           
            <div class="card mb-2" >
                <div class="row">
                    <div class="col-4">
                        <img alt="IMG">
                    </div>
                    <div class="col-8">
                        <p>${room.mentor.userName}</p>
                    </div>
                </div>
                <h4>${room.roomName}</h4>
                <div>${room.createdAt}</div>
                <div class="d-flex justify-content-between align-items-center">
                    <span>현재인원/${room.userLimit} </span>
                    <form action="/api/chatting/participant/${room.id}/${loginId}" 
                        method="post">
                        <button type="submit"> 단톡 참석 </button>
                    </form>
                </div>
            </div>
        `;
        cardRow.appendChild(roomComponent);
    });

    cardBox.appendChild(cardRow);


    for (let i = 0; i < rowCount; i++) { 
        // 여기서 slice로 잘라서 부분배열들마다 row아래에 col-3으로 넣어주는 로직해야함
       

        //cardBox.appendChild(cardRow);
    }
    

}


function updateRoomListTb(roomList, loginId) {

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
                <form action="/api/chatting/participant/${room.id}/${loginId}" 
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
            url:"/api/chatting/login/" + userId,
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
