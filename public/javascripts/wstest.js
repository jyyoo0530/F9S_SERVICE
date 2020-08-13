var webSocketClient = new WebSocket("ws://localhost:8080/ws");

function sendText() {
    var msg = {
        type: "message",
        text: document.getElementById("text").value,
        id: clientID,
        date: Date.now()
    };
    webSocketClient.onopen = function (event) {
        webSocketClient.send(JSON.stringify(msg));
    };

    document.getElementById("wstest1").vlaue = "";
}

sendText()