var webSocketClient = new WebSocket("ws://localhost:8080/ws");
var wsStatus = document.getElementById("wsStatus");
wsStatus.innerHTML = "Websocket Connected";
var msgSent = document.getElementById("msgSent");
var openBtn = document.getElementById("openConn");
var sendBtn = document.getElementById("sendText");
var closeBtn = document.getElementById("closeConn");
var connectionChecker = 1;                           // 1: connected, 0: closed

function openConnection() {
    webSocketClient = new WebSocket("ws://localhost:8080/ws");
    webSocketClient.onopen = function (e) {
        alert("[open] Connection Established");
        wsStatus.innerHTML = "Websocket Connected";
        connectionChecker = 1;
    };
}

function sendText() {
    if (connectionChecker === 1) {
        alert("[onOpen] Sending message to the Server");
        webSocketClient.send("MESSSSSSAAAAAAAAGGGGGGEEEEEE");
        msgSent.innerHTML = "MESSSSSSAAAAAAAAGGGGGGEEEEEE";
    } else {
        alert("Websocket is not connected")
    }
}

function closeConn() {
    webSocketClient.close()
    // wsStatus.innerHTML("Websocket Close")
    webSocketClient.onclose = function (e) {
        alert("!!!!!!!!!CONNECTION CLOSED!!!!!!!!!!!!!!")
        msgSent.innerHTML = "";
        wsStatus.innerHTML = "Websocket Disconnected";
        connectionChecker = 0;
    };
}

openBtn.addEventListener('click', () => {
    openConnection()
});
sendBtn.addEventListener('click', () => {
    sendText()
});
closeBtn.addEventListener('click', () => {
    closeConn()
});

webSocketClient.onclose = function (e) {
    alert("!!!!!!!!!CONNECTION CLOSED!!!!!!!!!!!!!!");
    document.getElementById("wsStatus").innerHTML = "";
    wsStatus.innerHTML = "Websocket Disconnected";
    connectionChecker = 0;
};
webSocketClient.onmessage = function (e) {
    alert("@@@MESSAGE INCOMING@@@");
    document.getElementById("msgSent").innerHTML = e.data;
}