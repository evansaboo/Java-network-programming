/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net;

/**
 *
 * @author Evan
 */
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@ApplicationScoped
@ServerEndpoint("/Mail_Service")
public class WebSocketServer {

    @Inject
    private SessionHandler sessionHandler;

    @OnOpen
    public void open(Session session) {
    }

    @OnClose
    public void close(Session session) {
        
    }

    @OnError
    public void onError(Throwable error) {
        Logger.getLogger(WebSocketServer.class.getName()).log(Level.SEVERE, null, error);
    }

    @OnMessage
    public void handleMessage(String message, Session session) {
        try (JsonReader reader = Json.createReader(new StringReader(message))) {
            JsonObject jsonMessage = reader.readObject();
            long userId;

            try {
                userId = Long.parseLong(jsonMessage.getString("userId"));
            } catch (ClassCastException | NullPointerException e) {
                userId = -2;
            }
            if (userId == -2) {
                switch (jsonMessage.getString("action")) {
                    case "login":
                        sessionHandler.handleLogin(session, jsonMessage);
                        break;
                    case "register":
                        sessionHandler.register(session, jsonMessage);
                        break;
                }
                return;
            } else if (sessionHandler.getUser(userId) != null) {
                switch (jsonMessage.getString("action")) {
                    case "getUsername":
                        sessionHandler.getUsername(session, userId);
                        break;
                    case "getRecvdMsgs":
                        sessionHandler.sendRecvdMsgsToUser(session, userId);
                        break;
                    case "getSentMsgs":
                        sessionHandler.sendSentMsgsToUser(session, userId);
                        break;
                    case "getMsg":
                        sessionHandler.getMsg(session, jsonMessage, userId);
                        break;
                    case "sendNewMsg":
                        sessionHandler.insertNewMsg(session, jsonMessage, userId);
                        break;
                    case "deleteMsg":
                        sessionHandler.deleteMsg(session, jsonMessage, userId);
                        break;
                    case "logout":
                        sessionHandler.logout(session, userId);
                        break;
                    default:
                        sessionHandler.checkIfLoggedIn(session, jsonMessage, userId);
                        break;
                }
                return;

            }
            sessionHandler.handleError(session,"inboxError" , "Your session has expired, please login again.");
            
        }
    }
}
