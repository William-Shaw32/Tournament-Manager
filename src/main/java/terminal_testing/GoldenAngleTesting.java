package terminal_testing;

/**
 * This class can be used to test creating a dynamic sequence/set of hues using the golden angle algorithm
 * @author William Shaw
 */
public class GoldenAngleTesting 
{
    private static final double GOLDEN_RATIO = (1.0 + Math.sqrt(5)) / 2.0;             // The golden ratio (Φ)
    private static final double GOLDEN_ANGLE = 360.0 / (GOLDEN_RATIO * GOLDEN_RATIO);  // The golden angle (≈ 137.5°)
    private static final int NUM_COLOURS = 20;                                         // The number of colours in the set/sequence                                  
    private static final double INITIAL_HUE = 0.0;                                     // The initial hue (0.0 = red)
    private static double[] hues = new double[NUM_COLOURS];                            // Array of hues
    
    /**
     * Java main method
     * Computes the hues in the sequence/set
     * @param args Command line arguments
     */
    public static void main(String[] args)
    {
        hues[0] = INITIAL_HUE;
        for(int i = 1; i < NUM_COLOURS; i++)
        {
            hues[i] = (hues[i-1] + GOLDEN_ANGLE) % 360;
        }
        printHues();
    }

    /**
     * Prints the hues to the terminal
     */
    private static void printHues()
    {
        for(int i = 0; i < NUM_COLOURS; i++)
        {
            System.out.println("Hue " + i + ": " + hues[i] + "°");
        }
    }
}
