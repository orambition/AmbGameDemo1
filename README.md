# AmbGameDemo1
based on lwjgl3 game demo
教程地址：
https://github.com/lwjglgamedev/lwjglbook

添加声音支持
    -增加SoundBuffer用于加载声音
    -完善Utils类，增加读取ogg压缩声音文件的方法
    -增加音源类
    -增加听众类
    -增加声音管理类
    page294 - over
    cp22 - over
    
    2018-03-27

完善实例化（分组）渲染方法
    -支持粒子的分组渲染，将纹理块的选择添加为分组渲染的属性
    -修改InstancedMesh类，将模型*视野矩阵和模型*光视野矩阵、纹理坐标整合为一个VBO
    -修改Renderer类以支持分组渲染
    -修改场景着色器和粒子着色器，以支持分组的纹理坐标
增加FPS显示
    -修改窗口、引擎、Main函数以支持FPS显示
    -增加天空盒纯色和环境光属性
    -修改了GameDemo1Logic，以高度图生成了组对象
    page283 - over
    cp21-p2 - over
    
    2018-03-26

增加实例化（分组）渲染方法
    -增加InstancedMesh类，继承自Mesh，将共用一个Mesh的对象分为一组，进行分组绘制，优化性能
    -修改Renderer类中的场景、深度图的渲染方法，以适应分组绘制
    -修改Renderer类中的天空盒绘制方法，使其支持环境光，着色器一同修改
    -修复了粒子渲染的Bug
    -修改了Scene类，增加实例化（分组）Mesh、阴影绘制等属性，修复Bug
    -修改了场景的点着色器和片段着色器，使其支持实例化渲染（深度图着色器一同修改了）
    -修改了OBJLoader类使其可以创建实例化（分组）对象
    page280 - over
    cp21-p1 - over
    
    2018-03-25

更新joml库为最新版本
    -修改GameItem类中旋转属性为四元数
    -优化Transformation类，使用新的库函数
    -修改render类中天空盒渲染方法，以适应新的库函数
    
    2018-03-24

增加粒子支持
    -增加粒子类、粒子发生器类
    -增加粒子着色器，修改相应的render方法
    -修改纹理，使其支持分块获取
    -粒子朝向存在bug，Renderer类中renderParticles（）方法存在问题
    page273 - over
    cp20 - over
   
    2018-03-23

修复md5模型阴影不现实bug
    修改视野旋转矩阵的生成方法，使其支持z轴旋转
    增加平行光角度2维旋转

    2018-03-22

增加阴影支持

    2018-03-21

已完成md5模型加载，可以加载模型和动画文件，可以播放动画。
    -修改了Mesh类，使其可以将动画文件传入vbo
    -修改了场景着色器，使其可以根据骨骼位置实时计算顶点位置和其法线
    -修改了shaderProgram使其可以传入着色器所需数据
    
page251 - over
cp19-2 - over

    2018-03-20

