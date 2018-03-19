#version 330
//深度图片段着色器只关注深度，也就是z值
void main(){
    gl_FragDepth = gl_FragCoord.z;
}