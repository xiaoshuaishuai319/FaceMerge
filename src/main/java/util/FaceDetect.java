package util;

import bean.FaceV3DetectBean;
import com.baidu.aip.face.AipFace;
import com.baidu.aip.util.Base64Util;
import com.baidu.aip.util.Util;
import org.json.JSONObject;
import org.opencv.core.Point;

import java.util.HashMap;

/**
 * @ClassName: FaceDetect
 * @description: 人脸检测接口返回处理方法
 * @author: 小帅丶
 * @create: 2019-05-18
 **/
public class FaceDetect {
    private static AipFace aipFace;
    //请填写自己应用的appid apikey secretkey
    static {
        aipFace = new AipFace("","","");
    }
    /**
     * 人脸检测并返回72关键点
     * @author 小帅丶
     * @date 2019/5/18
     * @param path 图片路径
     * @return org.opencv.core.Point[]
     **/
    public static Point[] detect(String path) {
        Point[] points;
        try {
        HashMap<String, String> option = new HashMap<String, String>();
        String image = Base64Util.encode(Util.readFileByBytes(path));
        String type = "BASE64";
        option.put("face_field","age,beauty,expression,face_shape,gender,glasses,landmark,landmark150,race,quality,eye_status,emotion,face_type");
            JSONObject jsonObject = aipFace.detect(image,type,option);
            com.alibaba.fastjson.JSON object = com.alibaba.fastjson.JSON.parseObject(jsonObject.toString());
            FaceV3DetectBean bean = com.alibaba.fastjson.JSONObject.toJavaObject(object, FaceV3DetectBean.class);
            int k72 = bean.getResult().getFace_list().get(0).getLandmark72().size();
            points = new Point[k72];
            for (int i = 0; i < k72; i++) {
                double x = bean.getResult().getFace_list().get(0).getLandmark72().get(i).getX();
                double y = bean.getResult().getFace_list().get(0).getLandmark72().get(i).getY();
                points[i] = new Point(x, y);
            }
            return points;
        } catch (Exception e) {
            System.out.println("错误了"+e.getMessage());
            return  null;
        }
    }
}
