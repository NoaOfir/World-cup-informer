#pragma once

#include "../include/ConnectionHandler.h"
#include "../include/User.h"
#include <string>
#include <mutex>
using namespace std;

// TODO: implement the STOMP protocol
class StompProtocol
{
private:
int receiptIdSupply;
int subIdSupply;
ConnectionHandler& handler;
User& user;
bool registerd;
atomic<bool> isConnected;
string userName;
mutex myMutex;
void login(vector<string>splitInput);
void join(vector<string>splitInput);
void exit(vector<string>splitInput);
void logout(vector<string>splitInput);
void report(vector<string>splitInput);
void connected();
void receipt(vector<string>splitOutput);
void error(vector<string>splitOutput);
void message (vector<string>splitOutput);
void summary(vector<string>splitinput);
void needToLogout();
public: StompProtocol(ConnectionHandler &handler, User &user);
void stompProcessMessage (string &input);
void stompProcessRecieveMessage(string &output);
};
