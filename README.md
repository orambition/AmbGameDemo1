# AmbGameDemo1
based on lwjgl3 game demo
教程地址：
https://github.com/lwjglgamedev/lwjglbook

修复md5模型阴影不现实bug
修改视野旋转矩阵的生成方法，使其支持z轴旋转
增加平行光角度2维旋转

    2018-03-23

增加阴影支持

    2018-03-21

已完成md5模型加载，可以加载模型和动画文件，可以播放动画。
    -修改了Mesh类，使其可以将动画文件传入vbo
    -修改了场景着色器，使其可以根据骨骼位置实时计算顶点位置和其法线
    -修改了shaderProgram使其可以传入着色器所需数据
    
page251 - over
cp19-2 - over

    2018-03-20

