package Core.Engine;

import Core.Engine.items.GameItem;

public interface IHud {
    GameItem[] getGameItems();

    default void cleanUp() {
        GameItem[] gameItems = getGameItems();
        for (GameItem gameItem : gameItems) {
            gameItem.getMesh().cleanUp();
        }

    }

}