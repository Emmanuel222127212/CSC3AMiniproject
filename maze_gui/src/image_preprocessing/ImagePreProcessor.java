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

			imgTrimming(GreyScale);
			Graphics DrawGrey = GreyScale.getGraphics();

			DrawGrey.drawImage(ColourImage, 0, 0, null);

			DrawGrey.dispose();

			File output = new File("Converted/GreyImage.png");
			output.getParentFile().mkdirs();
			
			
			ImageIO.write(GreyScale, "png", output);

			
			return GreyScale;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	private void imgTrimming(BufferedImage img) {
		int height=img.getHeight();
		int width = img.getWidth();
		
		int top=0;
		int bottom=height-1;
		int left = 0;
		int right=width-1;
		boolean DetectedWall=false;
		//top
		for (int r = 0; r < height-1; r++) {
			for(int c=0;c<width-1;c++) {
				
				if(DetectedWall) {
					DetectedWall=false;
					break;
				}
				int rgbIntensity=new Color(img.getRGB(r, c)).getRed(); //image greyscaled so this just gets intensity
				
				//wall found
				if(rgbIntensity==0) {
					top=r;
					DetectedWall=true;
					break;
				}
			}
			
		}
		//bottom
		for (int r = height-1; r>0; r--) {
			for(int c=0;c<width-1;c++) {
				
				if(DetectedWall) {
					DetectedWall=false;
					break;
				}
				
				int rgbIntensity=new Color(img.getRGB(r, c)).getRed(); //image greyscaled so this just gets intensity
				
				//wall found
				if(rgbIntensity==0) {
					bottom=r;
					DetectedWall=true;
					break;
					
				}
			}
			
		}
		
		//left
		for(int c=0;c<width-1;c++) {
			for (int r = 0; r < height-1; r++) {
				if(DetectedWall) {
					DetectedWall=false;
					break;
				}
				
				int rgbIntensity=new Color(img.getRGB(r, c)).getRed(); //image greyscaled so this just gets intensity
				
				//wall found
				if(rgbIntensity==0) {
					left=c;
					DetectedWall=true;
					break;
				}
			}
		}
		
		//Right
		for(int c=width-1;c>0;c--) {
			for (int r = 0; r < height-1; r++) {
				if(DetectedWall) {
					DetectedWall=false;
					break;
				}
				
				int rgbIntensity=new Color(img.getRGB(r, c)).getRed(); //image greyscaled so this just gets intensity
				
				//wall found
				if(rgbIntensity==0) {
					right=c;
					DetectedWall=true;
					break;
				}
			}
		}
		
		img=img.getSubimage(left, top, right, bottom);
		
		
	}
	
	
	 
}
