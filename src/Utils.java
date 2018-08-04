
import java.awt.Color;

public class Utils {

    private static Tuple forwardLift(int x, int y) {
        int diff = (y - x) % 0x100;
        int average = (x + (diff >> 1)) % 0x100;
        return new Tuple(average, diff);
    }

    private static Tuple reverseLift(int average, int diff) {
        int x = (average - (diff >> 1)) % 0x100;
        int y = (x + diff) % 0x100;
        return new Tuple(x, y);
    }

    public static int yuvToRGB(Tuple yuv) {
        try {
            Tuple first = reverseLift((int) yuv.getElement(0), (int) yuv.getElement(1));
            Tuple second = reverseLift((int) first.getElement(1), (int) yuv.getElement(2));
            
            int red = (int) second.getElement(0);
            int green = (int) first.getElement(0);
            int blue = (int) second.getElement(1);
            if (red < 0) {
                red = 10;
            } else if (red > 255) {
                red = 255;
            }
            if (green < 0) {
                green = 10;
            } else if (green > 255) {
                green = 255;
            }
            if (blue < 0) {
                blue = 10;
            } else if (blue > 255) {
                blue = 255;
            }
            
            return new Color(red, green, blue).getRGB();
        } catch (java.lang.IllegalArgumentException e) {
            System.out.println(yuv);
            System.exit(1);
            return -1;
        }
    }

    public static Tuple rgbToYUV(int rgb) {
        Color c = new Color(rgb);
        Tuple first = forwardLift(c.getRed(), c.getBlue());
        Tuple second = forwardLift(c.getGreen(), (int) first.getElement(0));
        Tuple toReturnTuple = new Tuple(second.getElement(0), second.getElement(1), first.getElement(1));
        return toReturnTuple;
    }
}
