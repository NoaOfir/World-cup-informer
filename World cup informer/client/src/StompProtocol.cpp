#include "StompProtocol.h"
#include "ConnectionHandler.h"
#include "User.h"
#include <vector>
#include "Event.h"
#include<mutex>

#include <sstream>
#include <iostream> 
#include <fstream>


using namespace std;

class Event;



extern bool isLogout;//use in main thread, false as defuelt
extern bool askLogIn;
extern bool shouldTerminate;//do shutdown


StompProtocol::StompProtocol(ConnectionHandler& handler,User& user):receiptIdSupply(1),subIdSupply(0),handler(handler),user(user),registerd(false) ,isConnected(false),userName(),myMutex(){}

void StompProtocol::stompProcessMessage(string &input){
    std::vector<string> splitInput;
	//unsigned int index=0;
	std:: string part;
	std::stringstream in(input);

	while (std::getline(in, part, ' '))
		splitInput.push_back(part);

	std:: string structure=splitInput[0];
	//go to the relevant command	
    if(structure=="login"){
        login(splitInput);
		
		return;
    }
	if(structure=="join"){
		
        join(splitInput);
		return;
    }
	if(structure=="exit"){
        exit(splitInput);
		return;
	}
	if(structure=="logout"){
        logout(splitInput);
		return;
	}
	if(structure=="report"){
        report(splitInput);
		return;
	}
	if(structure=="summary"){
        summary(splitInput);
		return;
	}
}
void StompProtocol:: login(vector<string>splitInput){
		
    	if(isConnected){
				std::cerr<<"The client is already logged in, log out before trying again"<< std::endl;
				needToLogout();
				return;

			}
			int index=splitInput[1].find(":");//host+port
			std::string host=splitInput[1].substr(0,index);
            handler.setHost(host);
			std::string port_str=splitInput[1].substr(index+1);
			short port=std::stoul(port_str,nullptr,0);
            handler.setPort(port);
			if(!registerd&&!handler.connect()){
					std::cerr<<"could not connect to server" << std::endl;
			}
			else{
				if(!handler.getSocket().is_open()){//handle socket error
				std::cerr<<"could not connect to server" << std::endl;
				return;
				}
				askLogIn=true;
				registerd=true;
				isLogout=false;
				userName=splitInput[2];

                //create the frame
				stringstream tempFrame;
				tempFrame <<"CONNECT\n"<<
				"accept-version:1.2\n"<<
				"host:stomp.cs.ac.il\n"<<
				"login:"+userName+"\n"<<
				"passcode:"+splitInput[3]+"\n\n";
				string frame=tempFrame.str();
				handler.sendFrame(frame);

			}				

			}
    void StompProtocol:: join(vector<string>splitInput){
		
		string destination=splitInput[1];
		int subChannelId=user.subChannel(destination);//return -1 if we already subscribe to this channel
		int receiptId = -1;
		if(subChannelId!=-1){//new subscribe
		 receiptId = user.addReceiptId("join "+destination);//help us later to know what to do with a recieved receipt
		}
		string receiptIdStr=to_string(receiptId);
		 if (receiptId != -1){
			//create the frame
		        stringstream tempFrame;
				tempFrame <<"SUBSCRIBE\n"<<
				"destination:"+destination+"\n"<<
				"id:"+to_string(subChannelId)+"\n"<<
				"receipt:"+receiptIdStr+"\n\n";
				
				string frame=tempFrame.str();
				handler.sendFrame(frame);
		 }

	}
	void StompProtocol:: exit(vector<string>splitInput){
	
	string destination=splitInput[1];
	int subChannelId=user.unsubChannel(destination);//erase the channel from my channels and return the subid of this channel
	int receiptId = -1;
	if(subChannelId!=-1){
		receiptId=user.addReceiptId("exit "+destination);//help us later to know what to do with a recieved receipt
	}
	string receiptIdStr=to_string(receiptId);
		
     stringstream tempFrame;
	 //create the frame
				tempFrame <<"UNSUBSCRIBE\n"<<
				"id:"+to_string(subChannelId)+"\n"<<
					"receipt:"+receiptIdStr+"\n\n";				
				string frame=tempFrame.str();
				handler.sendFrame(frame);
			
	}
	void StompProtocol:: logout(vector<string>splitInput){

		int receiptId= user.addReceiptId("logout");//help us later to know what to do with a recieved receipt
		
		stringstream tempFrame;
		//create the frame
				tempFrame <<"DISCONNECT\n"<<
				"receipt:"+to_string(receiptId)+"\n\n";				
				string frame=tempFrame.str();
				handler.sendFrame(frame);
	

	}
