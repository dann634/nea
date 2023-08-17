package com.jackson.network.packet;

public class RequestPacket {
    private String request;

    public RequestPacket(String request) {
        this.request = request;
    }

    public String getRequest() {
        return this.request;
    }
}
