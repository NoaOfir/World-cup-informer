#include "thread_InputManager.h"
#include "ConnectionHandler.h"
#include "StompProtocol.h"

using namespace std;

extern bool isLogout;//use in main thread, false as defuelt
extern bool askLogIn;
extern bool shouldTerminate;//do shutdown


thread_InputManager::thread_InputManager(ConnectionHandler& handler, StompProtocol& protocol):handler(handler), protocol(protocol){}


void thread_InputManager::run(){
    while(1){
        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
		std::string line(buf);
        if((line!="") & !isLogout){
            protocol.stompProcessMessage(line);
        }
        else if ((isLogout)&(line.find("login")!=string::npos))
        {
            isLogout=false;
            askLogIn=false;
             protocol.stompProcessMessage(line);
        }

    }
}