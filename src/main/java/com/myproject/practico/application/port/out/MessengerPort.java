package com.myproject.practico.application.port.out;

public interface MessengerPort {
    void sendMessage(String userId, String text);
}