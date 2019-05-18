package util;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Subdiv2D;
import org.opencv.photo.Photo;

import java.util.LinkedList;
import java.util.List;


/**
 * @ClassName: OpenCV_33_FaceSwap
 * @description: 人脸融合方法
 * @author: 小帅丶
 * @create: 2019-05-18
 **/
public class OpenCV_33_FaceSwap {
    /**
     * @author 小帅丶
     * @date 2019/5/18
     * @param imgPath1 原始图1
     * @param imgPath2 原始图2
     * @param imgSavePath 融合图保存路径
     * @return void
     **/
    public static void faceMerge(String imgPath1,String imgPath2,String imgSavePath){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // 两张图片地址
        String path1 = imgPath1;
        String path2 = imgPath2;


        // load the two images.
        Mat imgCV1 = Imgcodecs.imread(path1);
        Mat imgCV2 = Imgcodecs.imread(path2);
        if (null == imgCV1 || imgCV1.cols() <= 0 || null == imgCV2 || imgCV2.cols() <= 0) {
            System.out.println("There is wrong with images");
            return;
        }
        ImageUI ui1 = new ImageUI();
        ImageUI ui2 = new ImageUI();
        ui1.imshow("原始图像A", imgCV1);
        ui2.imshow("原始图像B", imgCV2);
        System.out.println("image readed by opencv");

        // 人脸识别出的关键点
        Point[] points1 = FaceDetect.detect(path1);
        Point[] points2 = FaceDetect.detect(path2);

        // 计算凸包
        Mat imgCV1Warped = imgCV2.clone();
        imgCV1.convertTo(imgCV1, CvType.CV_32F);
        imgCV1Warped.convertTo(imgCV1Warped, CvType.CV_32F);

        MatOfInt hullIndex = new MatOfInt();
        Imgproc.convexHull(new MatOfPoint(points2), hullIndex, true);
        int[] hullIndexArray = hullIndex.toArray();
        int hullIndexLen = hullIndexArray.length;
        List<Point> hull1 = new LinkedList<Point>();
        List<Point> hull2 = new LinkedList<Point>();

        // 保存组成凸包的关键点
        for (int i = 0; i < hullIndexLen; i++) {
            hull1.add(points1[hullIndexArray[i]]);
            hull2.add(points2[hullIndexArray[i]]);
        }

        // delaunay triangulation 三角剖分和仿射变换
        Rect rect = new Rect(0, 0, imgCV1Warped.cols(), imgCV1Warped.rows());
        List<Correspondens> delaunayTri = delaunayTriangulation(hull2, rect);
        for(int i=0;i<delaunayTri.size();++i) {
            List<Point> t1 = new LinkedList<Point>();
            List<Point> t2 = new LinkedList<Point>();
            Correspondens corpd = delaunayTri.get(i);
            for(int j = 0; j < 3; j++) {
                t1.add(hull1.get(corpd.getIndex().get(j)));
                t2.add(hull2.get(corpd.getIndex().get(j)));
            }
            imgCV1Warped = warpTriangle(imgCV1, imgCV1Warped, list2MP(t1), list2MP(t2), i);
        }

        // 无缝融合
        List<Point> hull8U = new LinkedList<Point>();
        for(int i = 0; i < hull2.size(); ++i) {
            Point pt = new Point(hull2.get(i).x, hull2.get(i).y);
            hull8U.add(pt);
        }

        Mat mask = Mat.zeros(imgCV2.rows(), imgCV2.cols(), imgCV2.depth());
        Imgproc.fillConvexPoly(mask, list2MP(hull8U), new Scalar(255, 255, 255));

        Rect r = Imgproc.boundingRect(list2MP(hull2));
        Point center = new Point((r.tl().x + r.br().x)/2, (r.tl().y + r.br().y)/2);

        Mat output = new Mat();
        //无缝融合
        imgCV1Warped.convertTo(imgCV1Warped, CvType.CV_8UC3);
        Photo.seamlessClone(imgCV1Warped, imgCV2, mask, center, output, Photo.NORMAL_CLONE);

        ImageUI out = new ImageUI();
        out.imshow("换脸合成结果", output);

        String filename = imgSavePath;
        Imgcodecs.imwrite(filename, output);
    }
    /**
     * 获取Delaunay三角形的列表
     * @param hull
     * @param rect
     * @return
     */
    public static List<Correspondens> delaunayTriangulation(List<Point> hull, Rect rect) {
        Subdiv2D subdiv = new Subdiv2D(rect);
        for(int it = 0; it < hull.size(); it++) {
            subdiv.insert(hull.get(it));
        }
        MatOfFloat6 triangles = new MatOfFloat6();
        subdiv.getTriangleList(triangles);
        int cnt = triangles.rows();
        float buff[] = new float[cnt*6];
        triangles.get(0, 0, buff);

        List<Correspondens> delaunayTri = new LinkedList<Correspondens>();
        for(int i = 0; i < cnt; ++i) {
            List<Point> points = new LinkedList<Point>();
            points.add(new Point(buff[6*i+0], buff[6*i+1]));
            points.add(new Point(buff[6*i+2], buff[6*i+3]));
            points.add(new Point(buff[6*i+4], buff[6*i+5]));

            Correspondens ind = new Correspondens();
            if (rect.contains(points.get(0)) && rect.contains(points.get(1)) && rect.contains(points.get(2))) {
                int count = 0;
                for (int j = 0; j < 3; j++) {
                    for (int k = 0; k < hull.size(); k++) {
                        if (Math.abs(points.get(j).x - hull.get(k).x) < 1.0 && Math.abs(points.get(j).y - hull.get(k).y) < 1.0) {
                            ind.add(k);
                            count++;
                        }
                    }
                }
                if (count == 3)
                    delaunayTri.add(ind);
            }
        }
        return delaunayTri;
    }

