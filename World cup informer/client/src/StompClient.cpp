#include <ConnectionHandler.h>
#include <thread_InputManager.h>
#include <thread>
bool isLogout=false;//use in main thread, false as defuelt
 bool askLogIn=false;
extern bool shouldTerminate;//do shutdown


int main(int argc, char *argv[]) {
	

	ConnectionHandler *handler=new ConnectionHandler();

	User *user=new User();

	StompProtocol protocol(*handler,*user);
	thread_InputManager inputManager(*handler,protocol);
	std::thread thread1(&thread_InputManager::run,&inputManager);
	while(true){
		if(!isLogout){
			string ans;
			if(askLogIn&&!handler->getFrame(ans)){
				 std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
			}
			if(ans!=""){//if there is something to process
			protocol.stompProcessRecieveMessage(ans);
			}


		}


	
	}
	thread1.join();
	 delete(handler);
	delete(user);
}