
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;

public class ImageCompression {
    
    // Lossless compress of the image.
    private static byte[] compressToBytes(BufferedImage img) {
        BitArray toReturn = new BitArray();
        
        // Add width and height (in this order).
        toReturn.add(BitArray.intToBitArray(img.getWidth()));
        toReturn.add(BitArray.intToBitArray(img.getHeight()));
        
        // Add all pixles, row by row.
        for (int i = 0; i < img.getHeight(); i++) {
            for (int j = 0; j < img.getWidth(); j++) {
                toReturn.add(BitArray.intToBitArray(img.getRGB(j, i)));
            }
        }
        
        LZWCodingProcessor huffmanCodingPocessor = new LZWCodingProcessor();
        huffmanCodingPocessor.addSamples(toReturn.toByteArray());
        
        return huffmanCodingPocessor.compress();
    }
    
    // Get YUV of the image.
    private static Tuple[][] getYUVImage(BufferedImage originalImage) {
        Tuple[][] yuvMat = new Tuple[originalImage.getHeight()][originalImage.getWidth()];
        for (int i = 0; i < originalImage.getHeight(); i++) {
            for (int j = 0; j < originalImage.getWidth(); j++) {
                yuvMat[i][j] = Utils.rgbToYUV(originalImage.getRGB(j, i)).clone();
            }
        }
        return yuvMat;
    }

    private static byte[] lossyCompress(BufferedImage originalImage) {
        BufferedImage toReturnImage;
        Tuple[][] yuvMat = getYUVImage(originalImage);
        ImageParts imageParts = ImageParts.createImageParts(yuvMat);
        
        // Lossy compress.
        for (int i = 0; i < imageParts.getImageParts().size(); i++) {
            for (int j = 0; j < imageParts.getImageParts().get(i).size(); j++) {
                double[][] appliedDCT = imageParts.getImageParts().get(i).get(j).applyDCT();
                double[][] reversedDCT;
                double[][] quanitized = imageParts.getImageParts().get(i).get(j).quanitize(appliedDCT);
                reversedDCT = imageParts.getImageParts().get(i).get(j).reverseDCT(quanitized);
                imageParts.getImageParts().get(i).get(j).setIntensities(reversedDCT);
            }
        }
        
        // Construct the new image.
        toReturnImage = new BufferedImage(yuvMat[0].length, yuvMat.length, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < toReturnImage.getHeight(); i++) {
            for (int j = 0; j < toReturnImage.getWidth(); j++) {
                int blockIIdx = (int) Math.floor((double) i / (double) 8);
                int blockJIdx = (int) Math.floor((double) j / (double) 8);
                ImagePart current = imageParts.getImageParts().get(blockIIdx).get(blockJIdx);
                int internalIIdx = i - (8 * blockIIdx);
                int internalJIdx = j - (8 * blockJIdx);
                Tuple currentColor = current.getColor(internalJIdx, internalIIdx);
                Color toSetColor = new Color(Utils.yuvToRGB(currentColor));
                toReturnImage.setRGB(j, i, toSetColor.getRGB());
            }
        }
        
        // Fix dark spots.
        for (int i = 0; i < originalImage.getHeight(); i++) {
            for (int j = 0; j < originalImage.getWidth(); j++) {
                int acceptedDifference = 30;
                Color oldC = new Color(originalImage.getRGB(j, i));
                Color newC = new Color(toReturnImage.getRGB(j, i));
                if (Math.abs(oldC.getRed() - newC.getRed()) > acceptedDifference) {
                    toReturnImage.setRGB(j, i, oldC.getRGB());
                } else if (Math.abs(oldC.getGreen()- newC.getGreen()) > acceptedDifference)  {
                    toReturnImage.setRGB(j, i, oldC.getRGB());
                } else if (Math.abs(oldC.getBlue()- newC.getBlue()) > acceptedDifference)  {
                    toReturnImage.setRGB(j, i, oldC.getRGB());
                }
            }
        }
        
        // Lossless compress.
        return compressToBytes(toReturnImage);
    }
    
    // Compress into bytes.
    public static byte[] compress(BufferedImage originalImage, boolean isLossy) {
        return isLossy ? lossyCompress(originalImage) : compressToBytes(originalImage);
    }
    
    // Decompress from bytes.
    public static BufferedImage decompress(byte[] bytes) {
        try {
            BufferedImage toReturn;
            
            LZWCodingProcessor codingProcessor = new LZWCodingProcessor();
            List<Byte> decompressed = codingProcessor.decompress(bytes);
            BitArray data = new BitArray(decompressed);
            Pair<Integer, Integer> widthData = BitArray.bitArrayToInt(data, 0);
            Pair<Integer, Integer> heightData = BitArray.bitArrayToInt(data, widthData.getValue());
            if (widthData.getKey() == 0 || heightData.getKey() == 0) {
                return new BufferedImage(0, 0, BufferedImage.TYPE_INT_RGB);
            }
            
            toReturn = new BufferedImage(widthData.getKey(), heightData.getKey(), BufferedImage.TYPE_INT_RGB);
            Pair<Integer, Integer> currentElement = BitArray.bitArrayToInt(data, heightData.getValue());
            toReturn.setRGB(0, 0, currentElement.getKey());
            for (int i = 0; i < heightData.getKey(); i++) {
                for (int j = 0; j < widthData.getKey(); j++) {
                    if (i == 0 && j == 0) {
                        continue;
                    }
                    currentElement = BitArray.bitArrayToInt(data, currentElement.getValue());
                    toReturn.setRGB(j, i, currentElement.getKey());
                }
            }
            
            return toReturn;
        } catch (Exception ex) {
            Logger.getLogger(ImageParts.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
