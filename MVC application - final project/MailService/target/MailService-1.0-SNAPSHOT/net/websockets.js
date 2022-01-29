/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


var socket = new WebSocket("ws://localhost:8080/MailService/Mail_Service");
socket.onerror = function () {
    onError();
};
var userId = localStorage.getItem('userId');

function userInit() {
    if (userId === null) {
        return;
    }
    var initAction = {
        action: "checkIfLoggedIn",
        userId: userId
    };
    socket.send(JSON.stringify(initAction));
}

function notify(msg, alertType) {

    $.notify({
        message: msg

    }, {
        element: 'body',
        position: null,
        type: alertType,
        allow_dismiss: true,
        newest_on_top: true,
        placement: {
            from: "top",
            align: "right"
        },
        offset: 20,
        spacing: 10,
        z_index: 1031,
        delay: 5000,
        timer: 1000,
        animate: {
            enter: 'animated fadeInDown',
            exit: 'animated fadeOutUp'
        }
    });

}

function goToErrorPage() {
    window.location.replace("error.html");
}

function sendToServer(msg) {
    if (socket.readyState !== WebSocket.OPEN) {
        onError();
        return;
    }
    socket.send(JSON.stringify(msg));
}

function getFromServer(action) {
    var jsonMsg = {
        action: action,
        userId: userId
    };
    sendToServer(jsonMsg);
}
function onError(){
        notify('Failed to connect to Server, please refresh the page and try again.', 'danger');

}