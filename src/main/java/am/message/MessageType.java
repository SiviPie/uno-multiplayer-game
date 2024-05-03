package am.message;

/*

SERVER can:
- Send Player object to assign to client
- Send PlayerList object to clients to upda


CLIENT can:
- Send Username to help get a player assigned



TEXT:
---CASE 1: Client sends a text message
- Client: Sends text message for all the other clients
- Server: Receives the message and broadcasts it, except for the sender
---CASE2: Server sends a message

*/


public enum MessageType {
    TEXT,
    STATUS_UPDATE,
    PLAYER_LIST_INIT,
    PLAYER_LIST_ADD,
    PLAYER_LIST_REMOVE,
    PLAYER_CARDS_UPDATE,
    PLAYER_TURN,
    FIRST_CARD,
    CARD_ADD,
    GAME_CHOICE,
    PLAY_CARD,
    INIT,
    SERVER_SHUTDOWN,
    CLIENT_DISCONNECT
}
