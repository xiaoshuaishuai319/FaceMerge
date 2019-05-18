import util.OpenCV_33_FaceSwap;

/**
 * @ClassName: TestFaceMerge
 * @description: 示例代码
 * @author: 小帅丶
 * @create: 2019-05-18
 **/
public class TestFaceMerge {
    public static void main(String[] args) {
        String path1 = "E:\\testimg\\face4.jpg";//原图1
        String path2 = "E:\\testimg\\facetemplate.jpg";//原图2
        String filename = "E:\\testimg\\output.jpg";//融合图
        OpenCV_33_FaceSwap.faceMerge(path1,path2,filename);
    }

}
