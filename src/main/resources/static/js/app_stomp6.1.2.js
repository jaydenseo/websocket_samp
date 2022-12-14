var stompClient = null;
var data = {};//전송 데이터(JSON)
var sock = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);

    if (connected) {
    }
    else {
        // $("#talk").html("");
    }

}

// stomp 5버전 이상
function connect() {

    // Create an instance
    stompClient = new StompJs.Client({
        // The URL for the STOMP broker to connect to.
        // brokerURL: 'ws://localhost:9901/websocket',  //webSocketFactory로 대체

        // Typical usage with SockJS
        webSocketFactory: function () {
            return new SockJS("/websocket");
        },
        // Connection headers, important keys - `login`, `passcode`, `host`.
        connectHeaders: {
            userId: $('#mid').val(), 
            hostIp: window.location.host
        },
        // incoming and outgoing frames are logged
        debug: function (str) {
            console.log(str);
        },
        reconnectDelay: 5000,       // automatically reconnect with delay in milliseconds, set to 0 to disable. (default: 5,000ms)
        heartbeatIncoming: 10000,    // Incoming heartbeat interval in milliseconds. Set to 0 to disable. client send heartbeats (default: 10,000ms)
        heartbeatOutgoing: 10000,    // Outgoing heartbeat interval in milliseconds. Set to 0 to disable. client receive heartbeats (default: 10,000ms)
        connectionTimeout: 5000      // Will retry if Stomp connection is not established in specified milliseconds. Default 0, which implies wait for ever.
    });

    // Attemp to connect
    stompClient.activate();

    stompClient.onConnect = function (frame) {
        setConnected(true);
        console.log('onConnect: ' + frame);
        stompClient.subscribe('/topic/chat', function (message) {
            showChat(JSON.parse(message.body));
        });
        stompClient.subscribe('/topic/notice', function (message) {
            showNotice(JSON.parse(message.body));
        });
        subscription = stompClient.subscribe('/topic/system', function (message) {
            showSystem(JSON.parse(message.body));
        });
    };

    stompClient.onStompError = function (frame) {
        // Will be invoked in case of error encountered at Broker
        // Bad login/passcode typically will cause an error
        // Complaint brokers will set `message` header with a brief message. Body may contain details.
        // Compliant brokers will terminate the connection after any error
        console.log('Broker reported error: ' + frame.headers['message']);
        console.log('Additional details: ' + frame.body);
    };
    
}

function disconnect() {
    if (stompClient !== null) {
        // stomp 5버전 이상
        stompClient.deactivate();
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

        // stomp 5버전 이상
        stompClient.publish({destination:"/app/chat/message", body: temp});
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

        // stomp 5버전 이상
        stompClient.publish({destination:"/app/chat/notice", body: temp});
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
    $("#talk").scrollTop($("#talk")[0].scrollHeight);//스크롤바 하단으로 이동
}

// [공지] 수신
function showNotice(chat) {
    var css = "class=notice";
    
    var item = `<div ${css} id="noticeDiv">
                    <span><b>공지</b></span> [ ${chat.date} ]<br/>
                    <span>${chat.message}</span>
                </div>`;
    $("#talk").append(item);
    $("#talk").scrollTop($("#talk")[0].scrollHeight);//스크롤바 하단으로 이동
}

// [시스템메시지] 수신
function showSystem(chat) {

    console.log("system:" + chat);
    var css = "class=system";
    var item = `<div ${css} id="noticeDiv">
                    <span>${chat.message}</span>
                </div>`;
    $("#talk").append(item);
    $("#talk").scrollTop($("#talk")[0].scrollHeight);//스크롤바 하단으로 이동

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


