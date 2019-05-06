package com.example.pepmina.resisto;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.*;

public class Image {
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

    private Mat cuttedImage;
    private Mat resistorBody;
    private ArrayList<Mat> colorsImages;
    private Vector<String> colorsNames;

    /**
     * Image constructor that set all attributes of the Image object (resistorBody ,colorsImages and colorsNames)
     * @param cuttedImage: The image bounded by the rectangle drawn in the camera
     * @throws Exception
     */
    public Image(Mat cuttedImage) throws Exception {
        this.cuttedImage=cuttedImage;
        this.setResistorBody();
        setColorsImages();
        setColorsNames();
    }

    /**
     * getter function that return mat object cantain the body of the resistor only
     * @return resistorBody
     */
    public Mat getResistorBody() {
        return resistorBody;
    }

    /**
     * getter function that return the colors images that has been cropped from body of the resistor
     * @return colorsImages
     */
    public ArrayList<Mat> getColorsImages() {
        return colorsImages;
    }

    /**
     * getter function that return the colors names that has been cropped from body of the resistor
     * @return colorsNames
     */
    public Vector<String> getColorsNames() {
        return colorsNames;
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

    /**
     * This method detect the colors in the resistance body, extract them and set them in the colorsImages attribute.
     */
    private void setColorsImages() {
        Mat src=resistorBody;
        Mat resistance=src.clone();
        //blurring the image
        Mat blurred = src.clone();
        Imgproc.medianBlur(src, blurred, 9);
        Mat gray0 = new Mat(blurred.size(), CvType.CV_8U), gray = new Mat();
        // create array list containing all contours in the image
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        List<Mat> blurredChannel = new ArrayList<Mat>();
        blurredChannel.add(blurred);
        List<Mat> gray0Channel = new ArrayList<Mat>();
        gray0Channel.add(gray0);
        MatOfPoint2f approxCurve;
        // get the area of the image
        double imageArea=src.height()*src.width();
        // Min. and Max. area percentage of the color contour
        double minArea = 3000.0/59411.0;
        double maxArea=6500.0/59411.0;
        // variable to calculate the number of colors in a resistance and initialize it with zero
        int numOfColorsInRes=0;
        // TreeMap of the final accepted contours of the colors with y coordinate is the key
        TreeMap<Double,MatOfPoint> finalContours=new TreeMap<>();
        // create array list containing the y coordinate of the accepted contours
        ArrayList<Double> yOfColors=new ArrayList<Double>();
        for (int c = 0; c < 3; c++) {
            int ch[] = { c, 0 };
            Core.mixChannels(blurredChannel, gray0Channel, new MatOfInt(ch));
            int thresholdLevel = 1;
            for (int t = 0; t < thresholdLevel; t++) {
                if (t == 0) {
                    // Apply edge detection
                    Imgproc.Canny(gray0, gray, 10, 30, 3, true);
                    // create two vertical lines at the edge of the image
                    Point p1=new Point(0,0);
                    Point p2=new Point(0,gray.height());
                    Point p3=new Point(gray.width()-1,0);
                    Point p4=new Point(gray.width()-1,gray.height()-1);
                    Imgproc.line(gray,p1,p2,new Scalar(255, 255, 255),1);
                    Imgproc.line(gray,p3,p4,new Scalar(255, 255, 255),1);
                    // dilate then erode to complete rectangles
                    Imgproc.dilate(gray,gray,Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(5,5)),new Point(-1, -1),4);
                    Imgproc.erode(gray,gray,Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(5,5)),new Point(-1, -1),4);
                } else {
                    Imgproc.adaptiveThreshold(gray0, gray, thresholdLevel,
                            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                            Imgproc.THRESH_BINARY,
                            (src.width() + src.height()) / 200, t);
                }

                // find all contours in the image
                Imgproc.findContours(gray, contours, new Mat(),
                Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
                // loop every contour in the image
                for (MatOfPoint contour : contours) {
                    MatOfPoint2f temp = new MatOfPoint2f(contour.toArray());
                    // get the area of the contour
                    double area = Imgproc.contourArea(contour)/imageArea;
                    // adjust the rectangle approximation
                    // approxCurve is the edges of the contour
                    approxCurve = new MatOfPoint2f();
                    Imgproc.approxPolyDP(temp, approxCurve,
             Imgproc.arcLength(temp, true) * 0.05, true);
                    // get the contour start point
                    Point p=contour.toArray()[0];
                    // get the y-coordinate of the point
                    double y=p.y;
                    // test if the contour is considered a color contour
                    // test if the contour has four edges
                    // test if the area of the contour is between the minimum and maximum accepted area
                    // test if we have't accepted a contour in the same location
                    if (approxCurve.total() == 4 && area >= minArea&& area<=maxArea&& !repeatedContour(yOfColors,y,contour.height())) {
                        // increase number of colors in a resistance
                        numOfColorsInRes++;
                        // add the y-coordinate of the contour to yOfColors
                        yOfColors.add(y);
                        // put this contour and it's y-coordinate in the finalContour TreeMap
                        finalContours.put(y,contour);
                        // get the index of the accepted contour
                        int id=contours.indexOf(contour);
                        // draw the accepted contour to the resistance image
                        Imgproc.drawContours(src, contours, id, new Scalar(255, 0, 0, .8), 2);
                    }
                }
            }
        }
        // create array list of images of colors
        ArrayList<Mat> colors=new ArrayList<>();
        // crop the colors from the resistance body and put them in the colors array list
        colors=cropColors(resistance,finalContours);
        // set colorsImages attribute to colors
        this.colorsImages= colors;
    }

