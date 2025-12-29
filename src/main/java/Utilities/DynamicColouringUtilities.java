package utilities;

import javafx.scene.paint.Color;

public class DynamicColouringUtilities 
{
    private static final double SATURATION = 0.6;
    private static final double BRIGHTNESS = 0.9;
    private static final double OPACITY = 0.75;
    private static final double GOLDEN_RATIO = (1.0 + Math.sqrt(5)) / 2.0;             // The golden ratio (Φ)
    private static final double GOLDEN_ANGLE = 360.0 / (GOLDEN_RATIO * GOLDEN_RATIO);  // The golden angle (≈ 137.5°)
    private static final double INITIAL_HUE = Math.random() * 360.0;                                     // The initial hue
    
    public static Color generateNextColour(int n)
    {
        double nextHue = (INITIAL_HUE + (n * GOLDEN_ANGLE)) % 360;
        Color nextColor = Color.hsb(nextHue, SATURATION, BRIGHTNESS, OPACITY);
        return nextColor;
    }
}   
