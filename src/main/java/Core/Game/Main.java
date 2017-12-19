package Core.Game;

import Core.Engine.GameEngine;
import Core.Engine.IGameLogic;

public class Main {
    public static void main(String[] args){
        try{
            boolean vSync = true;
            IGameLogic gameLogic = new GameDemo1Logic();
            GameEngine gameEngine = new GameEngine("Amb",1024,768,vSync,gameLogic);
            gameEngine.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
