# FaceMerge

#### 介绍
使用OpenCV结合百度AI人脸检测(需要人脸72个关键点)实现人脸融合

#### 示例图
##### 原图一
![原图一](https://images.gitee.com/uploads/images/2019/0518/121744_eac851c9_131538.jpeg "原图一")
##### 原图二
![原图二](https://images.gitee.com/uploads/images/2019/0518/121812_09f7eec5_131538.jpeg "原图二")
##### 融合图
![融合图](https://images.gitee.com/uploads/images/2019/0518/121849_c84fdbd2_131538.jpeg "融合图")




#### 使用说明

1. FaceDetect代码中填写自己百度AI应用的appid apikey secretkey
2. TestFaceMerge代码中修改为自己本地图片路径即可
3. org.opencv jar需要单独引入 文件在resources/file文件夹中
4. opencv_java320.dll需要单独引入项目 文件在resources/file文件夹中


#### OpenCV代码来源

1. https://github.com/lichao3140/Opencv_Java