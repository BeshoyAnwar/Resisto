package com.example.pepmina.resisto;
import java.util.HashMap;
import java.util.Vector;

public class Resistance {
    
   /** band value and multplier value.
    * according to the colour code .
    */
    public HashMap<String,Integer> BandValueAndMultiplierValues = new HashMap<String, Integer>() {
        {
            put("Black" ,  0);
            put("Brown", 1);
            put("Red" , 2);  
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
   /** Temperature coefficient in parts-per-million per degree Kelvin.
    *  according to the colour code .
    */
    public HashMap<String,Integer> TempCoeffValues = new HashMap<String, Integer>() {
        {
            put("Black" ,  250);
            put("Brown", 100);
            put("Red" , 50);  
            put("Orange",  15);
            put("Yellow",  25);
            put("Green", 20);
            put("Blue", 10);
            put("Violet",  5);
            put("Grey"  ,  1);
        }
    };
   /** values of tolerance in +/- %.   
    *  according to the colour code .
    */
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
    //colours given to the algorthism.
    private Vector<String> colours;
    // value of the resistance.
    private Double value;
    //tolerance of the resistance.
    private Double tolerance;
    //temperature coeffent of the resistance.
    private Integer tempCoeff;
    
    /**
     * Resistance constructor that set the colours and runs the algorthsim to get the value of resistance .
     * @param colours: The input colours for the algorthism .
     * 
     */
    public Resistance(Vector<String> colours) {
        this.colours = colours;
        this.runAlgorithm();
    }
    /**
     * getter function that return the value of the resistance 
     * according to the colours given and the colour code.
     * @return value
     */
    public Double getValue() {
        return value;
    }
    /**
     * getter function that return the value of the tolerance
     * according to the colours given and the colour code.
     * @return tolerance
     */
    public Double getTolerance() {
        return tolerance;
    }
    /**
     * getter function that return the value of the temperature coeffent 
     * according to the colours given and the colour code. 
     * @return value
     */
    public Integer getTempCoeff() {
        return tempCoeff;
    }
    /**
     * setter function that sets the colours 
     * but you must call the algorthism function to get the resistance,tolerance and temperature coeffent value .
     * @param colours : colours in the object
     */
    public void setColours(Vector<String> colours) {
        this.colours = colours;
    }
    /**
     * getter function that return the value of the colours stored in it 
     *  
     * @return colours
     */
    public Vector<String> getColours() {return colours; }
    /**
     * function used to check whether there is a temperature coeffent or not for this resistance
     * 
     * @return flag
     */
    private boolean tempCoeffExist(){
        if(colours.size()==6)
            return true ;
        else
            return false ;
    }
    /**
     * function used to print colours in the object
     */
    public void printColours() {System.out.println(colours); }
    /**
     * The runAlgorithm function is the main Algorithm for the colour code.
     * it has helper functions like "calculateValue" and "toString".
     * it separates the reistance to types 
     * and calculate resistance , tolerance and temperature coeff. 
     */
    private void runAlgorithm() {
        /*
        this switch is used to identify the type of resistance according to the number of bands 
        and according to that it calculates resistance value , tolerance and
        temperature coeffent "if it exists"
        */
        switch (colours.size()) {
            case 3://3 bands
                value = calculateValue( 3 );//resistance calculation as 2 band values and a multplier value
                tolerance = ToleranceValues.get("None") ;//calculate the tolerance
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
                tempCoeff = TempCoeffValues.get(colours.get(5));//calculate the temperature coeff.
                break;
            default:
                System.out.println("problem");
                break;
        }
        
    }
    /**
     * The toString function has the job of  calculating the value of the resistance with units and return it as string :
     * G :Giga.
     * M :Mega.
     * K :Kilo.
     * @return value of the  resistance as string with it's unit.
     */
    public String toString() {
        String value;
        /*
        this if condition is used to categorize the limits in each resistance value"lower limit"
        */
        if(this.value >1000000000)
        {
            value = String.valueOf(this.value /1000000000) ;//divide to add the unit
            if(value.charAt(value.length()-1)=='0')
                value = value.substring(0,value.length()-2)+" G";// add unit to string
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
    /**
     * The calculateValue function has the job of calculating the value of the resistance according to the colour code :
     * @param Rotations : it is the number of digits in the resistance used by the colour code
     * @return resistance value .
     */
    private Double calculateValue(Integer Rotations ) {
        Double resistance = 0.0 ;
        /*
        this is the most important part of the algorthism it calculates the band values together 
        so that they can be multipled by the multplier
        it multplies the value with 0 and then adds the value to the units.
        */
        for (int i=0;i<Rotations-1;i++)
        {
            resistance *= 10;
            resistance += BandValueAndMultiplierValues.get(colours.elementAt(i));
        }
        Integer MultplierPower = BandValueAndMultiplierValues.get(colours.elementAt(Rotations-1));//multplier
        //this is a very important part
        /*
        because of the fact that dividing a double by a double gives a wrong value in some cases i had to use a special case 
        for the division by 10 and by 100 .
        in the rest of the cases it is just multplication
        */
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
