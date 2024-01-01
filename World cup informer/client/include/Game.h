
    #pragma once
	
#include <string>
#include <map>
using namespace std;
class Game
{
private:
    string teamA;
    string teamB;
    string user;
    string gameName;
    string general_stats;
    string team_a_stats;
    string team_b_stats;
    string gameEvents;
public:
    Game(string teamA, string teamB,string user,  string gameName, string general_stats, string team_a_stats,  string team_b_stats, string gameEvents);
    ~Game();
    string getGameName();
    void editStats(string& generalUpdates, string &team_aUpdates, string &team_bUpdates);
    void editGameEvents(string &gameEvent);
string getTeam_a();
   
string getTeam_b();
   
string getUser();
   
string getGeneral_stats();


string getTeam_a_stats();
   

string getTeam_b_stats();
   
string getGameEvents();

};

  

