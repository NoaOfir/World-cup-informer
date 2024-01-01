#include "../include/User.h"
	#include <unordered_map>
	#include <vector>
	#include <string>
	#include "Game.h"

	 using std::vector;

    using namespace std;
   
	

	
	User::User(): name(), channelBySubId(),returnReceipt(),subIdSupplier(1), receiptIdSupplier(1), updatesFromUserToChannel(vector<Game*>{}){
		
	};
	User::~User(){
		for(Game *g:updatesFromUserToChannel){
			delete g;

		}
	}
	
	unordered_map<string, int> &User::getchannelBySubId() { return channelBySubId;}
	
	unordered_map<int, string> &User::getreturnReceipt() { return returnReceipt;}
	
	vector<Game*> User::getupdatesFromUserToChannel() { return updatesFromUserToChannel;}
		
	std::string User::getName() { return name;}
	
	void User::setName(std::string name) { this->name=name;}

	
	
	
	
	// returning the new topic_id, or the old one.
	int User::subChannel(string &channel) {
	    std::pair<string,int> keyValue (channel,subIdSupplier);
	    if(channelBySubId.find(channel)==channelBySubId.end()) {        //checks if the topic already exists
	        channelBySubId.insert(keyValue);
	        subIdSupplier++;
			return subIdSupplier-1;
	    }
		return -1;	        
	}
	
	
	// returns the subChannelId or -1 if not exists
	int User::unsubChannel(string channel) {
	   
	    if(channelBySubId.find(channel)!=channelBySubId.end()){
	        int subId = channelBySubId.find(channel)->second;
	        channelBySubId.erase(channel);                                 //remove this channel from the channel i subscribe to
	        return subId;                                                  
	    }
	                           
	  return -1;                                                
	    
	}
	
	
	int User::addReceiptId(string details)  {
	    std::pair<int,string> tmp_pair (receiptIdSupplier, details);     
	    returnReceipt.insert(tmp_pair);
	    receiptIdSupplier++;
	    return receiptIdSupplier-1;
	
	}
	
	// removing the receiptId from the map if it has been returned
	string User::returnedReceiptId(int receiptId) {
		string type=returnReceipt.find(receiptId)->second;
	    returnReceipt.erase(returnReceipt.find(receiptId)); 
		 return type;    
	   
	}


	void User::resetUser() {

	    channelBySubId.clear();
	    
	}
	vector<Game*> User:: getVector(){
		return updatesFromUserToChannel;
	}

	void User:: addToVector(Game* g){
		updatesFromUserToChannel.push_back(g);
	}
	
