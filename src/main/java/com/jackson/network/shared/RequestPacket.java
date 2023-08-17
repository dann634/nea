package com.jackson.network.shared;

public class RequestPacket extends Packet {
    private String request;
    private static final long serialVersionUID  = 1L;


    public RequestPacket(String request) {
        this.request = request;
    }

    public String getRequest() {
        return this.request;
    }
}
