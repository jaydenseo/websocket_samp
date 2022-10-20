var stompClient = null;
var data = {};//전송 데이터(JSON)

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);

    if (connected) {
    }
    else {
        // $("#talk").html("");
    }

}

function connect() {
    var sock = new SockJS('/websocket');
    stompClient = Stomp.over(sock);
    stompClient.connect({userId: $('#mid').val(), connectTime: new Date().toLocaleString(), requestUrl: window.location.href, hostIp: window.location.host}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        // 채팅
        stompClient.subscribe('/topic/chat', function (chat) {
            showChat(JSON.parse(chat.body));
        });
        // 공지
        stompClient.subscribe('/topic/notice', function (chat) {
            showNotice(JSON.parse(chat.body));
        });
        // 입장/퇴장 (시스템)
        stompClient.subscribe('/topic/system', function (chat) {
            showSystem(JSON.parse(chat.body));
        });
    });

    sock.onclose = function() {
        setConnected(false);
        console.log("Disconnected");
        setTimeout(function() {
            connect();
        }, 3000);
    };
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

// [채팅] 발신
function sendChat() {

    if($("#msg").val().trim() != ''){
        data.type = "CHAT"
		data.name = $("#mid").val();
		data.message = $("#msg").val();
		data.date = new Date().toLocaleString();
		var temp = JSON.stringify(data);
        stompClient.send("/app/chat/message", {}, temp);
	}
    $("#msg").val("");
}

// [공지] 발신
function sendNotice() {

    if($("#msg").val().trim() != ''){
        data.type = "NOTICE"
		data.name = $("#mid").val();
		data.message = $("#msg").val();
		data.date = new Date().toLocaleString();
		var temp = JSON.stringify(data);
        stompClient.send("/app/chat/notice", {}, temp);
	}
    $("#msg").val("");
}

// [채팅] 수신
function showChat(chat) {
    var css;
    if(chat.name == $("#mid").val()){
        css = "class=me";
    }else{
        css = "class=other";
    }
    
    var item = `<div ${css} id="messageDiv">
                    <span><b>${chat.name}</b></span> [ ${chat.date} ]<br/>
                    <span>${chat.message}</span>
                </div>`;
    $("#talk").append(item);
    $("#talk").scrollTop=$("#talk").scrollHeight;//스크롤바 하단으로 이동
}

// [공지] 수신
function showNotice(chat) {
    var css = "class=notice";
    
    var item = `<div ${css} id="noticeDiv">
                    <span><b>공지</b></span> [ ${chat.date} ]<br/>
                    <span>${chat.message}</span>
                </div>`;
    $("#talk").append(item);
    $("#talk").scrollTop=$("#talk").scrollHeight;//스크롤바 하단으로 이동
}

// [시스템메시지] 수신
function showSystem(chat) {

    console.log("system:" + chat);
    var css = "class=system";
    var item = `<div ${css} id="noticeDiv">
                    <span>${chat.message}</span>
                </div>`;
    $("#talk").append(item);
    $("#talk").scrollTop=$("#talk").scrollHeight;

}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#sendChat" ).click(function(){ sendChat(); });
    $( "#sendNotice" ).click(function(){ sendNotice(); });
    $("#msg").keyup(function(ev){
        if(ev.keyCode == 13){
            sendChat();
        }
    });
});