void StompProtocol:: report(vector<string>splitInput){

string file=splitInput[1];
names_and_events toParse=parseEventsFile(file);
string team_a=toParse.team_a_name;
string team_b=toParse.team_b_name;
string gameName=team_a+"_"+team_b;
vector<Event>allEvents=toParse.events;
for(Event& event:allEvents){
	map<std::string, std::string> gameUpdates=event.get_game_updates();
    std::map<std::string, std::string>teamAUpdates=event.get_team_a_updates();
    std::map<std::string, std::string>teamBUpdates= event.get_team_b_updates() ;

	string gameUpdatesStr;
	for(const auto u:gameUpdates){
		gameUpdatesStr=gameUpdatesStr+"\t"+ u.first+": " +u.second+ "\n";
	}
	string teamAUpdatesStr;
	for(const auto u:teamAUpdates){
		teamAUpdatesStr=teamAUpdatesStr+"\t"+ u.first+": " +u.second+ "\n";
	}
	string teamBUpdatesStr;
	for(const auto u:teamBUpdates){
		teamBUpdatesStr=teamBUpdatesStr+"\t"+ u.first+": " +u.second+ "\n";
	}
stringstream tempFrame;
				tempFrame <<"SEND\n"<<
				"destination:"+gameName+"\n\n"<<
				"user: "+userName+"\n"<<
				"team a: "+team_a+"\n"<<
				"team b: "+team_b+"\n"<<
				"event name: "+event.get_name()+"\n"<<
				"time: "+to_string(event.get_time())+"\n"<<
				"general game updates:\n"<<
				gameUpdatesStr<<
				"team a updates:\n"<<
				teamAUpdatesStr<<
				"team b updates:\n"<<
				teamBUpdatesStr<<
				"description:\n"<<
				event.get_discription()+"\n";
				string frame=tempFrame.str();
				handler.sendFrame(frame);	

}
}
///////recieve message

