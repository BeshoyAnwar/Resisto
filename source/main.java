import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.*;

public class main {

    private static final int NUM_CODES = 12;

    // HSV colour bounds
    private static final Scalar COLOR_BOUNDS[][] = {
            { new Scalar(0, 0, 0),   new Scalar(180, 250, 50) },    // black
            { new Scalar(98, 109, 20), new Scalar(112, 255, 255) },  // blue
            { new Scalar(10, 100, 20), new Scalar(20, 255, 200) },    // brown
            { new Scalar(20, 100, 100) , new Scalar(25, 255, 255) },// gold
            { new Scalar(45, 100, 50), new Scalar(75, 255, 255) },   // green
            { new Scalar(0,0, 20), new Scalar(0, 0, 200) },           // grey
            { new Scalar(11, 100, 100) , new Scalar(20, 255, 255) },   // orange
            {new Scalar(0, 100, 100),new Scalar(9, 255, 255)},// red
            { new Scalar(0,0, 200), new Scalar(0, 0, 230) },// silver
            { new Scalar(100, 100, 255) , new Scalar(150, 150, 255)}, // violet
            { new Scalar(0, 0, 255), new Scalar(20, 20, 255) },     // white
            { new Scalar(23,41,100),new Scalar(40,255,255) } // yellow

    };
    private static final String[]names = {
            "black",
            "blue",
            "brown",
            "gold",
            "green",
            "grey",
            "orange",
            "red",
            "silver",
            "violet",
            "white",
            "yellow"
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
    public static Boolean repeatedContour(ArrayList<Double> existingContours,Double newContour,int range)
    {
        for(Double contour:existingContours)
        {
            if(newContour>=contour-range&&newContour<=contour+range)
            {
                return true;
            }
        }
        return false;
    }
    public static ArrayList<Mat> cropColors(Mat resistance,TreeMap<Double,MatOfPoint> countours)
    {
        ArrayList<Mat> colors=new ArrayList<>();
        for(Map.Entry<Double,MatOfPoint> contour:countours.entrySet())
        {
            //Mat mask=Mat.zeros(resistance.rows(),resistance.cols(),CvType.CV_8UC2);
            Mat mask=new Mat();
            Mat temp=new Mat();
            Core.inRange(resistance,new Scalar(0,0,0),new Scalar(0,0,0),temp);
            resistance.copyTo(mask,temp);
            //Imgcodecs.imwrite("m.jpg", mask);
            Rect r=Imgproc.boundingRect(contour.getValue());
            Imgproc.rectangle(mask,r,new Scalar(255,255,255),-1);
            Core.bitwise_and(resistance,mask,mask);
            colors.add(mask);
        }

        return colors;
    }
    public static ArrayList<Mat> findRectangle(Mat src) throws Exception {
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
        double minArea = 3000.0/59411.0;
        int maxId = -1;
        double maxArea=5000.0/59411.0;
        int numOfColorsInRes=0;
        TreeMap<Double,MatOfPoint> finalContours=new TreeMap<>();
        ArrayList<Double> yOfColors=new ArrayList<Double>();
        for (int c = 0; c < 3; c++) {
            int ch[] = { c, 0 };
            Core.mixChannels(blurredChannel, gray0Channel, new MatOfInt(ch));
            int thresholdLevel = 1;
            for (int t = 0; t < thresholdLevel; t++) {
                if (t == 0) {
                    Imgproc.Canny(gray0, gray, 10, 30, 3, true); // true ?
                    //Imgproc.Canny(gray0, gray, 15, 20);
                    Point p1=new Point(0,0);
                    Point p2=new Point(0,gray.height());
                    Point p3=new Point(gray.width()-1,0);
                    Point p4=new Point(gray.width()-1,gray.height()-1);
                    Imgproc.line(gray,p1,p2,new Scalar(255, 255, 255),1);
                    Imgproc.line(gray,p3,p4,new Scalar(255, 255, 255),1);
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

            }
        }
        ArrayList<Mat> colors=new ArrayList<>();
        colors=cropColors(resistance,finalContours);
//        for(int i=0;i<numOfColorsInRes;i++)
//        {
//            Imgcodecs.imwrite("ColorNo"+(i+1)+".jpg", colors.get(i));
//        }
//        System.out.println(numOfColorsInRes);

        return colors;
    }

    public static void main(String[] args){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        for (int j=0;j<COLOR_BOUNDS.length;j++){
            for(int i=0;i<names.length;i++){
                String in_path="in\\"+names[i]+".png";
                String out_path="out\\"+names[j]+"_range_"+names[i]+"_input"+".png";
                Mat source = Imgcodecs.imread(in_path);//read input image
                Mat dest = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);//copy the input to dest matrix
                Imgproc.cvtColor(source, dest, Imgproc.COLOR_BGR2HSV);//convert from BGR to HSV
                Mat mask = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);//create mask has source dimensions
                Core.inRange(dest, COLOR_BOUNDS[j][0], COLOR_BOUNDS[j][1], mask);//perform inrange to j range color
                Imgcodecs.imwrite(out_path, mask);
            }
        }
       /* Mat source = Imgcodecs.imread("black.png");//read input image
        Mat dest = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);//copy the input to dest matrix
        Imgproc.cvtColor(source, dest, Imgproc.COLOR_BGR2HSV);//convert from BGR to HSV
        Mat mask = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);//create mask has source dimensions
        Core.inRange(dest, COLOR_BOUNDS[0][0], COLOR_BOUNDS[0][1], mask);//perform inrange to black range
        Imgcodecs.imwrite("out/black.png", mask);

         source = Imgcodecs.imread("blue.png");//read input image
         dest = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);//copy the input to dest matrix
        Imgproc.cvtColor(source, dest, Imgproc.COLOR_BGR2HSV);//convert from BGR to HSV
         mask = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);//create mask has source dimensions
        Core.inRange(dest, COLOR_BOUNDS[0][0], COLOR_BOUNDS[0][1], mask);//perform inrange to black range
        Imgcodecs.imwrite("out/blue.png", mask);

        source = Imgcodecs.imread("brown.png");//read input image
        dest = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);//copy the input to dest matrix
        Imgproc.cvtColor(source, dest, Imgproc.COLOR_BGR2HSV);//convert from BGR to HSV
        mask = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);//create mask has source dimensions
        Core.inRange(dest, COLOR_BOUNDS[0][0], COLOR_BOUNDS[0][1], mask);//perform inrange to black range
        Imgcodecs.imwrite("out/brown.png", mask);

        source = Imgcodecs.imread("gold.png");//read input image
        dest = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);//copy the input to dest matrix
        Imgproc.cvtColor(source, dest, Imgproc.COLOR_BGR2HSV);//convert from BGR to HSV
        mask = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);//create mask has source dimensions
        Core.inRange(dest, COLOR_BOUNDS[0][0], COLOR_BOUNDS[0][1], mask);//perform inrange to black range
        Imgcodecs.imwrite("out/gold.png", mask);

        source = Imgcodecs.imread("green.png");//read input image
        dest = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);//copy the input to dest matrix
        Imgproc.cvtColor(source, dest, Imgproc.COLOR_BGR2HSV);//convert from BGR to HSV
        mask = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);//create mask has source dimensions
        Core.inRange(dest, COLOR_BOUNDS[0][0], COLOR_BOUNDS[0][1], mask);//perform inrange to black range
        Imgcodecs.imwrite("out/green.png", mask);

        source = Imgcodecs.imread("grey.png");//read input image
        dest = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);//copy the input to dest matrix
        Imgproc.cvtColor(source, dest, Imgproc.COLOR_BGR2HSV);//convert from BGR to HSV
        mask = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);//create mask has source dimensions
        Core.inRange(dest, COLOR_BOUNDS[0][0], COLOR_BOUNDS[0][1], mask);//perform inrange to black range
        Imgcodecs.imwrite("out/grey.png", mask);

        source = Imgcodecs.imread("orange.png");//read input image
        dest = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);//copy the input to dest matrix
        Imgproc.cvtColor(source, dest, Imgproc.COLOR_BGR2HSV);//convert from BGR to HSV
        mask = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);//create mask has source dimensions
        Core.inRange(dest, COLOR_BOUNDS[0][0], COLOR_BOUNDS[0][1], mask);//perform inrange to black range
        Imgcodecs.imwrite("out/orange.png", mask);

        source = Imgcodecs.imread("red.png");//read input image
        dest = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);//copy the input to dest matrix
        Imgproc.cvtColor(source, dest, Imgproc.COLOR_BGR2HSV);//convert from BGR to HSV
        mask = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);//create mask has source dimensions
        Core.inRange(dest, COLOR_BOUNDS[0][0], COLOR_BOUNDS[0][1], mask);//perform inrange to black range
        Imgcodecs.imwrite("out/red.png", mask);

        source = Imgcodecs.imread("silver.png");//read input image
        dest = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);//copy the input to dest matrix
        Imgproc.cvtColor(source, dest, Imgproc.COLOR_BGR2HSV);//convert from BGR to HSV
        mask = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);//create mask has source dimensions
        Core.inRange(dest, COLOR_BOUNDS[0][0], COLOR_BOUNDS[0][1], mask);//perform inrange to black range
        Imgcodecs.imwrite("out/silver.png", mask);

        source = Imgcodecs.imread("violet.png");//read input image
        dest = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);//copy the input to dest matrix
        Imgproc.cvtColor(source, dest, Imgproc.COLOR_BGR2HSV);//convert from BGR to HSV
        mask = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);//create mask has source dimensions
        Core.inRange(dest, COLOR_BOUNDS[0][0], COLOR_BOUNDS[0][1], mask);//perform inrange to black range
        Imgcodecs.imwrite("out/violet.png", mask);

        source = Imgcodecs.imread("white.png");//read input image
        dest = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);//copy the input to dest matrix
        Imgproc.cvtColor(source, dest, Imgproc.COLOR_BGR2HSV);//convert from BGR to HSV
        mask = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);//create mask has source dimensions
        Core.inRange(dest, COLOR_BOUNDS[0][0], COLOR_BOUNDS[0][1], mask);//perform inrange to black range
        Imgcodecs.imwrite("out/white.png", mask);

        source = Imgcodecs.imread((String)names[0]+".png");//read input image
        dest = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);//copy the input to dest matrix
        Imgproc.cvtColor(source, dest, Imgproc.COLOR_BGR2HSV);//convert from BGR to HSV
        mask = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);//create mask has source dimensions
        Core.inRange(dest, COLOR_BOUNDS[0][0], COLOR_BOUNDS[0][1], mask);//perform inrange to black range
        Imgcodecs.imwrite("out/yellow.png", mask);



*/












        /*for(int i=0;i<names.length;i++){
            String in_path=path+"\\in\\"+names[i]+".png";
            String out_path=path+"\\out\\"+names[i]+"_input_"+"black_range"+".png";
            Mat source = Imgcodecs.imread(String.valueOf(names[0]));//read input image
            Mat dest = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);//copy the input to dest matrix
            Imgproc.cvtColor(source, dest, Imgproc.COLOR_BGR2HSV);//convert from BGR to HSV
            Mat mask = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);//create mask has source dimensions
            Core.inRange(dest, COLOR_BOUNDS[0][0], COLOR_BOUNDS[0][1], mask);//perform inrange to black range
            Imgcodecs.imwrite("out.png", mask);
        }*/

        /* Cropping rectangles*/
//        int numOfTests=9;
//        for(int i=1;i<=numOfTests;i++)
//        {
//            Mat source = Imgcodecs.imread("resisto"+i+".jpg");
//            Mat dest=new Mat();
//            try {
//                dest= findRectangle(source);
//                Imgcodecs.imwrite("output"+i+".jpg", dest);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
        Mat source = Imgcodecs.imread("resisto9.jpg");
        ArrayList<Mat> colors=new ArrayList<>();
        try {
            colors=findRectangle(source);
        } catch (Exception e) {
            e.printStackTrace();
        }


        //*************************************************here*************************************************************/

        /*Mat source =
                Imgcodecs.imread("resisto1.jpg");
        Mat dest = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);
        Imgproc.cvtColor(source, dest, Imgproc.COLOR_BGR2HSV);
        Mat mask = new Mat(source.rows(),source.cols(),CvType.CV_8UC2);

        SortedMap<Integer, Integer> colors_location =
                new TreeMap<Integer, Integer>();
        int area;
        for(int i = 0; i < NUM_CODES; i++) {

            Core.inRange(dest, COLOR_BOUNDS[i][0], COLOR_BOUNDS[i][1], mask);
            List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
            //System.out.println("for i=" + i + ", size= " + contours.size());
            Imgcodecs.imwrite("mask\\"+names[i]+".png", mask);
            System.out.println("Color= "+ names[i]+", size= "+contours.size());
            for (int contIdx = 0; contIdx < contours.size(); contIdx++) {
                area = (int) Imgproc.contourArea(contours.get(contIdx));
                int cx;
                Moments M = Imgproc.moments(contours.get(contIdx));
                cx = (int) (M.get_m10() / M.get_m00());
                if (area>200 && area<800){

                    colors_location.put(cx, i);
                    //System.out.println("    area= " + area+",cx= "+cx);
                }
                System.out.println("    area= " + area+",cx= "+cx);
            }
        }

        Set s = colors_location.entrySet();
        // Using iterator in SortedMap
        Iterator i = s.iterator();
		while (i.hasNext())
        {
            Map.Entry m = (Map.Entry)i.next();

            int key = (Integer)m.getKey();
            int value = (Integer)m.getValue();

            System.out.println("Key : " + key +
                    "  value : " + value);
        }*/
        /*List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Core.inRange(dest,COLOR_BOUNDS[4][0],COLOR_BOUNDS[4][1], mask);
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        for (int contIdx = 0; contIdx < contours.size(); contIdx++) {
            area = (int) Imgproc.contourArea(contours.get(contIdx));
            Moments M = Imgproc.moments(contours.get(contIdx));
            int cx = (int) (M.get_m10() / M.get_m00());
            if (area>20){

                colors_location.put(cx, 5);
            }
            System.out.println("for cx=" + cx + ", area= " + area);
        }
       /* for (int i = 0; i < contours.size(); i++) {
            Imgproc.drawContours(mask, contours, i, new Scalar(0, 0, 255), -1);
        }*/


/*
        Set s = colors_location.entrySet();
        // Using iterator in SortedMap
        Iterator i = s.iterator();

        // Traversing map. Note that the traversal
        // produced sorted (by keys) output .

            /*if (() > 20)
            {


                // if a colour band is split into multiple contours
                // we take the largest and consider only its centroid
                boolean shouldStoreLocation = true;
                for(int locIdx = 0; locIdx < _locationValues.size(); locIdx++)
                {
                    if(Math.abs(_locationValues.keyAt(locIdx) - cx) < 10)
                    {
                        if (areas.get(_locationValues.keyAt(locIdx)) > area)
                        {
                            shouldStoreLocation = false;
                            break;
                        }
                        else
                        {
                            _locationValues.delete(_locationValues.keyAt(locIdx));
                            areas.delete(_locationValues.keyAt(locIdx));
                        }
                    }
                }

                if(shouldStoreLocation)
                {
                    areas.put(cx, area);
                    _locationValues.put(cx, i);
                }
            }*/
/*
        Imgcodecs.imwrite("output.jpg", mask);
        //processFrame(source);
        Mat destination = new Mat(source.rows(), source.cols(), source.type());
/*
        // applying brightness enhacement
        source.convertTo(destination, -1, 1, 50);

        // output image
        Imgcodecs.imwrite("output.jpg", destination);*/
    }
}
