import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.*;

public class main {

    private static final int NUM_CODES = 12;

    // HSV colour bounds
    private static final Scalar COLOR_BOUNDS[][] = {
            {new Scalar(0, 0, 0), new Scalar(180, 250, 50)},    // black
            {new Scalar(98, 109, 20), new Scalar(112, 255, 255)},  // blue
            {new Scalar(10, 100, 20), new Scalar(20, 255, 200)},    // brown
            {new Scalar(20, 100, 100), new Scalar(25, 255, 255)},// gold
            {new Scalar(45, 100, 50), new Scalar(75, 255, 255)},   // green
            {new Scalar(0, 0, 20), new Scalar(0, 0, 200)},           // grey
            {new Scalar(11, 100, 100), new Scalar(20, 255, 255)},   // orange
            {new Scalar(0, 100, 100), new Scalar(9, 255, 255)},// red
            {new Scalar(0, 0, 200), new Scalar(0, 0, 230)},// silver
            {new Scalar(100, 100, 255), new Scalar(150, 150, 255)}, // violet
            {new Scalar(0, 0, 255), new Scalar(20, 20, 255)},     // white
            {new Scalar(23, 41, 100), new Scalar(40, 255, 255)} // yellow

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

    //private SparseIntArray _locationValues = new SparseIntArray(4);
    private static HashMap<Integer, Integer> _locationValues = new HashMap<Integer, Integer>();

    public static double angle(Point p1, Point p2, Point p0) {
        double dx1 = p1.x - p0.x;
        double dy1 = p1.y - p0.y;
        double dx2 = p2.x - p0.x;
        double dy2 = p2.y - p0.y;
        return (dx1 * dx2 + dy1 * dy2)
                / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2)
                + 1e-10);
    }

    public static Boolean repeatedContour(ArrayList<Double> existingContours, Double newContour, int range) {
        for (Double contour : existingContours) {
            if (newContour >= contour - range && newContour <= contour + range) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<Mat> cropColors(Mat resistance, TreeMap<Double, MatOfPoint> countours) {
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
            colors.add(mask);
        }

        return colors;
    }

    public static ArrayList<Mat> findRectangle(Mat src) throws Exception {
        Mat resistance = src.clone();
        Mat blurred = src.clone();
        Imgproc.medianBlur(src, blurred, 9);

        Mat gray0 = new Mat(blurred.size(), CvType.CV_8U), gray = new Mat();

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        List<Mat> blurredChannel = new ArrayList<Mat>();
        blurredChannel.add(blurred);
        List<Mat> gray0Channel = new ArrayList<Mat>();
        gray0Channel.add(gray0);

        MatOfPoint2f approxCurve;

        double imageArea = src.height() * src.width();
        double minArea = 3000.0 / 59411.0;
        int maxId = -1;
        double maxArea = 5000.0 / 59411.0;
        int numOfColorsInRes = 0;
        TreeMap<Double, MatOfPoint> finalContours = new TreeMap<>();
        ArrayList<Double> yOfColors = new ArrayList<Double>();
        for (int c = 0; c < 3; c++) {
            int ch[] = {c, 0};
            Core.mixChannels(blurredChannel, gray0Channel, new MatOfInt(ch));
            int thresholdLevel = 1;
            for (int t = 0; t < thresholdLevel; t++) {
                if (t == 0) {
                    Imgproc.Canny(gray0, gray, 10, 30, 3, true); // true ?
                    //Imgproc.Canny(gray0, gray, 15, 20);
                    Point p1 = new Point(0, 0);
                    Point p2 = new Point(0, gray.height());
                    Point p3 = new Point(gray.width() - 1, 0);
                    Point p4 = new Point(gray.width() - 1, gray.height() - 1);
                    Imgproc.line(gray, p1, p2, new Scalar(255, 255, 255), 1);
                    Imgproc.line(gray, p3, p4, new Scalar(255, 255, 255), 1);
                    Imgcodecs.imwrite("edge.jpg", gray);
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
                    double area = Imgproc.contourArea(contour) / imageArea;
                    approxCurve = new MatOfPoint2f();
                    Imgproc.approxPolyDP(temp, approxCurve,
                            Imgproc.arcLength(temp, true) * 0.05, true);
                    Point p = contour.toArray()[0];
                    double y = p.y;
                    if (approxCurve.total() == 4 && area >= minArea && area <= maxArea && !repeatedContour(yOfColors, y, contour.height())) {
                        double maxCosine = 0;
                        numOfColorsInRes++;
                        yOfColors.add(y);
                        finalContours.put(y, contour);
                        int id = contours.indexOf(contour);
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

            }
        }
        ArrayList<Mat> colors = new ArrayList<>();
        colors = cropColors(resistance, finalContours);
        for(int i=0;i<numOfColorsInRes;i++)
        {
            Imgcodecs.imwrite("ColorNo"+(i+1)+".jpg", colors.get(i));
        }
       System.out.println(numOfColorsInRes);

        return colors;
    }

    public static Vector<String> detectColors(ArrayList<Mat> colors){
        Vector<String> colorsNames=new Vector<>();
        for(int i=0;i<colors.size();i++){
            Mat src = colors.get(i);
            Mat dest = new Mat(src.rows(), src.cols(), CvType.CV_8UC2);//copy the input to dest matrix
            Imgproc.cvtColor(src, dest, Imgproc.COLOR_BGR2HSV);//convert from BGR to HSV
            String colorOfMaxArea="";int maxArea=0;
            for (int j = 0; j < COLOR_BOUNDS.length; j++) {
                Mat mask = new Mat(src.rows(), src.cols(), CvType.CV_8UC2);//create mask has src dimensions
                Core.inRange(dest, COLOR_BOUNDS[j][0], COLOR_BOUNDS[j][1], mask);//perform inrange to j range color
                List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
                Mat hierarchy = new Mat();
                Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
                if(j==0 && contours.size()==2){
                    continue;
                }
                int area=0;
                for (int contIdx = 0; contIdx < contours.size(); contIdx++) {
                    area += (int) Imgproc.contourArea(contours.get(contIdx));
                }
                if(area>maxArea){
                    colorOfMaxArea= COLORS_NAMES[j];
                    maxArea=area;
                }
                //System.out.println("for i= "+i+", Color= "+ COLORS_NAMES[j]+", size= "+contours.size());

                //System.out.println("for i= "+i+", Color= "+ COLORS_NAMES[j]+", area= "+area+", size= "+contours.size());
            }
            //System.out.println("Color= "+ colorOfMaxArea);
            colorsNames.add(colorOfMaxArea);
            //System.out.println("-------------------------------");
        }
        return colorsNames;
    }
    public static void main(String[] args) throws Exception {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat source = Imgcodecs.imread("tests/r1.jpg");
        ArrayList<Mat> colors=findRectangle(source);
        System.out.println(colors.size());
        Vector<String> colors_names= detectColors(colors);


        for(int i=0;i<colors_names.size();i++) System.out.println(colors_names.get(i));
        Resistance res=new Resistance(colors_names);
        System.out.println(res.toString());
    }
}
