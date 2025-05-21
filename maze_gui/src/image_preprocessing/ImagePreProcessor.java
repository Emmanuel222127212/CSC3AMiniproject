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
		
		
		fileName=fPath;
		greyScaledImg=GreyScaleImage(fPath);
		
	}
	
	public BufferedImage getProcessedImage() {
		return greyScaledImg;
	}
	
	private BufferedImage GreyScaleImage(String Filename) {

	    try {
	        File file = new File(Filename);
	        System.out.println("Trying to read: " + file.getAbsolutePath());
	        System.out.println("Exists? " + file.exists());

	        BufferedImage ColourImage = ImageIO.read(file);

	        BufferedImage GreyScale = new BufferedImage(ColourImage.getWidth(), ColourImage.getHeight(),
	                BufferedImage.TYPE_BYTE_GRAY);

	        Graphics DrawGrey = GreyScale.getGraphics();
	        DrawGrey.drawImage(ColourImage, 0, 0, null);
	        DrawGrey.dispose();
	        
	        BufferedImage trimmedImg = imgTrimming(GreyScale);
 
	        File output = new File("Converted/GreyImage.png");
	        output.getParentFile().mkdirs();
	        ImageIO.write(trimmedImg, "png", output);

	        return trimmedImg;

	    } catch (IOException e)
	    {
	        e.printStackTrace();
	    }
	    return null;
	}
	private BufferedImage imgTrimming(BufferedImage img) 
	{
		
	    int height = img.getHeight();
	    int width = img.getWidth();

	    int top = 0;
	    int bottom = height - 1;
	    int left = 0;
	    int right = width - 1;

	    boolean detectedWall = false;

	    // Top
	    for (int r = 0; r < height; r++)
	    {
	        for (int c = 0; c < width; c++) 
	        {
	            int rgbIntensity = new Color(img.getRGB(c, r)).getRed();  
	            
	            if (rgbIntensity == 0) {
	            	
	                top = r;
	                detectedWall = true;
	                
	                break;
	            }
	        }
	        if (detectedWall) 
	        	{
	        	break;
	        	}
	    }

	    detectedWall = false;

	    // Bottom
	    for (int r = height - 1; r >= 0; r--) 
	    {
	        for (int c = 0; c < width; c++) 
	        {
	            int rgbIntensity = new Color(img.getRGB(c, r)).getRed();
	            if (rgbIntensity == 0) {
	                bottom = r;
	                detectedWall = true;
	                break;
	            }
	        }
	        if (detectedWall) break;
	    }

	    detectedWall = false;

	    // Left
	    for (int c = 0; c < width; c++) 
	    {
	        for (int r = 0; r < height; r++) 
	        {
	            int rgbIntensity = new Color(img.getRGB(c, r)).getRed();
	            
	            if (rgbIntensity == 0) 
	            {
	                left = c;
	                detectedWall = true;
	                break;
	            }
	        }
	        if (detectedWall) {
	        	break;
	        }
	    }

	    detectedWall = false;

	    // Right
	    for (int c = width - 1; c >= 0; c--) 
	    {
	        for (int r = 0; r < height; r++) 
	        {
	            int rgbIntensity = new Color(img.getRGB(c, r)).getRed();
	            if (rgbIntensity == 0) {
	                right = c;
	                detectedWall = true;
	                break;
	            }
	        }
	        if (detectedWall) break;
	    }

	    // Calculate the trimmed width and height
	    int trimmedWidth = right - left + 1;
	    int trimmedHeight = bottom - top + 1;

	    // Validate the bounds
	    if (trimmedWidth <= 0 || trimmedHeight <= 0 ||
	        left < 0 || top < 0 ||
	        left + trimmedWidth > width || top + trimmedHeight > height) {
	        throw new IllegalArgumentException("Calculated trimming bounds are invalid.");
	    }

	    // Return trimmed image
	    return img.getSubimage(left, top, trimmedWidth, trimmedHeight);
	}
	 
}
