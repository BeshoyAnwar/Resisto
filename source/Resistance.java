import java.util.HashMap;
import java.util.Vector;

public class Resistance {
    /*//fail rate values
    HashMap<String,Double> FailRate = new HashMap<String, Double>() {
        {
            put("Brown", 1.0);
            put("Red" , 0.1);
            put("Orange",  0.01);
            put("Yellow",  0.001);
        }
    };*/
    /*band value and multplier value*/
    public HashMap<String,Integer> BandValueAndMultiplierValues = new HashMap<String, Integer>() {
        {
            put("Black" ,  0);
            put("Brown", 1);
            put("Red" , 2);  //"Orange","Yellow","Green","Blue","Violet","Grey"  ,"White",
            put("Orange",  3);
            put("Yellow",  4);
            put("Green", 5);
            put("Blue", 6);
            put("Violet",  7);
            put("Grey"  ,  8);
            put("White", 9);

            put("Gold", -1);
            put("Silver", -2);
        }
    };
    /* Temperature coefficient in parts-per-million per degree Kelvin. */
    public HashMap<String,Integer> TempCoeffValues = new HashMap<String, Integer>() {
        {
            put("Black" ,  250);
            put("Brown", 100);
            put("Red" , 50);  //"Orange","Yellow","Green","Blue","Violet","Grey"  ,"White",
            put("Orange",  15);
            put("Yellow",  25);
            put("Green", 20);
            put("Blue", 10);
            put("Violet",  5);
            put("Grey"  ,  1);
        }
    };
    /* value tolerance in +/- %. */
    public HashMap<String,Double> ToleranceValues = new HashMap<String, Double>() {
        {
            put("Brown",   1.0);
            put("Red" ,  2.0);
            put("Green", 0.5);
            put("Blue",    0.25);
            put("Violet",  0.1);
            put("Grey",  0.05);
            put("Gold",  5.0);
            put("Silver", 10.0);
            put("None",   20.0);
        }
    };
    private Vector<String> colours;
    private Double value;
    private Double tolerance;
    private Integer tempCoeff;
    //private Double FailRate ;
    //private Double Min ;
    //private Double Max ;


    public Resistance(Vector<String> colours) {
        this.colours = colours;
        this.runAlgorithm();
    }

    public Double getValue() {
        return value;
    }
    public Double getTolerance() {
        return tolerance;
    }
    public Integer getTempCoeff() {
        return tempCoeff;
    }
    public void setColours(Vector<String> colours) {
        this.colours = colours;
    }
    public Vector<String> getColours() {return colours; }

    private boolean tempCoeffExist(){
        if(colours.size()==6)
            return true ;
        else
            return false ;
    }
    public void printColours() {System.out.println(colours); }
    private void runAlgorithm() {
        switch (colours.size()) {
            case 3:
                value = calculateValue( 3 );
                tolerance = ToleranceValues.get("None") ;
                break;
            case 4:
                value = calculateValue( 3 );
                tolerance = ToleranceValues.get(colours.get(3)) ;
                break;
            case 5:
                value = calculateValue( 4 );
                tolerance = ToleranceValues.get(colours.get(4)) ;
                break;
            case 6:
                value = calculateValue( 4 );
                tolerance = ToleranceValues.get(colours.get(4)) ;
                tempCoeff = TempCoeffValues.get(colours.get(5));
                break;
            default:
                System.out.println("problem");
                break;
        }
        //to_string();
    }
    public String toString() {
        String value;
        if(this.value >1000000000)
        {
            value = String.valueOf(this.value /1000000000) ;
            if(value.charAt(value.length()-1)=='0')
                value = value.substring(0,value.length()-2)+" G";
            else
                value+=" G";
        }
        else if(this.value >1000000)
        {
            value = String.valueOf(this.value /1000000) ;
            if(value.charAt(value.length()-1)=='0')
                value = value.substring(0,value.length()-2)+" M";
            else
                value+=" M";
        }
        else if(this.value >1000)
        {
            value = String.valueOf(this.value /1000) ;
            if(value.charAt(value.length()-1)=='0')
                value = value.substring(0,value.length()-2)+" K";
            else
                value+=" K";
        }
        else
        {
            value = String.valueOf(this.value) ;
            if(value.charAt(value.length()-1)=='0')
                value = value.substring(0,value.length()-2);

        }
        value += "Ohms";
        return value;
    }
    private Double calculateValue(Integer Rotations ) {
        Double resistance = 0.0 ;
        for (int i=0;i<Rotations-1;i++)
        {
            resistance *= 10;
            resistance += BandValueAndMultiplierValues.get(colours.elementAt(i));
        }
        Integer MultplierPower = BandValueAndMultiplierValues.get(colours.elementAt(Rotations-1));
        switch(  MultplierPower )
        {
            case -1:
                return resistance / 10 ;
            case -2:
                return resistance /100  ;
            default :
                return resistance * Math.pow(10,MultplierPower) ;
        }
    }
}

