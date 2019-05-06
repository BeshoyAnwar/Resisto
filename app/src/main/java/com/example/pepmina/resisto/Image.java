import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.*;

public class Image {
    private static final int NUM_CODES = 12;
    // HSV colour bounds
    private static final Scalar COLOR_BOUNDS[][] = {
            {new Scalar(0, 0, 0), new Scalar(180, 250, 50)},        // black
            {new Scalar(98, 109, 20), new Scalar(112, 255, 255)},   // blue
            {new Scalar(10, 100, 20), new Scalar(20, 255, 200)},    // brown
            {new Scalar(20, 100, 100), new Scalar(25, 255, 255)},   // gold
            {new Scalar(45, 100, 50), new Scalar(75, 255, 255)},    // green
            {new Scalar(0, 0, 20), new Scalar(0, 0, 200)},          // grey
            {new Scalar(5, 80, 100), new Scalar(20, 255, 255)},   // orange
            {new Scalar(0, 100, 100), new Scalar(4, 255, 255)},     // red
            {new Scalar(0, 0, 200), new Scalar(0, 0, 230)},         // silver
            {new Scalar(100, 100, 255), new Scalar(150, 150, 255)}, // violet
            {new Scalar(0, 0, 255), new Scalar(20, 20, 255)},       // white
            {new Scalar(23, 41, 100), new Scalar(40, 255, 255)}     // yellow

    };
    private static final String[] COLORS_NAMES = {
            "Black",
            "Blue",
            "Brown",
            "Gold",
            "Green",
            "Grey",
            "Orange",
            "Red",
            "Silver",
            "Violet",
            "White",
            "Yellow"
    };
    // red wraps around in HSV, so we need two ranges
    private static Scalar LOWER_RED1 = new Scalar(0, 100, 100);//done red and brown
    private static Scalar UPPER_RED1 = new Scalar(10, 255, 255);
    private static Scalar LOWER_RED2 = new Scalar(160, 100, 100);//has no detection bur keep it until test with real image
    private static Scalar UPPER_RED2 = new Scalar(179, 255, 255);

    private Mat cuttedImage;
    private Mat resistorBody;
    private ArrayList<Mat> colorsImages;
    private Vector<String> colorsNames;

    public Image(Mat cuttedImage) throws Exception {
        this.cuttedImage=cuttedImage;
        this.setResistorBody();
        setColorsImages();
        setColorsNames();
    }


