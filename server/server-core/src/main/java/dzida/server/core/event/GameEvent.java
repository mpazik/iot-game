package dzida.server.core.event;

public interface GameEvent {
    int InstanceStarted = 0;
    int PlayerCreated = 1;
    int PlayerLogIn = 2;
    int PlayerLogOut = 3;
    int PlayerMoved = 4;
    int CharacterSpawned = 5;
    int CharacterDied = 6;
    int CharacterMoved = 7;
    int SkillUsed = 8;
    int CharacterGotDamage = 9;
    int Pong = 10;
    int InitialData = 11;
    int ServerMessage = 12;
    int Location = 13;
    int Player = 14;
    int PlayingPlayers = 15;
    int TimeSyncRes = 16;
    int InstanceCreated = 17;
    int PlayerWillRespawn = 18;

    int getId();
}