    /**
     * setter function that detect the color of each image in colorImages and change the colorNames attribute
     */
    private void setColorsNames() {
        Vector<String> colorsNames=new Vector<>();
        //loop over the colorImages to detect which color in each image
        for(int i=0;i<colorsImages.size();i++){
            Mat src = colorsImages.get(i);
            Mat dest = new Mat(src.rows(), src.cols(), CvType.CV_8UC2);//create dest image with same size of src image
            Imgproc.cvtColor(src, dest, Imgproc.COLOR_RGB2HSV);//convert from RGB to HSV
            // varaibles contain the maxarea contours and name of its color so that the image color will be the one which has the max area color
            String colorOfMaxArea="";int maxArea=0;
            // loop over the 12 colors and get its contours to detect the color in the i-image
            for (int j = 0; j < COLOR_BOUNDS.length; j++) {
                Mat mask = new Mat(src.rows(), src.cols(), CvType.CV_8UC2);//create mask has src dimensions
                Core.inRange(dest, COLOR_BOUNDS[j][0], COLOR_BOUNDS[j][1], mask);//perform inrange to j range color
                List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
                Mat hierarchy = new Mat();
                Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
                int colorArea=0;
                // sum the area of the contours to determine the area of the  j color
                for (int contIdx = 0; contIdx < contours.size(); contIdx++) {
                    colorArea += (int) Imgproc.contourArea(contours.get(contIdx));
                }
                if(colorArea>maxArea){
                    colorOfMaxArea= COLORS_NAMES[j];
                    maxArea=colorArea;
                }
            }
            if(colorOfMaxArea!=""){
                colorsNames.add(colorOfMaxArea);
            }
            else{
                colorsNames.add("Black");
            }
        }
        this.colorsNames=colorsNames;
    }


    /**
     * checks whether the new contour will interfere with a previous accepted contour
     * @param existingContours the y-coordinate of the previous accepted contours
     * @param newContour the y-coordinate of the new contour to be tested
     * @param range the range in which contour should not be repeated
     * @return Boolean of whether there is a contour in the same range or not
     */
    private static Boolean repeatedContour(ArrayList<Double> existingContours, Double newContour, int range) {
        // looping the y-coordinate of the colors contours
        for (Double contour : existingContours) {
            // test if the y-coordinate of the new contour is between the y-coord. of the accepted contour+ range and - range, then return true
            if (newContour >= contour - range && newContour <= contour + range) {
                return true;
            }
        }
        return false;
    }

    /**
     * extract the images of the colors from the resistance image with colors contours drawn.
     * @param resistance the resistance image with colors contours drawn
     * @param countours TreeMap of key, the y-coordinate of the color and value, the contour of the color.
     * @return ArrayList of the images of the colors cropped
     */
    private static ArrayList<Mat> cropColors(Mat resistance, TreeMap<Double, MatOfPoint> countours) {
        // the returned colors ArrayList
        ArrayList<Mat> colors = new ArrayList<>();
        //looping the drawn contours in the resistance image
        for (Map.Entry<Double, MatOfPoint> contour : countours.entrySet()) {
            // create a black mask
            Mat mask=Mat.zeros(resistance.rows(),resistance.cols(),16);
            Core.inRange(resistance, new Scalar(0, 0, 0), new Scalar(0, 0, 0), mask);
            // get a rectangle from the drawn contour
            Rect r = Imgproc.boundingRect(contour.getValue());
            // draw the rectangle in the mask with white color
            Imgproc.rectangle(mask, r, new Scalar(255, 255, 255), -1);
            Mat foreground = new Mat(resistance.size(), CvType.CV_8UC3, new Scalar(0,0,0));
            List<Mat> bgr = new ArrayList<>();
            List<Mat> masked_img = new ArrayList<>();
            Core.split(resistance, bgr);
            Mat foreground_b = new Mat(resistance.size(), CvType.CV_8UC1, new Scalar(0,0,0));
            Mat foreground_g = new Mat(resistance.size(), CvType.CV_8UC1, new Scalar(0,0,0));
            Mat foreground_r = new Mat(resistance.size(), CvType.CV_8UC1, new Scalar(0,0,0));
            Core.bitwise_and(bgr.get(0),mask,foreground_b);
            Core.bitwise_and(bgr.get(1),mask,foreground_g);
            Core.bitwise_and(bgr.get(2),mask,foreground_r);
            masked_img.add(foreground_b);
            masked_img.add(foreground_g);
            masked_img.add(foreground_r);
            Core.merge(masked_img, foreground);
            // crop the color from the image
            mask = resistance.submat((int) r.y, (int) r.y+r.height, (int) (r.x), (int) (r.x+r.width));
            // add the color to colors ArrayList
            colors.add(mask);
        }
        //return colors ArrayList
        return colors;
    }

    /**
     * convert Mat image to Bitmap image
     * @param matImg the Mat image to be converted
     * @return Corresponding Bitmap image
     */
    public static Bitmap convertMatToBitmap(Mat matImg)
    {
        Bitmap bmp = null;
        bmp = Bitmap.createBitmap(matImg.cols(), matImg.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(matImg, bmp);
        return bmp;
    }

}
