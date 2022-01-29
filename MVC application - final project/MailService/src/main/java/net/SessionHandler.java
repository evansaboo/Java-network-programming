/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net;

import controller.Controller;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.websocket.Session;
import model.Account;
import model.Mail;

@ApplicationScoped
public class SessionHandler {

    @Inject
    private Controller contr;

    private final HashMap<Long, Account> users = new HashMap<>();
    private final HashMap<String, HashSet<Session>> userSession = new HashMap<>();

    public Account getUser(long userId) {
        synchronized (users) {
            return users.get(userId);
        }
    }

    public void setUserAsLoggedIn(long userId, Account acc) {
        synchronized (users) {
            users.put(userId, acc);
        }
    }

    public void sendToSession(Session session, JsonObject message) {
        try {
            session.getBasicRemote().sendText(message.toString());
        } catch (IOException ex) {
            Logger.getLogger(SessionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void handleLogin(Session session, JsonObject message) {
        String username;
        String password;
        try {
            username = message.getString("username");
            password = message.getString("password");
        } catch (NullPointerException | ClassCastException e) {
            handleError(session, "loginError", "Internal Server Error. Couldn't get Username or Password.");
            return;
        }

        JsonObjectBuilder addMessage = jsonBuilder("login");
        long userId;
        if ((userId = contr.checkAccCredential(username, password)) != -1) {
            setUserAsLoggedIn(userId, contr.getAccount(username));
            addMessage.add("success", true)
                    .add("userId", Long.toString(userId));
        } else {
            addMessage.add("success", false);
            addMessage.add("reason", "Login Failed. Wrong username or password");
        }
        sendToSession(session, addMessage.build());

    }

    public void register(Session session, JsonObject message) {
        String username;
        String password;
        try {
            username = message.getString("username");
            password = message.getString("password");
        } catch (NullPointerException | ClassCastException e) {
            handleError(session, "registerError", "Internal Server Error. Couldn't get Username or Password.");
            return;
        }

        JsonObjectBuilder addMessage = jsonBuilder("register");

        long userId;
        if ((userId = contr.addNewAccount(username, password)) != -1) {
            setUserAsLoggedIn(userId, contr.getAccount(username));

            addMessage.add("success", true)
                    .add("userId", Long.toString(userId));
        } else {
            addMessage.add("success", false);
            addMessage.add("reason", "Registration Failed, username already exists");
        }
        sendToSession(session, addMessage.build());
    }

    public JsonArray listToJsonArray(List<Mail> mail) {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        for (Mail m : mail) {
            builder.add(m.toJson(true).build());
        }
        return builder.build();
    }

    public void checkIfLoggedIn(Session session, JsonObject jsonMessage, long userId) {
        JsonObjectBuilder addMessage = jsonBuilder("checkIfLoggedIn");
        addMessage.add("isLoggedIn", true);
        sendToSession(session, addMessage.build());
    }

    public JsonObjectBuilder jsonBuilder(String action) {
        JsonObjectBuilder addMessage = Json.createObjectBuilder();
        addMessage.add("action", action);
        return addMessage;
    }

    public void insertNewMsg(Session session, JsonObject jsonMessage, long userId) {
        String toUser;
        String subject;
        String msgContent;
        try {
            toUser = jsonMessage.getString("msgTo");
            subject = jsonMessage.getString("subject");
            msgContent = jsonMessage.getString("msgBody");
        } catch (NullPointerException | ClassCastException e) {
            handleError(session, "inboxError", "Internal Server Error. Couldn't get message information");
            logout(session, userId);
            return;
        }

        Account acc = getUser(userId);
        boolean response = contr.insertNewMsg(acc, toUser, subject, msgContent);
        JsonObjectBuilder addMessage = jsonBuilder("sendNewMsg");

        addMessage.add("success", response);
        if (response) {
            addMessage.add("reason", "The message was successfully sent");
            notifyUser(toUser, acc);
        } else {
            addMessage.add("reason", "Unable to send the message. Receiver doesn'y exist");
        }

        sendToSession(session, addMessage.build());
    }

    private void notifyUser(String toUser, Account acc) {
        synchronized (userSession) {
            HashSet<Session> sessions = userSession.get(toUser);
            if (sessions != null) {
                for (Iterator<Session> i = sessions.iterator(); i.hasNext();) {
                    Session s = i.next();

                    if (s.isOpen()) {
                        JsonObjectBuilder msg = jsonBuilder("newMsgAlert");
                        msg.add("label", "New message from <b>" + acc.getUsername() + "</b>");
                        sendToSession(s, msg.build());
                    } else {
                        i.remove();
                    }
                }
            }
            userSession.put(toUser, sessions);
        }
    }

    public void deleteMsg(Session session, JsonObject jsonMessage, long userId) {
        long mailId = 0;
        try {
            mailId = Long.parseLong(jsonMessage.getString("mailId"));
            String username = getUser(userId).getUsername();
            Mail mail = contr.getMail(mailId);
            String mailFrom = mail.getMailFrom();
            String mailTo = mail.getMailTo();
            if (!username.equals(mailFrom) || !username.equals(mailTo)) {
                handleError(session, "inboxError", "Permission to delete the message was denied.");
                logout(session, userId);
                return;
            }

        } catch (NumberFormatException | NullPointerException | ClassCastException e) {
            handleError(session, "inboxError", "Internal Server Error. Failed to parse requested message to delete.");
            logout(session, userId);
            return;
        }
        contr.deleteMsg(userId, mailId, jsonMessage.getBoolean("isReceiver"));
    }

    public void logout(Session session, long userId) {
        Account acc = getUser(userId);
        String username = acc.getUsername();
        remSessionFromUserSessions(username, session);
        removeUserFromList(userId);
    }

    private void remSessionFromUserSessions(String username, Session session) {
        synchronized (userSession) {
            HashSet<Session> sessions = userSession.get(username);
            sessions.remove(session);
            if (sessions.isEmpty()) {
                userSession.remove(username);
            } else {
                userSession.put(username, sessions);
            }
        }
    }

    public void removeUserFromList(long userId) {
        synchronized (users) {
            users.remove(userId);
        }
    }

    public void getMsg(Session session, JsonObject jsonMessage, long userId) {
        long mailId = Long.parseLong(jsonMessage.getString("mailId"));

        Mail mail = contr.getMail(mailId);
        JsonObjectBuilder builder = mail.toJson(false);
        builder.add("action", "mail");
        sendToSession(session, builder.build());
    }

    public void getUsername(Session session, long userId) {
        JsonObjectBuilder builder = jsonBuilder("getUsername");
        String username = getUser(userId).getUsername();
        builder.add("username", username);
        addSessionToUserSessions(username, session);
        sendToSession(session, builder.build());
    }

    private void addSessionToUserSessions(String username, Session session) {
        synchronized (userSession) {
            HashSet<Session> sessions = userSession.get(username);
            if (sessions != null) {
                sessions.add(session);
            } else {
                sessions = new HashSet<>();
                sessions.add(session);
                userSession.put(username, sessions);
            }
        }
    }

    public void sendRecvdMsgsToUser(Session session, long userId) {
        String username = getUser(userId).getUsername();
        List<Mail> recvdMsgs = contr.getAllMailByTo(username);
        JsonArray jsonArray = listToJsonArray(recvdMsgs);
        JsonObjectBuilder addMessage = jsonBuilder("getRecvdMsgs");
        addMessage.add("content", jsonArray);
        sendToSession(session, addMessage.build());
    }

    public void sendSentMsgsToUser(Session session, long userId) {
        String username = getUser(userId).getUsername();
        List<Mail> sentMsgs = contr.getAllMailByFrom(username);
        JsonArray jsonArray = listToJsonArray(sentMsgs);
        JsonObjectBuilder addMessage = jsonBuilder("getSentMsgs");
        addMessage.add("content", jsonArray);
        sendToSession(session, addMessage.build());
    }

    public void handleError(Session session, String action, String msg) {
        JsonObjectBuilder builder = jsonBuilder(action);
        builder.add("reason", msg);
        sendToSession(session, builder.build());
    }

}
