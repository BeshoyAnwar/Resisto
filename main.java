import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.HashMap;

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
            { new Scalar(0, 100, 100),new Scalar(9, 255, 255)},// red  
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
	}
}