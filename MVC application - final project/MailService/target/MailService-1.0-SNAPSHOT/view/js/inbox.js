
socket.onmessage = onMessage;

if (userId === null) {
  goToErrorPage();
}
socket.onopen =  function (){
    getFromServer("getUsername");
};

socket.onerror =  function (){
    sessionStorage.setItem("error", "Something went wrong while communicating with the server.");
    goToErrorPage();
};


$(document).ready(function () {
    $("#msgForm").submit(function (e) {
        e.preventDefault();
        sendMsg();
    });

    $("#refreshBtn").click(function () {
        getFromServer("getRecvdMsgs");
        getFromServer("getSentMsgs");
    });

    $("#logout").click(function () {
        logout();
    });

    $(document).on('click', 'a[id^="recv_"], a[id^="send_"]', function (e) {
        var pid = $(this).attr("id").substring(5);
        if ($(e.target).is("#delete")) {
            deleteMail(this);
            return;
        }
        if ($(e.target).is("#reply")) {
            var from = $('span[id^="from_' + pid + '"]').text();
            var subject = $('span[id^="title_' + pid + '"]').text();
            $('#msgModal #inputUsername').val(from);
            $('#msgModal #inputSubject').val("RE: " + subject);
            $('#msgModal').modal('show');
            return;
        }
        getMessage(this);

    });

    $("#msgModal").on('hidden.bs.modal', function () {
        clearModal();
    });

});

function checkIfInboxIsEmpty(el) {
    if (!$.trim($(el).html())) {
        $(el).append('<div class="list-group-item">'
                + '<span class="text-center">This tab is empty.</span>'
                + '</div>');
    }

}

function listAllRecvdMails(list, inboxId) {
    $(inboxId).empty();
    for (var i = 0; i < list.length; i++) {
        var obj = list[i];
        $(inboxId).append(inboxMsgTemplate(obj.mailId, obj.mailFrom, obj.title, obj.body, obj.mailCreated));
    }
    checkIfInboxIsEmpty(inboxId);
}

function listAllSentMails(list, inboxId) {
    $(inboxId).empty();
    for (var i = 0; i < list.length; i++) {
        var obj = list[i];
        $(inboxId).append(sentInboxTemplate(obj.mailId, obj.mailTo, obj.title, obj.body, obj.mailCreated));
    }
    checkIfInboxIsEmpty(inboxId);
}

function inboxMsgTemplate(mailId, mailFrom, mailTitle, mailBody, mailCreated) {
    return '<a id="recv_' + mailId + '" href="#" class="list-group-item" onclick="return false;">'
            + '<span id="reply" class="fa fa-reply reply" ></span>'
            + '<span id="delete" class="fa fa-remove delete" ></span>'
            + '<span id="from_' + mailId + '" class="name" >' + mailFrom + '</span> '
            + '<span id="title_' + mailId + '">' + mailTitle + '</span>'
            + '<span class="text-muted truncate mailBody">- ' + mailBody + '</span> '
            + '<span class="badge">' + mailCreated + '</span>'
            + '</a>';
}
function sentInboxTemplate(mailId, mailTo, mailTitle, mailBody, mailCreated) {
    return '<a id="send_' + mailId + '" href="#" class="list-group-item" onclick="return false;">'
            + '<span id="delete" class="fa fa-remove delete" ></span>'
            + '<span>To: </span>'
            + '<span id="to_' + mailId + '" class="name">' + mailTo + '</span> '
            + '<span id="title_' + mailId + '">' + mailTitle + '</span>'
            + '<span class="text-muted truncate mailBody" >- ' + mailBody + '</span> '
            + '<span class="badge">' + mailCreated + '</span>'
            + '</a>';
}
function onMessage(event) {
    var response = JSON.parse(event.data);
    switch (response.action) {
        case "mail_list":
            listAllRecvdMails(response.received_mails, "#recvd_mail");
            listAllSentMails(response.sent_mails, "#sent_mail");
            break;
        case "getUsername":
            $(document).ready(function () {
                $('span#username').text(response.username);
            });
            getFromServer("getRecvdMsgs");
            getFromServer("getSentMsgs");

            break;
        case "getRecvdMsgs":
            listAllRecvdMails(response.content, "#recvd_mail");
            break;
        case "getSentMsgs":
            listAllSentMails(response.content, "#sent_mail");
            break;
        case "sendNewMsg":
            if (response.success) {
                notify(response.reason, "success");
                getFromServer("getSentMsgs");
            } else
                notify(response.reason, "danger");
            break;
        case "mail":
            parseMail(response);
            break;
        case "newMsgAlert":
            notify(response.label, "info");
            getFromServer("getRecvdMsgs");
            break;
        case "inboxError":
            sessionStorage.setItem("error", response.reason);
            goToErrorPage();
            break;
        default:
            console.log(response.action);
            break;
    }
}

function sendMsg() {
    var receiver = $("#inputUsername");
    var subject = $("#inputSubject");
    var inputMsgBody = $("#inputMsgBody");

    var jsonMsg = {
        action: "sendNewMsg",
        userId: userId,
        msgTo: receiver.val(),
        subject: subject.val(),
        msgBody: inputMsgBody.val()
    };
    sendToServer(jsonMsg);

    $('#msgModal').modal('hide');
}
function clearModal() {
    var receiver = $("#inputUsername");
    var subject = $("#inputSubject");
    var inputMsgBody = $("#inputMsgBody");
    receiver.val("");
    subject.val("");
    inputMsgBody.val("");
}

function deleteMail(elem) {
    var pid = $(elem).attr("id");
    var isReceiver = pid.indexOf('recv') > -1;
    var jsonMsg = {
        action: "deleteMsg",
        userId: userId,
        mailId: pid.substring(5),
        isReceiver: isReceiver
    };

    sendToServer(jsonMsg);
    $(elem).fadeOut(100, function () {
        var elemId = "#" + $(elem).parent().attr('id');
        $(this).remove();
        checkIfInboxIsEmpty(elemId);
    });
}

function getMessage(elem) {
    var id = $(elem).attr("id");
    var pid = id.substring(5);
    var jsonMsg = {
        action: "getMsg",
        userId: userId,
        mailId: pid,
    };

    sendToServer(jsonMsg);
}

function logout() {
    var jsonMsg = {
        action: "logout",
        userId: userId
    };
    sendToServer(jsonMsg);

    localStorage.removeItem("userId");
}

function parseMail(response) {
    $('#msgTitle').text(response.title);
    $('#msgFrom').text(response.mailFrom);
    $('#msgTo').text(response.mailTo);
    $('#msgDate').text(response.mailCreated);
    $('#msgBody').text(response.body);
    $('#showMsgModal').modal('show');
}
