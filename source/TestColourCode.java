import java.util.*;
import java.util.Random;

public class TestColourCode {
    public String getRandomElement(List<String> list)
    {
        Random rand = new Random();
        return list.get(rand.nextInt(list.size()));
    }
    public static void main(String args[])
    {











        TestColourCode obj = new TestColourCode();//class object
        Vector<String> vector = new Vector<String>(6);//vector to be put colours in
        List<String> BandAndMultb = new ArrayList<>(Arrays.asList("Black", "Brown", "Red","Orange","Yellow","Green","Blue","Violet","Grey"  ,"White"));
        // Add bands
        vector.add(obj.getRandomElement(BandAndMultb ));//band 1
        vector.add(obj.getRandomElement(BandAndMultb ));//band 2
        vector.add(obj.getRandomElement(BandAndMultb ));//band 3
        //add multplier
        BandAndMultb.add("Gold");
        BandAndMultb.add("Silver");
        vector.add(obj.getRandomElement(BandAndMultb ));//multplier
        //add tolerance
        List<String> tolerance = new ArrayList<>(Arrays.asList( "Brown", "Red","Green","Blue","Violet","Grey" ,"Gold","Silver","None" ));
        vector.add(obj.getRandomElement(tolerance ));//tolerance
        List<String> tempcoeff = new ArrayList<>(Arrays.asList("Black", "Brown", "Red","Orange","Yellow","Green","Blue","Violet","Grey"  ));
        vector.add(obj.getRandomElement(tempcoeff ));//add tempcoeff
        //initalizing ColourCode valuable
        ColourCode test = new ColourCode (vector) ;

        System.out.println("                          DYNAMINC RAND CASE ");
        System.out.println("colours are :  ");
        test.PrintColours();//given colours
        test.RunAlgorthism();
        System.out.println("answer is  : " );
        System.out.println("Resistance = "+ test.getResistance()+ " Ohms"+"  "+ test.getResistanceAsString());
        System.out.println("Tolerence =  "+ test.getTolerance() + "%"  );
        if(test.TempCoeffExist())
           System.out.println("TempCoeff =  "+ test.getTempCoeff() + " ppm/k");



        Vector<String> vector2 = new Vector<String>(6);//vector to be put colours in
        vector2.add("Orange");//band 1
        vector2.add("Red");//band 2
        vector2.add("Brown");//band 3
        vector2.add("Green");//mult
        vector2.add("Brown");//tolerance
        vector2.add("Red");//temp coeff
        ColourCode test2 = new ColourCode (vector2) ;
        System.out.println("                                  STATIC CASE ");
        System.out.println("colours are :  ");
        test2.PrintColours();//given colours
        test2.RunAlgorthism();
        System.out.println("answer is  : " );
        System.out.println("Resistance = "+ test2.getResistance()+ " Ohms"+"  "+ test2.getResistanceAsString());
        System.out.println("Tolerence =  "+ test2.getTolerance() + "%"  );
        if(test2.TempCoeffExist())
            System.out.println("TempCoeff =  "+ test2.getTempCoeff() + " ppm/k");




    }
}
