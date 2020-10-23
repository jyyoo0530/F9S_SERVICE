var url = "ws://localhost:8080/websocket/websocket"
let webSocketClient;
webSocketClient = new WebSocket(url);
var wsStatus = document.getElementById("wsStatus");
wsStatus.innerHTML = "Websocket Connected";
var msgSent = document.getElementById("msgSent");
var sendBtn = document.getElementById("sendText");
var closeBtn = document.getElementById("closeConn");
var connectionChecker = 1;                                             // 1: connected, 0: closed

webSocketClient.onopen = function (e) {
    wsStatus.innerHTML = "Websocket Connected";
    connectionChecker = 1;
};

function sendText(msgTxt) {
    if (connectionChecker === 1) {
        alert("[onOpen] Sending message to the Server");
        webSocketClient.send(msgTxt);
        msgSent.innerHTML = msgTxt;
    } else {
        alert("Websocket is not connected")
    }
}

function closeConn() {
    webSocketClient.close();
    // wsStatus.innerHTML("Websocket Close")
    webSocketClient.onclose = function (e) {
        alert("!!!!!!!!!CONNECTION CLOSED!!!!!!!!!!!!!!")
        msgSent.innerHTML = "";
        wsStatus.innerHTML = "Websocket Disconnected";
        connectionChecker = 0;
    };
}

function getRandomInt(min, max) {
    min = Math.ceil(min);
    max = Math.floor(max);
    return Math.floor(Math.random() * (max - min)) + min; //최댓값은 제외, 최솟값은 포함
}

var msgList = ["This is Test", "How are you", "Apple", "Banana", "WaterMelon"];
let msgTxt;

sendBtn.addEventListener('click', () => {
    msgTxt = msgList[getRandomInt(0, 5)]
    sendText(msgTxt)
});
closeBtn.addEventListener('click', () => {
    closeConn()
});

webSocketClient.onclose = function (e) {
    alert("!!!!!!!!!CONNECTION CLOSED!!!!!!!!!!!!!!");
    document.getElementById("wsStatus").innerHTML = "";
    document.getElementById("msgSent").innerHTML = "";
    wsStatus.innerHTML = "Websocket Disconnected";
    connectionChecker = 0;
};
webSocketClient.onmessage = function (e) {
    alert(" MESSAGE INCOMING ")
    document.getElementById("msgSent").innerHTML = msgTxt;
}