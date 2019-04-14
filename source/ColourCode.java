import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class ColourCode {
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
    /* Resistance tolerance in +/- %. */
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
    private Vector<String> Colours ;
    private Double Resistance ;
    private String ResistanceAsString ;
    private Double Tolerance  ;
    private Integer TempCoeff ;
    //private Double FailRate ;
    //private Double Min ;
    //private Double Max ;


    public ColourCode(Vector<String> colours) {
        Colours = colours;
    }
    public ColourCode() {
    }

    public String getResistanceAsString() { return ResistanceAsString; }
    public Double getResistance() {
        return Resistance;
    }
    public Double getTolerance() {
        return Tolerance;
    }
    public Integer getTempCoeff() {
        return TempCoeff;
    }
    public void setColours(Vector<String> colours) {
        Colours = colours;
    }
    public Vector<String> getColours() {return Colours; }

    public boolean TempCoeffExist(){
    if(Colours.size()==6)
        return true ;
    else
        return false ;
    }
    public void PrintColours () {System.out.println(Colours); }
    public void RunAlgorthism () {
        switch (Colours.size()) {
            case 3:
                Resistance = CalculateValue( 3 );
                Tolerance  = ToleranceValues.get("None") ;
                break;
            case 4:
                Resistance = CalculateValue( 3 );
                Tolerance  = ToleranceValues.get(Colours.get(3)) ;
                break;
            case 5:
                Resistance = CalculateValue( 4 );
                Tolerance  = ToleranceValues.get(Colours.get(4)) ;
                break;
            case 6:
                Resistance = CalculateValue( 4 );
                Tolerance  = ToleranceValues.get(Colours.get(4)) ;
                TempCoeff = TempCoeffValues.get(Colours.get(5));
                break;
            default:
                System.out.println("problem");
                break;
        }
        CalculateRestanceString();
    }
    private void CalculateRestanceString() {
        if(Resistance>1000000000)
        {
            ResistanceAsString = String.valueOf(Resistance/1000000000) ;
            if(ResistanceAsString.charAt(ResistanceAsString.length()-1)=='0')
                ResistanceAsString = ResistanceAsString.substring(0,ResistanceAsString.length()-2)+'G';
            else
                ResistanceAsString+='G';
        }
        else if(Resistance>1000000)
        {
            ResistanceAsString = String.valueOf(Resistance/1000000) ;
            if(ResistanceAsString.charAt(ResistanceAsString.length()-1)=='0')
                ResistanceAsString = ResistanceAsString.substring(0,ResistanceAsString.length()-2)+'M';
            else
                ResistanceAsString+='M';
        }
        else if(Resistance>1000)
        {
            ResistanceAsString = String.valueOf(Resistance/1000) ;
            if(ResistanceAsString.charAt(ResistanceAsString.length()-1)=='0')
                ResistanceAsString = ResistanceAsString.substring(0,ResistanceAsString.length()-2)+'K';
            else
                ResistanceAsString+='K';
        }
        else
        {
            ResistanceAsString = String.valueOf(Resistance) ;
            if(ResistanceAsString.charAt(ResistanceAsString.length()-1)=='0')
                ResistanceAsString = ResistanceAsString.substring(0,ResistanceAsString.length()-2);

        }
        ResistanceAsString += " Ohms";
    }
    private Double CalculateValue( Integer Rotations ) {
        Double resistance = 0.0 ;
        for (int i=0;i<Rotations-1;i++)
        {
            resistance *= 10;
            resistance += BandValueAndMultiplierValues.get(Colours.elementAt(i));
        }
        //System.out.println(resistance);
        //System.out.println((  BandValueAndMultiplierValues.get(Colours.elementAt(Rotations-1))));
        //System.out.println(resistance *( Math.pow(10, BandValueAndMultiplierValues.get(Colours.elementAt(Rotations-1)))));
        Integer MultplierPower = BandValueAndMultiplierValues.get(Colours.elementAt(Rotations-1));
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

