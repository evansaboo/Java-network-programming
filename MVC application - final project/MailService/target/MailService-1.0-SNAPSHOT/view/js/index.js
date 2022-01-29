
socket.onopen = function () {
    userInit();
};

socket.onmessage = onMessage;
$(document).ready(function () {
    $("#loginForm").submit(function (e) {
        e.preventDefault();
        login();
    });

});

function login() {
    var username = $("#username").val();
    var password = $("#password").val();
    if (!username || !password)
        return;
    var loginAction = {
        action: "login",
        username: username,
        password: password
    };
    sendToServer(loginAction);

}


function onMessage(event) {
    var response = JSON.parse(event.data);
    switch (response.action) {
        case "login":
            if (response.success) {
                localStorage.setItem("userId", response.userId);
                window.location.replace("inbox.html");
                return;
            }
            notify(response.reason, 'danger');
            break;
        case "checkIfLoggedIn":
            if (response.isLoggedIn) {
                window.location.replace("inbox.html");
            }
            break;
        case "loginError":
            notify(response.reason, 'danger');
            break;
    }
}

