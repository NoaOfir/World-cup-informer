# pragma once
#include <mutex>
#include"ConnectionHandler.h"
#include "StompProtocol.h"


class thread_InputManager{
    private:
    ConnectionHandler& handler;
    StompProtocol& protocol;


    public:
    thread_InputManager(ConnectionHandler& handler, StompProtocol& protocol);
    void run();

};