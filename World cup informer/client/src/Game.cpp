#include "../include/Game.h"
#include "User.h"
Game::Game(string teamA, string teamB,string user,string gameName,string general_stats, string team_a_stats,string team_b_stats, string gameEvents): teamA(teamA),teamB(teamB),user(user),gameName(gameName),general_stats(general_stats),team_a_stats(team_a_stats),team_b_stats(team_b_stats), gameEvents(gameEvents){}


Game::~Game(){
    this->teamA.clear();
    this->teamB.clear();
    this->user.clear();
};
string Game:: getGameName(){
    return gameName;
}
//save all the stats from the reports
void Game::editStats(string& generalUpdates, string &team_aUpdates, string &team_bUpdates){
 if(generalUpdates!=""){ 
    if(general_stats!="")  {
general_stats=general_stats+"\n"+generalUpdates;
    }
    else
    general_stats=generalUpdates;
 }
 if(team_aUpdates!=""){
    if(team_a_stats!=""){
team_a_stats=team_a_stats+"\n"+team_aUpdates;
    }
    else
    team_a_stats=team_aUpdates;
 }
 if(team_bUpdates!=""){
    if(team_b_stats!=""){
team_b_stats=team_b_stats+"\n"+team_bUpdates;
    }
    else
    team_b_stats=team_bUpdates;
 }
}
//save all the game events in right structure
void Game::editGameEvents(string &gameEvent){
    gameEvents=gameEvents+"\n\n\n"+gameEvent;
}

string Game::getTeam_a(){
    return teamA;
}
string Game::getTeam_b(){
    return teamB;
}
string Game::getUser(){
    return user;
}
string Game::getGeneral_stats(){
    return general_stats;
}

string Game::getTeam_a_stats(){
    return team_a_stats;
}

string Game::getTeam_b_stats(){
    return team_b_stats;
}

string Game::getGameEvents(){
    return gameEvents;
}