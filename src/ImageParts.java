
import java.util.ArrayList;
import java.util.List;

public class ImageParts {

    private final List<List<ImagePart>> imageParts;

    private ImageParts(List<List<ImagePart>> imageParts) {
        this.imageParts = imageParts;
    }

    public static ImageParts createImageParts(Tuple[][] yuvMat) {
        // Construct "8x8" blocks.
        List<List<ImagePart>> imageParts = new ArrayList<>();
        for (int i = 0; i < yuvMat.length; i += 8) {
            List<ImagePart> imagePartsWidth = new ArrayList<>();
            for (int j = 0; j < yuvMat[0].length; j += 8) {
                List<List<Tuple>> imagePartMat = new ArrayList<>();
                for (int k = i; k < (i + 8); k++) {
                    List<Tuple> imagePartWidth = new ArrayList<>();
                    for (int z = j; z < (j + 8); z++) {
                        try {
                            imagePartWidth.add(yuvMat[k][z]);
                        } catch (IndexOutOfBoundsException ex) {
                            imagePartWidth.add(new Tuple(0, 0, 0));
                        }
                    }
                    if (imagePartWidth.size() > 0) {
                        imagePartMat.add(imagePartWidth);
                    }
                }
                if (imagePartMat.size() > 0) {
                    imagePartsWidth.add(new ImagePart(imagePartMat));
                }
            }
            imageParts.add(imagePartsWidth);
        }

        return new ImageParts(imageParts);
    }
    
    public List<List<ImagePart>> getImageParts() {
        return imageParts;
    }

    public ImagePart getImagePart(int x, int y) {
        return imageParts.get(y).get(x);
    }

    public void addIntensity(int num) {
        for (int i = 0; i < imageParts.size(); i++) {
            for (int j = 0; j < imageParts.get(0).size(); j++) {
                imageParts.get(i).get(j).addIntensity(num);
            }
        }
    }

    @Override
    public String toString() {
        String output = "ImageParts:[\n";
        for (int i = 0; i < imageParts.size(); i++) {
            if (i > 0) {
                output += "\n\n";
            }
            output += "Level(" + i + ")[\n";
            for (int j = 0; j < imageParts.get(i).size(); j++) {
                if (j > 0) {
                    output += ",\n";
                }
                output += imageParts.get(i).get(j).toString();
            }
            output += "\n]\n";
        }
        output += "]";
        return output;
    }
}
