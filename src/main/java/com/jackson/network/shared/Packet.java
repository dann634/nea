package com.jackson.network.shared;

import java.io.Serial;
import java.io.Serializable;

public class Packet implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final Object object; //Main Data
    private final String msg; //Request / Response Message
    private String ext; //Any Extra Details needed

    public Packet(String msg, Object object) {
        this.object = object;
        this.msg = msg;
    }

    public Packet(String msg, Object object, String ext) {
        this.object = object;
        this.msg = msg;
        this.ext = ext;
    }

    //Getters
    public String getExt() {
        return ext;
    }

    public Object getObject() {
        return object;
    }

    public String getMsg() {
        return msg;
    }
}