    /**
     *
     * @param img1
     * @param img2
     * @param t1
     * @param t2
     * @param z
     * @return
     */
    public static Mat warpTriangle(Mat img1, Mat img2, MatOfPoint t1, MatOfPoint t2, int z) {
        Rect r1 = Imgproc.boundingRect(t1);
        Rect r2 = Imgproc.boundingRect(t2);

        Point[] t1Points = t1.toArray();
        Point[] t2Points = t2.toArray();

        List<Point> t1Rect = new LinkedList<Point>();
        List<Point> t2Rect = new LinkedList<Point>();
        List<Point> t2RectInt = new LinkedList<Point>();

        for (int i = 0; i < 3; i++) {
            t1Rect.add(new Point(t1Points[i].x - r1.x, t1Points[i].y - r1.y));
            t2Rect.add(new Point(t2Points[i].x - r2.x, t2Points[i].y - r2.y));
            t2RectInt.add(new Point(t2Points[i].x - r2.x, t2Points[i].y - r2.y));
        }
        // mask 包含目标图片三个凸点的黑色矩形
        Mat mask = Mat.zeros(r2.height, r2.width, CvType.CV_32FC3);
        Imgproc.fillConvexPoly(mask, list2MP(t2RectInt), new Scalar(1.0, 1.0, 1.0), 16, 0);

        Mat img1Rect = new Mat();
        img1.submat(r1).copyTo(img1Rect);

        // img2Rect 原始图片适应mask大小并调整位置的图片
        Mat img2Rect = Mat.zeros(r2.height, r2.width, img1Rect.type());
        img2Rect = applyAffineTransform(img2Rect, img1Rect, t1Rect, t2Rect);

        Core.multiply(img2Rect, mask, img2Rect); // img2Rect在mask三个点之间的图片
        Mat dst = new Mat();
        Core.subtract(mask, new Scalar(1.0, 1.0, 1.0), dst);
        Core.multiply(img2.submat(r2), dst, img2.submat(r2));
        Core.absdiff(img2.submat(r2), img2Rect, img2.submat(r2));

        return img2;
    }

    public static Mat applyAffineTransform(Mat warpImage, Mat src, List<Point> srcTri, List<Point> dstTri) {
        Mat warpMat = Imgproc.getAffineTransform(list2MP2(srcTri), list2MP2(dstTri));
        Imgproc.warpAffine(src, warpImage, warpMat, warpImage.size(), Imgproc.INTER_LINEAR);
        return warpImage;
    }

    /**
     * List exchange to MatOfPoint
     * @param points
     * @return
     */
    public static MatOfPoint list2MP(List<Point> points) {
        Point[] t = (Point[])points.toArray(new Point[points.size()]);
        return new MatOfPoint(t);
    }

    /**
     * List exchange to MatOfPoint2f
     * @param points
     * @return
     */
    public static MatOfPoint2f list2MP2(List<Point> points) {
        Point[] t = (Point[])points.toArray(new Point[points.size()]);
        return new MatOfPoint2f(t);
    }
}
