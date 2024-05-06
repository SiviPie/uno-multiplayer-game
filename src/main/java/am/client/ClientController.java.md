## Table of Contents

- [ClientController](#clientcontroller)
  - [Properties](#properties)
  - [Methods](#methods)
    - [addPlayerToList](#addPlayerToList)
    - [setPlayerCards](#setPlayerCards)
    - [setPlayerSkipTurns](#setPlayerSkipTurns)
    - [removePlayerFromList](#removePlayerFromList)
    - [setPlayerTurn](#setPlayerTurn)
    - [addTextMessageFromOthers](#addTextMessageFromOthers)
    - [addTextMessageFromSelf](#addTextMessageFromSelf)
    - [addCard](#addCard)
    - [setLastPlayedCard](#setLastPlayedCard)
    - [deselectCard](#deselectCard)
    - [addCardToStack](#addCardToStack)
    - [clearCardsStack](#clearCardsStack)
    - [setWildColor](#setWildColor)
    - [removeWildColor](#removeWildColor)
  - [Event Listeners](#event-listeners)
    - [connectButtonClick](#connectButtonClick)
    - [onWindowClose](#onWindowClose)
    - [disconnectButtonClick](#disconnectButtonClick)
  - [Example Usage](#example-usage)

### ClientController <a name="clientcontroller"></a>

This class is responsible for handling the client-side logic of the UNO game. It manages the connection to the server, receives and processes messages from the server, and updates the game's GUI accordingly.

**Properties**:

- `client`: The client object that manages the connection to the server.
- `gameController`: The controller for the second (game) window.

**Methods**:

- `addPlayerToList` <a name="addPlayerToList"></a>: Adds a player to the list of players in the game.
  - **Parameters**:
    - `player`: The player to add to the list.
  - **Example Usage**:
    ```java
    Player player = new Player("John", 10);
    ClientController.addPlayerToList(player);
    ```

- `setPlayerCards` <a name="setPlayerCards"></a>: Sets the number of cards a player holds.
  - **Parameters**:
    - `opponent`: The opponent whose number of cards is being set.
  - **Example Usage**:
    ```java
    Opponent opponent = new Opponent(10);
    ClientController.setPlayerCards(opponent);
    ```

- `setPlayerSkipTurns` <a name="setPlayerSkipTurns"></a>: Sets the number of turns a player has to skip.
  - **Parameters**:
    - `opponent`: The opponent whose number of skip turns is being set.
  - **Example Usage**:
    ```java
    Opponent opponent = new Opponent(10);
    ClientController.setPlayerSkipTurns(opponent);
    ```

- `removePlayerFromList` <a name="removePlayerFromList"></a>: Removes a player from the list of players in the game.
  - **Parameters**:
    - `id`: The id of the player to remove.
  - **Example Usage**:
    ```java
    ClientController.removePlayerFromList(10);
    ```

- `setPlayerTurn` <a name="setPlayerTurn"></a>: Sets the current player's turn.
  - **Parameters**:
    - `username`: The username of the player whose turn it is.
    - `isMyTurn`: A boolean indicating whether it is the local player's turn.
  - **Example Usage**:
    ```java
    ClientController.setPlayerTurn("John", true);
    ```

- `addTextMessageFromOthers` <a name="addTextMessageFromOthers"></a>: Adds a text message from another player to the chat.
  - **Parameters**:
    - `username`: The username of the player who sent the message.
    - `message`: The message text.
  - **Example Usage**:
    ```java
    ClientController.addTextMessageFromOthers("John", "Hello everyone!");
    ```

- `addTextMessageFromSelf` <a name="addTextMessageFromSelf"></a>: Adds a text message from the local player to the chat.
  - **Parameters**:
    - `message`: The message text.
    - `username`: The username of the local player.
  - **Example Usage**:
    ```java
    ClientController.addTextMessageFromSelf("Hello everyone!", "John");
    ```

- `addCard` <a name="addCard"></a>: Adds a card to the player's hand.
  - **Parameters**:
    - `card`: The card to add.
  - **Example Usage**:
    ```java
    Card card = new Card(Card.Color.RED, Card.Value.ONE);
    ClientController.addCard(card);
    ```

- `setLastPlayedCard` <a name="setLastPlayedCard"></a>: Sets the last played card.
  - **Parameters**:
    - `card`: The last played card.
  - **Example Usage**:
    ```java
    Card card = new Card(Card.Color.RED, Card.Value.ONE);
    ClientController.setLastPlayedCard(card);
    ```

- `deselectCard` <a name="deselectCard"></a>: Deselects the currently selected card.
- **Example Usage**:
  ```java
  ClientController.deselectCard();
  ```

- `addCardToStack` <a name="addCardToStack"></a>: Adds a card to the stack of played cards.
  - **Parameters**:
    - `card`: The card to add.
  - **Example Usage**:
    ```java
    Card card = new Card(Card.Color.RED, Card.Value.ONE);
    ClientController.addCardToStack(card);
    ```

- `clearCardsStack` <a name="clearCardsStack"></a>: Clears the stack of played cards.
  - **Example Usage**:
    ```java
    ClientController.clearCardsStack();
    ```

- `setWildColor` <a name="setWildColor"></a>: Sets the color of the wild card.
  - **Parameters**:
    - `color`: The color to set.
  - **Example Usage**:
    ```java
    ClientController.setWildColor(Card.Color.RED);
    ```

- `removeWildColor` <a name="removeWildColor"></a>: Removes the color of the wild card.
  - **Example Usage**:
    ```java
    ClientController.removeWildColor();
    ```

**Event Listeners**:

- `connectButtonClick` <a name="connectButtonClick"></a>: Handles the click event of the Connect button.
- `onWindowClose` <a name="onWindowClose"></a>: Handles the close event of the game window.
- `disconnectButtonClick` <a name="disconnectButtonClick"></a>: Handles the click event of the Disconnect button.

**Example Usage**:

```java
// ClientController clientController = new ClientController();
// ...
// clientController.addPlayerToList(player);
// clientController.setPlayerCards(opponent);
// clientController.setPlayerSkipTurns(opponent);
// clientController.removePlayerFromList(id);
// clientController.setPlayerTurn(username, isMyTurn);
// clientController.addTextMessageFromOthers(username, message);
// clientController.addTextMessageFromSelf(message, username);
// clientController.addCard(card);
// clientController.setLastPlayedCard(card);
// clientController.deselectCard();
// clientController.addCardToStack(card);
// clientController.clearCardsStack();
// clientController.setWildColor(color);
// clientController.removeWildColor();
// ...
```