package com.mayreh.toyka;

public class Main {
    public static void main(String[] args) {
        new NioSocketServer("127.0.0.1", 8888).start();
    }
}
