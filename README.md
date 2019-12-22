# SRTool

### 开发

应用名称：SR工具(SRTool)

支持平台：Android 4.1+

官网：https://diyyx.wang/SRTool

开发工具：Android Studio

发起时间：2019年4月21日

代码编写：雨夏
策划及调试：柳树怪

### 功能

#### 存档下载

* 载具下载
* 沙盒下载

#### 载具编辑

* 通过拖动组件更改组件原始坐标，原焊接点不变
* 调节组件旋转角度，360度无死角旋转
* 代码高亮查看

#### 星系编辑

* 界面实时响应代码编辑，加快mod开发速度
* 代码高亮

#### Mod导入

* 在任意应用打开后缀名带srmod后缀文件，可弹出打开方式，找到并点击'简单火箭Mod'，弹出Mod管理框，可显示当前Mod介绍，前提Mod作者将带有介绍的README.md导入进Mod内，点击导入即可跳转简单火箭并打开Mod。

*PS:Mod导入或许在很多人看来并没用，但根据Google在Android 7.0(API 级别24)权限变更，为了安全考虑在你应用之外显示file://URL会触发FileUriExposedException,如果强行显示需要手机管理器API在24以下或其管理器拥有Root权限才能导入简单火箭Mod,至使决定将SRTool作为中间应用引导使简单火箭打开Mod

#### SR辅助

* 帧数开关
* ~~目标坐标变更~~
