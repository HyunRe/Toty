let stompClient;
let loginUserName, userId; // 로그인한 사용자 정보
let roomMentorId, roomId; // 단톡방 정보

function socketSetting() {
    stompClient = new StompJs.Client({ // 웹소켓 url
        brokerURL: 'ws://localhost:8070/websocket-chatRoom'
    });

    // 로그인한 사용자 정보 
    loginUserName = $("#userName").val();
    userId = $("#userId").val();

    // 단톡방 정보
    roomId = $("#roomId").val();
    roomMentorId = $("#roomMentorId").val();

    // WebSocket연결 성공 시 호출됨
    stompClient.onConnect = (frame) => {    
        setConnected(true);
        console.log('Connected: ' + frame);
    
        // 채팅 메시지 구독
        stompClient.subscribe('/chatRoom/' + roomId + '/message', (messageDTO) => {
            const message = JSON.parse(messageDTO.body);
            var isMine = 0;
            if (loginUserName === message.sender) {
                isMine = 1;
            }
            showMessage(message.sender, message.content, message.sendedAt, isMine);
        });
    
        // 채팅방 출입+종료 정보 구독
        stompClient.subscribe('/chatRoom/' + roomId + '/door', (message) => {
            if (message.body === "DISCONNECT") { // 멘토가 해당 채팅방 종료
                disconnect();
                alert("채팅방이 종료 되었습니다.");
            } else {
                const participant = JSON.parse(message.body);
                // 참석자 목록 업데이트
                updateParticipants(participant.chatterId, participant.chatterName, participant.access);
                // 채팅방에 출입 메시지
                showDoor(participant.chatterName, participant.access);
            }
        });
    };

    stompClient.onWebSocketError = (error) => {
        console.error('Error with websocket', error);
    };
    
    stompClient.onStompError = (frame) => {
        console.error('Broker reported error: ' + frame.headers['message']);
        console.error('Additional details: ' + frame.body);
    };

    // 연결
    stompClient.activate();
}


function sendMessage() {
    var params = {
        'sender': loginUserName
        , 'senderId': userId
        , 'message': $("#messageInput").val()  
    }

    stompClient.publish({
        destination: "/chatApp/" + roomId,
        body: JSON.stringify(params)
    });
}

function updateParticipants(chatterId, chatterName, access) {
    const chattersTbody = document.getElementById("chatters");
    if (access === 1) { // 입장
        const newChatter = document.createElement("tr");
        newChatter.className = "user-" + chatterId;
        newChatter.innerHTML =`
            <td style="text-align: center;"> 참석자 이미지 </td>
            <td>${chatterName}</td>
            <td style="text-align: right;">기타등등</td>
        `;
        chattersTbody.appendChild(newChatter);
    } else if (access === 0) { // 퇴장
        var exitChatter = chattersTbody.querySelector(".user-" + chatterId);
        exitChatter.remove();
    } else {
        alert("참석자 업데이트 실패");
    }
}

function showDoor(chatterName, access) {
    const doorDiv = document.createElement("div");
    doorDiv.className = "text-center mt-2 mb-3";
    doorDiv.style.fontSize = "0.7rem";
    doorDiv.style.backgroundColor = "lightgrey";

    var message;
    if (access === 1) { // 입장
        message = chatterName + "님이 입장했습니다."
    } else if (access === 0) { // 퇴장
        message = chatterName + "님이 퇴장했습니다."
    }
    doorDiv.textContent = message;
    chatContainer.appendChild(doorDiv);
}

function showMessage(sender, content, sendedAt, isMine) {
    const chatContainer = document.getElementById("chatContainer");

    const chatItemDiv = document.createElement("div");
    if (isMine === 0) {
        // 받은 메시지
        chatItemDiv.innerHTML = `
            <div>
                <img src=""  width="28" style="border-radius: 30%">
                <span style="font-size: 0.6rem;">${sender}</span>
            </div>
            <div class="message received">
                <p>${content}</p>
                <span style="font-size: 0.6rem;">${sendedAt}</span>
            </div>
        `;
    } else {
        // 보낸 메시지
        chatItemDiv.innerHTML = `
            <div class="message sent">
                <span style="font-size: 0.6rem; margin-right: 3px;">
                    ${sendedAt}
                </span>
                <p>${content}</p>
            </div>
        `;
    }
    chatContainer.appendChild(chatItemDiv);

    // 스크롤을 가장 아래로 내리기
    chatContainer.scrollTop = chatContainer.scrollHeight;
}


async function disconnect() {
    stompClient.deactivate();
    setConnected(false);
    console.log("Disconnected");
}

function exitRoom() {

    $.ajax({
        type:"PATCH",
        url:"/api/chatting/rooms/" + roomId + "/" + userId,
        success:async function(response) {
            await disconnect();
            location.href="/view/chatting/list";
        },
        error:function(xhr) {
            let response = xhr.responseJSON;
            console.log(response);
            alert("단톡방 나가기 실패 \n" + response.message);
        }
    });
}

function endRoom() {
    
    if (userId === roomMentorId) {
        $.ajax({
            type:"PATCH",
            url:"/api/chatting/rooms/" + roomId ,
            success:async function(response) { },
            error:function(xhr) {
                let response = xhr.responseJSON;
                console.log(response);
                alert("단톡방 종료 오류발생 \n" + response.message);
            }
        });
    }
}

function setConnected(connected) {
    // $("#connect").prop("disabled", connected);
    // $("#disconnect").prop("disabled", !connected);
    // if (connected) {
    //     $("#conversation").show();
    // }
    // else {
    //     $("#conversation").hide();
    // }
    // $("#greetings").html("");
}


function handleEnterKey(event) {
	if (event.key === 'Enter') {
		event.preventDefault();     // 줄바꿈 방지(기본 엔터 키 동작 방지)
		sendMessage();
	}
}



$(document).ready(function() {
    console.log("$doc ready func");

    socketSetting();
    

    console.log(" == end $doc ready func");

})
