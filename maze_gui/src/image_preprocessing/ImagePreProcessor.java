package image_preprocessing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImagePreProcessor {

    private String fileName;
    private BufferedImage greyScaledImg;

    public ImagePreProcessor(String fPath) {
        fileName = fPath;
        greyScaledImg = greyScaleAndTrimImage(fPath);
    }

    public BufferedImage getProcessedImage() {
        return greyScaledImg;
    }

    private BufferedImage greyScaleAndTrimImage(String filePath) {
        try {
            File file = new File(filePath);
            System.out.println("Trying to read: " + file.getAbsolutePath());
            System.out.println("Exists? " + file.exists());

            BufferedImage colorImage = ImageIO.read(file);
            if (colorImage == null) {
                System.err.println("Image could not be read.");
                return null;
            }

            BufferedImage greyImage = new BufferedImage(
                colorImage.getWidth(), colorImage.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY
            );

            Graphics g = greyImage.getGraphics();
            g.drawImage(colorImage, 0, 0, null);
            g.dispose();

            // Trim the image and store result
            greyImage = trimImage(greyImage);

            // Save output
            File output = new File("Converted/GreyImage.png");
            output.getParentFile().mkdirs();
            ImageIO.write(greyImage, "png", output);

            return greyImage;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private BufferedImage trimImage(BufferedImage img) {
        int height = img.getHeight();
        int width = img.getWidth();

        int top = 0, bottom = height - 1, left = 0, right = width - 1;
        boolean found;

        // Top
        found = false;
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                if (new Color(img.getRGB(c, r)).getRed() == 0) {
                    top = r;
                    found = true;
                    break;
                }
            }
            if (found) break;
        }

        // Bottom
        found = false;
        for (int r = height - 1; r >= 0; r--) {
            for (int c = 0; c < width; c++) {
                if (new Color(img.getRGB(c, r)).getRed() == 0) {
                    bottom = r;
                    found = true;
                    break;
                }
            }
            if (found) break;
        }

        // Left
        found = false;
        for (int c = 0; c < width; c++) {
            for (int r = 0; r < height; r++) {
                if (new Color(img.getRGB(c, r)).getRed() == 0) {
                    left = c;
                    found = true;
                    break;
                }
            }
            if (found) break;
        }

        // Right
        found = false;
        for (int c = width - 1; c >= 0; c--) {
            for (int r = 0; r < height; r++) {
                if (new Color(img.getRGB(c, r)).getRed() == 0) {
                    right = c;
                    found = true;
                    break;
                }
            }
            if (found) break;
        }

        int newWidth = right - left + 1;
        int newHeight = bottom - top + 1;

        if (newWidth <= 0 || newHeight <= 0) {
            System.out.println("No black pixels found to trim.");
            return img;
        }

        return img.getSubimage(left, top, newWidth, newHeight);
    }
}