void StompProtocol::stompProcessRecieveMessage(string &output){
	std::vector<string> splitOutput;
	
	std:: string part;
	
	std::stringstream in(output);

	while (std::getline(in, part, '\n'))
		splitOutput.push_back(part);
	
	std:: string type=splitOutput[0];
	
	//go to the relevant command	

	if(type=="CONNECTED"){
		connected();
		return ;
	}
	if(type=="RECEIPT"){
		receipt(splitOutput);
		return;
	}
	if(type=="ERROR"){
		error(splitOutput);
		return;
	}
	if(type=="MESSAGE"){
		message(splitOutput);
		return;
	}


}
void StompProtocol::connected(){
	user.setName(userName);
	isConnected=true;
	std::cout<<"Login successful\n";
}
void StompProtocol::receipt(vector<string>splitOutput){
	int index=splitOutput[1].find(":");
	string tempReceiptId=splitOutput[1].substr(index+1);
	int receiptId=stoi(tempReceiptId);
	string receiptType=user.returnedReceiptId(receiptId);
	//handle with a receipt type(that we saved before) 
	if(receiptType.find("join")!=string::npos)
	{
		int indChannel=receiptType.find(" ");
		string channel=receiptType.substr(indChannel+1);
		std::cout<<"Join channel "+channel+"\n";
		return;
	}
	if(receiptType.find("exit")!=string::npos){
		int indChannel=receiptType.find(" ");
		string channel=receiptType.substr(indChannel+1);
		std::cout<<"Exited channel "+channel+"\n";
		return;

	}
	if(receiptType=="logout"){
		needToLogout();
		
		return;
	 }
	}

	void StompProtocol::error(vector<string>splitOutput){
		
		if(splitOutput[2].find("password")!=std::string::npos){
			cout<<"Wrong password\n";
			needToLogout();
			return;
		}
		if(splitOutput[2].find("User already logged in")!=std::string::npos){
			cout<<"User already logged in\n";
			needToLogout();
			return;
		}
		if(splitOutput[2].find("malformed")!=std::string::npos){
			cout<<splitOutput[splitOutput.size()-2];
			needToLogout();
			return;
		}
		if(splitOutput[2].find("cannot unsubscribe")!=std::string::npos){//"User have to subscribe before unsubscribe"
			cout<<"User have to subscribe before unsubscribe\n";
			needToLogout();
			return;
		}
		if(splitOutput[2].find("Didn't logged in")!=std::string::npos){//"user have to log in befor unsubscribe"
			cout<<splitOutput[splitOutput.size()-2];
			needToLogout();
			return;
		}
		if(splitOutput[2].find("The client isn't subscribe to this channel")!=std::string::npos){//"The clinet have to subscribes to the channel before sending message"
			cout<<"The clinet have to subscribes to the channel before sending message\n";
			needToLogout();
			return;
		}

	}
	void StompProtocol::message (vector<string>splitOutput){
		//parse the message 

		string team_a="";
		string team_b="";
		string reporter="";
		string gameName="";
		string general_stats="";
       string team_a_stats="";
        string team_b_stats="";
		string description="";
		string eventName="";
		string time="";
		string gameEvent="";
		bool found=false;

	for(unsigned int i=8;i<splitOutput.size();i++){
		string s=splitOutput[i];
		if(s.find("team a:")!=std::string::npos){
			int index=s.find(":");
			team_a=s.substr(index+2);
		}

		if(s.find("team b:")!=std::string::npos){
			int index=s.find(":");
			team_b=s.substr(index+2);
		}
			gameName=team_a+"_"+team_b;

			if(s.find("user:")!=std::string::npos){
			int index=s.find(":");
			reporter=s.substr(index+2);
		}
			//check if we already have a game object with the same gamename and reporter
			for(Game* g:this->user.getVector()){
				if(g->getGameName()==gameName&&g->getUser()==reporter)
				found=true;
			}
			
			
		if(s.find("general game updates:")!=std::string::npos){
			int j=i;
			while(splitOutput[j+1].find("team a updates:")==string::npos){
			general_stats=general_stats+"\n"+splitOutput[j+1].substr(1);
			j++;
			
			}
	}

	if(s.find("team a updates:")!=std::string::npos){
			int j=i;
			while(splitOutput[j+1].find("team b updates:")==string::npos){
			team_a_stats=team_a_stats+"\n"+splitOutput[j+1].substr(1);
			j++;
			}
	}

	if(s.find("team b updates:")!=std::string::npos){
		int j=i;
		while(splitOutput[j+1].find("description:")==string::npos){
			
		team_b_stats=team_b_stats+"\n"+splitOutput[j+1].substr(1);
		j++;
		}
	}


	if(s.find("description:")!=std::string::npos){
		description=splitOutput[i+1];
	}
	
	if(s.find("event name:")!=std::string::npos){
		int index=s.find(":");
			eventName=s.substr(index+2);
	}

	if(s.find("time:")!=std::string::npos){
		int index=s.find(":");
		char temp=s[index+2];
		if (isdigit(temp)){
			time=s.substr(index+2);
		}
	}
	}
	gameEvent=time+" - "+eventName+":\n\n"+
	description;
	
	if(!found){
		//create the game object
				Game* newGame=new Game(team_a, team_b,reporter,gameName,general_stats,team_a_stats,team_b_stats,gameEvent);
				this->user.addToVector(newGame);
			}
	else{//update the data
		for(Game* g:this->user.getVector()){
				if(g->getGameName()==gameName&&g->getUser()==reporter){
					g->editStats(general_stats,team_a_stats,team_b_stats);
					g->editGameEvents(gameEvent);
				}


	}	
	}	

	}
	void StompProtocol::summary(vector<string>splitInput){

		string gameName=splitInput[1];
		string reporter=splitInput[2];
		string filePath=splitInput[3];
		if(user.getchannelBySubId().find(gameName)==user.getchannelBySubId().end()){
			std::cerr<<"The user isn't subscribe to this channel"<< std::endl;
			return;
		}
		Game *g=nullptr;
		for(Game *f:user.getVector()){
			
			if(f->getGameName()==gameName&&f->getUser()==reporter){			
				g=f;
			}
		}
		if(g!=nullptr){	
		string output=g->getTeam_a()+" vs "+g->getTeam_b()+"\n"+
		"Game stats:\n"+
		"General stats:\n"+
		g->getGeneral_stats()+"\n\n"+
		g->getTeam_a()+" stats:\n"+
		g->getTeam_a_stats()+"\n\n"+
		g->getTeam_b()+" stats:\n"+
		g->getTeam_b_stats()+"\n\n"+
		"Game event reports:\n"+
		g->getGameEvents();


		std::ofstream outputFile(filePath, std::ofstream::trunc);
		outputFile << output<<std::endl;
		outputFile.close();
		}
		else
			std::cerr<<"The user doesn't have reports from this reporter"<< std::endl;


	}
	

void StompProtocol::needToLogout(){
	std::cerr<<"The user logs out"<< std::endl;
	isConnected=false;
	registerd=false;
	isLogout=true;
	user.resetUser();
	handler.getSocket().close();
}





