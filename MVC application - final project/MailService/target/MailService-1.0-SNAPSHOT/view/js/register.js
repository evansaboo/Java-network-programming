
socket.onmessage = onMessage;

this.socket.onopen = function () {
    userInit();
};

$(document).ready(function () {
    $("#registerForm").submit(function (e) {
        e.preventDefault();
        register();
    });

});

function register() {

    var username = $("#username").val();
    var password = $("#password");
    var confirmPassword = $("#confirm_password");

    if (!username || !password) {
        notify("Username or password is empty.", 'danger');
        return;
    } else if (username.indexOf(' ') >= 0) {
        notify("Username can't contain spaces", 'danger');
        return;
    } else if (password.val() !== confirmPassword.val()) {
        notify("Passwords doesn't match", 'danger');
        return;
    }

    var loginAction = {
        action: "register",
        username: username,
        password: password.val()
    };
    sendToServer(loginAction);

}

function onMessage(event) {
    var response = JSON.parse(event.data);
    switch (response.action) {
        case "register":
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
        case "registerError":
            notifyUser(response.reason, 'danger');
            break;
    }
}
