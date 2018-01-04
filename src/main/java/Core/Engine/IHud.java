package Core.Engine;

public interface IHud {
    GameItem[] getGameItems();

    default void cleanUp() {
        GameItem[] gameItems = getGameItems();
        for (GameItem gameItem : gameItems) {
            gameItem.getMesh().cleanUp();
        }

    }

}