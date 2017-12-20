package Core.Engine;

public class GameEngine implements Runnable {

    public static final int Target_FPS = 75;
    public static final int Target_UPS = 30;
    private final Window window;
    private final Thread gameLoopThread;
    private final Timer timer;
    private final IGameLogic gameLogic;


    public GameEngine(String windowTitle, int width, int height, boolean vsSync, IGameLogic gameLogic)throws Exception{
        gameLoopThread = new Thread(this,"GAME_LOOP_THREAD");
        window = new Window(windowTitle,width,height,vsSync);
        this.gameLogic = gameLogic;
        timer = new Timer();
    }

    public void start(){
        String osName = System.getProperty("os.name");
        if (osName.contains("Mac")){
            gameLoopThread.run();
        }else {
            gameLoopThread.start();
        }
    }

    @Override
    public void run() {
        try{
            init();
            gameLoop();
        }catch (Exception exp){
            exp.printStackTrace();
        }finally {
            cleanup();
        }
    }
    private void init() throws Exception {
        window.init();
        timer.init();
        gameLogic.init(window);
    }
    private void gameLoop(){
        float interval = 1.0f/Target_UPS;
        float elapsedTime;//过去的时间间隔
        float accumulator = 0;//累加

        boolean running = true;
        while (running && !window.windowShouldClose()){
            elapsedTime = timer.getElapsedTime();
            accumulator += elapsedTime;
            input();
            //使游戏状态的处理速度恒定，与运行设备无关
            while (accumulator >= interval){
                update(interval);
                accumulator -= interval;
            }
            //游戏状态和屏幕渲染是分开的，更新速度也不同
            render(accumulator/interval);
            //没开启垂直同步则使用自己实现的sync方法
            if (!window.isvSync()){
                sync();
            }
        }
    }

    protected void cleanup(){
        gameLogic.cleanup();
    }

    private void sync(){
        //控制循环间隔时间。速度和设备有关，运行越快渲染越快
        float loopSlot = 1f/Target_FPS;
        double endTime = timer.getLastLoopTime()+loopSlot;
        while (timer.getTime()<endTime){
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void input(){
        gameLogic.input(window);
    }
    protected void update(float interval){
        gameLogic.update(interval);
    }
    protected void render(float alpha){
        //alpha是当前渲染帧在游戏状态帧的比率（用于计算插值，需要预言函数）
        gameLogic.render(window);
        window.update();
    }
}
