import jdk.nashorn.internal.runtime.logging.DebugLogger;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.nio.Buffer;
import javax.swing.*;
import javax.imageio.ImageIO;


public class imageReader {


  
   public static void main(String[] args) 
   {

	String fileName = args[0];

   	int width = Integer.parseInt(args[1]);
	int height = Integer.parseInt(args[2]);
	int resampleFormat = Integer.parseInt(args[3]);
	String outputFormat = args[4];

	int outputWidth = 0;
	int outputHeight = 0;

	if(outputFormat.equals("O1")) {
        outputWidth = 1920;
        outputHeight = 1080;
    } else if(outputFormat.equals("O2")) {
        outputWidth = 1280;
        outputHeight = 720;
    } else if(outputFormat.equals("O3") ) {
        outputWidth = 640;
        outputHeight = 480;
    }

    boolean upScale = false;
    //see if we need to upscale or downscale
    if( outputWidth > width ) {
	    upScale = true;
    } else {
	    upScale = false;
    }
	
    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    try {
	    File file = new File(args[0]);
	    InputStream is = new FileInputStream(file);

	    long len = file.length();
	    byte[] bytes = new byte[(int)len];
	    
	    int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
    		
    	int ind = 0;
		for(int y = 0; y < height; y++){
	
			for(int x = 0; x < width; x++){
		 
				byte a = 0;
				byte r = bytes[ind];
				byte g = bytes[ind+height*width];
				byte b = bytes[ind+height*width*2]; 
				
				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
				img.setRGB(x,y,pix);
				ind++;
			}
		}
		
		
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

       BufferedImage outputImage;

    if (upScale == true) { //upscale
        if(resampleFormat == 1) {
            //Nearest neighbor to choose your up sampled pixel
            outputImage = upsampleNearestNeighbor(img, outputWidth, outputHeight);

        } else if (resampleFormat == 2) {
                //Bilinear/Cubic interpolation
            outputImage = upsampleCubic(img, outputWidth, outputHeight);
        } else {
            //resample format not inputted correctly, return same image
            outputImage = img;
       }
   } else { //downscale
        if(resampleFormat == 1) {
            //Specific/Random sampling where you choose a specific pixel
            outputImage = downsampleSpecific(img, outputWidth, outputHeight);
        } else if (resampleFormat == 2 ){
                //Gaussian smoothing where you choose the average of a set of sample
            outputImage = downsampleGaussian(img, outputWidth, outputHeight);
        } else {
            //resample format incorrect, return same iamge
            outputImage = img;

        }
    }


    // Use a panel and label to display the image
    JPanel  panel = new JPanel ();
    panel.add (new JLabel (new ImageIcon (outputImage)));
    
    JFrame frame = new JFrame("Display images");
    
    frame.getContentPane().add (panel);
    frame.pack();
    frame.setVisible(true);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

       try {

           //output created image
           File outputFile = new File("output.png");
           ImageIO.write(outputImage, "png", outputFile); //todo: change this so that it uses the output image instead of the inputted img
       } catch(IOException e) {
           e.printStackTrace();
       }

   }

    public static BufferedImage downsampleSpecific(BufferedImage inputImg, int outputWidth, int outputHeight) {
        int inputHeight = inputImg.getHeight(), inputWidth = inputImg.getWidth();
        BufferedImage newImg = new BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_INT_RGB);
        float x_ratio = ((float)inputWidth)/outputWidth;
        float y_ratio = ((float)inputHeight)/outputHeight;
        for(int x=0; x<outputWidth; x++) {
            for(int y=0;y<outputHeight;y++) {

                newImg.setRGB(x,y,inputImg.getRGB((int)(x*x_ratio),(int)(y*y_ratio)) );
            }
        }

       return newImg;
    }
    public static int getNeighborColorAverage(BufferedImage img, int x, int y) {
        int redCount = 0;
        int greenCount = 0;
        int blueCount = 0;

        Color center = new Color(img.getRGB(x, y));
        redCount += center.getRed();
        greenCount += center.getGreen();
        blueCount += center.getBlue();

        //get up
        Color up = new Color(img.getRGB(x, y));
        if (y > 0) {
            up = new Color(img.getRGB(x, y-1));
        }
        redCount += up.getRed();
        greenCount += up.getGreen();
        blueCount += up.getBlue();

        //get down
        Color down = new Color(img.getRGB(x, y));
        if (y+1 < img.getHeight()) {
            down = new Color(img.getRGB(x, y+1));
        }
        redCount += down.getRed();
        greenCount += down.getGreen();
        blueCount += down.getBlue();

        //get left
        Color left = new Color(img.getRGB(x, y));
        if (x > 0) {
            left = new Color(img.getRGB(x-1, y));
        }
        redCount += left.getRed();
        greenCount += left.getGreen();
        blueCount += left.getBlue();

        //get right
        Color right = new Color(img.getRGB(x, y));
        if (x+1 < img.getWidth()) {
            right = new Color(img.getRGB(x+1, y));
        }
        redCount += right.getRed();
        greenCount += right.getGreen();
        blueCount += right.getBlue();

        Color avg = new Color(redCount/5,greenCount/5,blueCount/5);
        return avg.getRGB();
    }

