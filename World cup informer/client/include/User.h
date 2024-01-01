   // #ifndef BOOST_ECHO_CLIENT_USER_H
 //#define BOOST_ECHO_CLIENT_USER_H
	#pragma once
	
	#include <vector>
	
	#include <unordered_map>
	#include "Game.h"
	#include<map>
	class Game;
	
	using std::vector;
	using std::unordered_map;
	

    using namespace::std;
  
	
	class User {
	private:
	   
	    std::string name;
	   
	    unordered_map <string, int> channelBySubId;
	    unordered_map <int, string> returnReceipt;
	    
		 int subIdSupplier;
	    int receiptIdSupplier;
	    vector<Game*> updatesFromUserToChannel;//vector of all updates for specific game and reporter
	  
	
	public:
	   
	    User();
	    
	    ~User();
	
	    void resetUser();
	
	    unordered_map<string,int> &getchannelBySubId ();

	    unordered_map<int,string> &getreturnReceipt();

	   vector<Game*>  getupdatesFromUserToChannel();
		
	    std::string getName();

	    void setName(std::string name);
	
	    int subChannel(string &channel); 

	    int unsubChannel(string channel);
	
	    int addReceiptId(string details);

	    string returnedReceiptId(int receiptId);

		vector<Game*>getVector();
		
		void addToVector(Game* g);
	  
	};

