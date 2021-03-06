package Core.Game;

import Core.Engine.GameEngine;
import Core.Engine.IGameLogic;
import Core.Engine.Window;

public class Main {
    public static void main(String[] args){
        try{
            boolean vSync = true;
            IGameLogic gameLogic = new GameDemo1Logic();
            Window.WindowOptions opts = new Window.WindowOptions();
            opts.cullFace =true;
            opts.showFps = true;
            opts.compatibleProfile = true;
            opts.antialiasing = true;
            opts.frustumCulling = false;
            GameEngine gameEngine = new GameEngine("Amb",vSync,opts,gameLogic);
            gameEngine.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