    public Mat getResistorBody() {
        return resistorBody;
    }

    
    public ArrayList<Mat> getColorsImages() {
        return colorsImages;
    }
    /** 
    * Remove background from the image and extract the resistor body 
    */
    private void setResistorBody()
    {
        Mat img=this.cuttedImage;
        Mat dst = new Mat();
        Mat thresholdImg = new Mat();


        Imgproc.cvtColor(img, dst, Imgproc.COLOR_RGB2GRAY);
        Imgproc.threshold(dst, thresholdImg,120,255, Imgproc.THRESH_BINARY_INV);

        Mat kernel = Mat.ones(10,10, CvType.CV_32F);
        Imgproc.morphologyEx(thresholdImg, thresholdImg, Imgproc.MORPH_OPEN, kernel ,new Point(-1, -1), 2);
        Imgproc.dilate(thresholdImg, thresholdImg, kernel,new Point(-1, -1), 2);
        Imgproc.erode(thresholdImg, thresholdImg, kernel);

        // create the new image
        Mat foreground = new Mat(img.size(), CvType.CV_8UC3, new Scalar(255,255,255));
        List<Mat> bgr = new ArrayList<>();
        List<Mat> maskedImg = new ArrayList<>();
        Core.split(img, bgr);
        Core.bitwise_not(thresholdImg,thresholdImg);
        Mat foreground_b = new Mat(img.size(), CvType.CV_8UC1, new Scalar(255,255,255));
        Mat foreground_g = new Mat(img.size(), CvType.CV_8UC1, new Scalar(255,255,255));
        Mat foreground_r = new Mat(img.size(), CvType.CV_8UC1, new Scalar(255,255,255));
        Core.bitwise_and(bgr.get(0),thresholdImg,foreground_b);
        Core.bitwise_and(bgr.get(1),thresholdImg,foreground_g);
        Core.bitwise_and(bgr.get(2),thresholdImg,foreground_r);
        maskedImg.add(foreground_b);
        maskedImg.add(foreground_g);
        maskedImg.add(foreground_r);
        Core.merge(maskedImg, foreground);

        Mat grayMat = new Mat();
        Mat cannyEdges = new Mat();
        Mat threshold = new Mat();
        Mat hierarchy = new Mat();

        List<MatOfPoint> contourList = new ArrayList<MatOfPoint>(); //A list to store all the contours

        Imgproc.cvtColor(foreground, grayMat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(grayMat, threshold,130,200, Imgproc.THRESH_BINARY_INV);
        Imgproc.findContours(threshold,contourList, hierarchy,Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);

        MatOfPoint maxContour = new MatOfPoint();
        Iterator<MatOfPoint> iterator = contourList.iterator();

        MatOfPoint contour = iterator.next();
        double area = Imgproc.contourArea(contour);
        double maxArea = area;
        int contIndex =0 ;
        int maxContIndex =contIndex ;
        while (iterator.hasNext()){
            contour = iterator.next();
            contIndex ++ ;
            area = Imgproc.contourArea(contour);

            if(area > maxArea ){
                maxArea = area;
                maxContIndex = contIndex ;
                maxContour = contour ;
            }
        }
        Rect r = Imgproc.boundingRect(contourList.get(maxContIndex));
        double height = r.height;
        double width = r.width;
  
        Mat cropImg = img.submat((int) (r.y+(0.05*height)), (int) (r.y+(0.9*height)), (int) (r.x+(0.20*width)), (int) (r.x+(0.80*width)));

        this.resistorBody=cropImg;


    }

    private void setColorsImages() {
        Mat src=resistorBody;
//        Mat kernel=new Mat(3,3,CvType.CV_32S);
//        kernel.put(0, 0,-1,-1,-1,-1,8,-1,-1,-1,-1 );
//        //for(int i=0;i<3;i++)
//          //  for(int j=0;j<3;j++)
//            //{
//                //if(i==1&&j==1) kernel[i][j] = 9;
//              //  kernel.put(i, j, 9);
//            //}
//        Imgproc.filter2D(src,src,-1,kernel);
//        Imgcodecs.imwrite("sharpen.jpg", src);
        Mat resistance=src.clone();
        Mat blurred = src.clone();
        Imgproc.medianBlur(src, blurred, 9);

        Mat gray0 = new Mat(blurred.size(), CvType.CV_8U), gray = new Mat();

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        List<Mat> blurredChannel = new ArrayList<Mat>();
        blurredChannel.add(blurred);
        List<Mat> gray0Channel = new ArrayList<Mat>();
        gray0Channel.add(gray0);

        MatOfPoint2f approxCurve;

        double imageArea=src.height()*src.width();
        //3500 5500
        //3000 5500
        double minArea = 3000.0/59411.0;
        int maxId = -1;
        double maxArea=6500.0/59411.0;
        int numOfColorsInRes=0;
        TreeMap<Double,MatOfPoint> finalContours=new TreeMap<>();
        ArrayList<Double> yOfColors=new ArrayList<Double>();
        for (int c = 0; c < 3; c++) {
            int ch[] = { c, 0 };
            Core.mixChannels(blurredChannel, gray0Channel, new MatOfInt(ch));
            int thresholdLevel = 1;
            for (int t = 0; t < thresholdLevel; t++) {
                if (t == 0) {
                    //10 30 3 true
                    //5 50
                    Imgproc.Canny(gray0, gray, 10, 30, 3, true); // true ?
                    //Imgproc.Canny(gray0, gray, 15, 20);
                    Point p1=new Point(0,0);
                    Point p2=new Point(0,gray.height());
                    Point p3=new Point(gray.width()-1,0);
                    Point p4=new Point(gray.width()-1,gray.height()-1);
                    Imgproc.line(gray,p1,p2,new Scalar(255, 255, 255),1);
                    Imgproc.line(gray,p3,p4,new Scalar(255, 255, 255),1);
                    Imgproc.dilate(gray,gray,Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(5,5)));
                    Imgproc.dilate(gray,gray,Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(3,3)));
                    Imgproc.dilate(gray,gray,Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(3,3)));
                    Imgproc.dilate(gray,gray,Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(3,3)));
                    Imgproc.dilate(gray,gray,Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(3,3)));
                    Imgproc.erode(gray,gray,Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(5,5)));
                    Imgproc.erode(gray,gray,Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(5,5)));
                    Imgproc.erode(gray,gray,Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(3,3)));
                    Imgproc.erode(gray,gray,Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(3,3)));
                    Imgcodecs.imwrite("edge"+c+".jpg", gray);
                    Imgproc.dilate(gray, gray, new Mat(), new Point(-1, -1), 1); // 1
                    // ?
                } else {
                    Imgproc.adaptiveThreshold(gray0, gray, thresholdLevel,
                            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                            Imgproc.THRESH_BINARY,
                            (src.width() + src.height()) / 200, t);
                }

                Imgproc.findContours(gray, contours, new Mat(),
                        Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
                Imgcodecs.imwrite("gray.jpg", gray);

                for (MatOfPoint contour : contours) {
                    MatOfPoint2f temp = new MatOfPoint2f(contour.toArray());
                    double area = Imgproc.contourArea(contour)/imageArea;
                    approxCurve = new MatOfPoint2f();
                    Imgproc.approxPolyDP(temp, approxCurve,
                            Imgproc.arcLength(temp, true) * 0.05, true);
                    Point p=contour.toArray()[0];
                    double y=p.y;
                    if (approxCurve.total() == 4 && area >= minArea&& area<=maxArea&& !repeatedContour(yOfColors,y,contour.height())) {
                        double maxCosine = 0;
                        numOfColorsInRes++;
                        yOfColors.add(y);
                        finalContours.put(y,contour);
                        int id=contours.indexOf(contour);
                        Imgproc.drawContours(src, contours, id, new Scalar(255, 0, 0, .8), 2);
//                        List<Point> curves = approxCurve.toList();
//                        for (int j = 2; j < 5; j++) {
//
//                            double cosine = Math.abs(angle(curves.get(j % 4),
//                                    curves.get(j - 2), curves.get(j - 1)));
//                            maxCosine = Math.max(maxCosine, cosine);
//                        }
//
//                        if (maxCosine < 0.3) {
//                            maxArea = area;
//                            maxId = contours.indexOf(contour);
//                        }
                    }
                }
                Imgcodecs.imwrite("output"+".jpg", src);

            }
        }
        System.out.println(numOfColorsInRes);
        ArrayList<Mat> colors=new ArrayList<>();
        colors=cropColors(resistance,finalContours);
        for(int i=0;i<numOfColorsInRes;i++)
        {
            Imgcodecs.imwrite("ColorNo"+(i+1)+".jpg", colors.get(i));
        }
        System.out.println(numOfColorsInRes);

        //return colors;

        this.colorsImages= colors;
    }

    public Vector<String> getColorsNames() {
        return colorsNames;
    }

    private void setColorsNames() {
        Vector<String> colorsNames=new Vector<>();
        //final double MAX_AREA_PERCENTAGE =250.0/5000;
        for(int i=0;i<colorsImages.size();i++){
            Mat src = colorsImages.get(i);
//            int area=src.rows()*src.width();
//            System.out.println(area);
//            //double maxAreaConst=MAX_AREA_PERCENTAGE*area;
            //System.out.println(maxAreaConst);
            Mat dest = new Mat(src.rows(), src.cols(), CvType.CV_8UC2);//copy the input to dest matrix
            Imgproc.cvtColor(src, dest, Imgproc.COLOR_BGR2HSV);//convert from BGR to HSV
            String colorOfMaxArea="";int maxArea=0;
            for (int j = 0; j < COLOR_BOUNDS.length; j++) {
                Mat mask = new Mat(src.rows(), src.cols(), CvType.CV_8UC2);//create mask has src dimensions
                Core.inRange(dest, COLOR_BOUNDS[j][0], COLOR_BOUNDS[j][1], mask);//perform inrange to j range color
                List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
                Mat hierarchy = new Mat();
                Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
//                if(j==0){
//                    continue;
//                }
                int colorArea=0;
                for (int contIdx = 0; contIdx < contours.size(); contIdx++) {
                    colorArea += (int) Imgproc.contourArea(contours.get(contIdx));
                }
                if(colorArea>maxArea){
                    colorOfMaxArea= COLORS_NAMES[j];
                    maxArea=colorArea;
                }
                //System.out.println("for i= "+i+", Color= "+ COLORS_NAMES[j]+", size= "+contours.size());

                System.out.println("for i= "+i+", Color= "+ COLORS_NAMES[j]+", area= "+colorArea+", size= "+contours.size());
            }
            System.out.println("Color= "+ colorOfMaxArea);
            if(colorOfMaxArea!=""){
                colorsNames.add(colorOfMaxArea);
            }
            else{
                colorsNames.add("Black");
            }
            System.out.println("-------------------------------");
        }
        this.colorsNames=colorsNames;
    }


    private static double angle(Point p1, Point p2, Point p0) {
        double dx1 = p1.x - p0.x;
        double dy1 = p1.y - p0.y;
        double dx2 = p2.x - p0.x;
        double dy2 = p2.y - p0.y;
        return (dx1 * dx2 + dy1 * dy2)
                / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2)
                + 1e-10);
    }

    private static Boolean repeatedContour(ArrayList<Double> existingContours, Double newContour, int range) {
        for (Double contour : existingContours) {
            if (newContour >= contour - range && newContour <= contour + range) {
                return true;
            }
        }
        return false;
    }

    private static ArrayList<Mat> cropColors(Mat resistance, TreeMap<Double, MatOfPoint> countours) {
        ArrayList<Mat> colors = new ArrayList<>();
        for (Map.Entry<Double, MatOfPoint> contour : countours.entrySet()) {
            //Mat mask=Mat.zeros(resistance.rows(),resistance.cols(),CvType.CV_8UC2);
            Mat mask = new Mat();
            Mat temp = new Mat();
            Core.inRange(resistance, new Scalar(0, 0, 0), new Scalar(0, 0, 0), temp);
            resistance.copyTo(mask, temp);
            //Imgcodecs.imwrite("m.jpg", mask);
            Rect r = Imgproc.boundingRect(contour.getValue());
            Imgproc.rectangle(mask, r, new Scalar(255, 255, 255), -1);
            Core.bitwise_and(resistance, mask, mask);
            mask = mask.submat((int) r.y, (int) r.y+r.height, (int) (r.x), (int) (r.x+r.width));
            colors.add(mask);
        }

        return colors;
    }


}