    public static BufferedImage downsampleGaussian(BufferedImage inputImg, int outputWidth, int outputHeight) {
        int inputHeight = inputImg.getHeight(), inputWidth = inputImg.getWidth();
        BufferedImage newImg = new BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_INT_RGB);

        float x_ratio = ((float)inputWidth)/outputWidth;
        float y_ratio = ((float)inputHeight)/outputHeight;

        for(int x=0; x<outputWidth; x++) {
            for(int y=0;y<outputHeight;y++) {

                int avgColor =  getNeighborColorAverage(inputImg, (int)(x*x_ratio),(int)(y*y_ratio) );

                //after we've avergaed the colors, scale the result to the proper place
                newImg.setRGB(x,y,avgColor);

            }
        }

        return newImg;
    }

    public static BufferedImage upsampleNearestNeighbor(BufferedImage inputImg, int outputWidth, int outputHeight) {
       int inputHeight = inputImg.getHeight(), inputWidth = inputImg.getWidth();
        BufferedImage newImg = new BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_INT_RGB);
        float x_ratio = ((float)inputWidth)/outputWidth;
        float y_ratio = ((float)inputHeight)/outputHeight;

        for(int x=0; x<outputWidth; x++) {
            for(int y=0;y<outputHeight;y++) {
                newImg.setRGB(x,y,inputImg.getRGB((int)(x*x_ratio),(int)(y*y_ratio)));
            }
        }
        return newImg;
    }

    public static BufferedImage upsampleCubic(BufferedImage inputImg, int outputWidth, int outputHeight) {
        int inputHeight = inputImg.getHeight(), inputWidth = inputImg.getWidth();

        BufferedImage newImg = new BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_INT_RGB);
        float x_ratio = ((float)inputWidth-1)/outputWidth;
        float y_ratio = ((float)inputHeight-1)/outputHeight;

        int A, B, C, D, x, y, index, gray ;


        float x_diff, y_diff, ya, yb ;
        int offset = 0 ;
        for (int i=0;i<outputHeight;i++) {
            for (int j = 0; j < outputWidth; j++) {
                x = (int) (x_ratio * j);
                y = (int) (y_ratio * i);
                x_diff = (x_ratio * j) - x;
                y_diff = (y_ratio * i) - y;
                index = y * inputWidth + x;

                // range is 0 to 255 thus bitwise AND with 0xff
                A = inputImg.getRGB(x, y) & 0xff;
                B = inputImg.getRGB(x + 1, y) & 0xff;
                C = inputImg.getRGB(x, y + 1) & 0xff;
                D = inputImg.getRGB(x + 1, y + 1) & 0xff;

                // Y = A(1-w)(1-h) + B(w)(1-h) + C(h)(1-w) + Dwh
                gray = (int) (
                        A * (1 - x_diff) * (1 - y_diff) + B * (x_diff) * (1 - y_diff) +
                                C * (y_diff) * (1 - x_diff) + D * (x_diff * y_diff)
                );

                newImg.setRGB(x,y,gray);
            }
        }

        return newImg;
    }


}