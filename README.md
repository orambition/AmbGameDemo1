# AmbGameDemo1
based on lwjgl3 game demo
教程地址：
https://github.com/lwjglgamedev/lwjglbook

完善阴影绘制，采用分层阴影绘制CMS
    -将阴影绘制过程从Render类中提出，建立新的阴影渲染类、阴影缓存（纹理）类、阴影分层类
    -删除shadowMap类
    -添加ArrTexture数组纹理，以支持分层阴影
    -重构Render类中的render方法
    -重构InstancedMesh类的render方法，将模型*视野和模型*光视野矩阵删除，只保留模型矩阵
    -修改Scene和深度图着色器，取消模型*视野矩阵,由模型和视野矩阵替代
    -完善ShaderProgram类，以支持数组的Uniform定义和赋值
    -修改粒子着色器和深度图着色器
    -修改的InstancedMesh类的render方法，将是否被选中，与粒子缩放共用
    -修改平行光类，删除正交矩阵和距离，该计算工作有shadow类动态完成
    page315 - over
    cp26 - over 
    
    2018-04-02

添加视野锥裁减优化类
    -通过视野锥来判断哪些对象被看到，对看不见的对象进行裁减，减少Renderer中的绘制列表
    -修改window类已适应优化
    -为网格类增加包围球半径
    -为GameItem类增加是否裁减属性
    page315 - over
    cp25 - over
    
    2018-03-29

使用NanoVG完善HUD
    -在window类启动模板测试，Renderer类clear做适应性修改
    -建立新的HUD类，其初始化和渲染在Logic主循环中完成,HUD绘制独立与场景
    -修改Renderer类的setup和render方法剔除HUD渲染
    -window类添加状态属性恢复方法
    -修改物体选择检测类
    page308 - over
    cp24 - over
    
    2018-03-28

添加物体选择功能
    -增加摄像机选择检测和鼠标选择检测类，并在主循环中调用，用于检测选择物体
    -为GameItem类增加被选中属性
    -修改场景的顶点着色器和片段着色器，以支持选择高亮
    -修改renderer类以适应着色器的改变
    -修改InstancedMesh类中的renderListInstanced方法，以支持着色器的改变
    -重构摄像机矩阵，将摄像机视野矩阵重构到了Camera类中
    -重构透视矩阵，将透视矩阵转移只Window类中
    -renderer类中添加准星绘制
    -修改Window类的init方法，已支持兼容模式，用于绘制准星
    page302 - over
    cp23 - over
    
    2018-03-28

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

