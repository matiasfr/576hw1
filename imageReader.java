import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import javax.imageio.ImageIO;


public class imageReader {

    public static int outputWidth = 0;
    public static int outputHeight = 0;
    public static boolean upScale = false;
    public static int resampleFormat = 0;
  
   public static void main(String[] args) 
   {

	String fileName = args[0];

   	int width = Integer.parseInt(args[1]);
	int height = Integer.parseInt(args[2]);
	resampleFormat = Integer.parseInt(args[3]);
	String outputFormat = args[4];

	if(outputFormat == "O1") {
        outputWidth = 1080;
        outputHeight = 1920;
    } else if(outputFormat == "O2") {
        outputWidth = 1280;
        outputHeight = 720;
    } else if(outputFormat == "O3") {
        outputWidth = 640;
        outputHeight = 480;
    }
    //see if we need to upscale or downscale
    if( outputWidth > width ) {
	    upScale = false;
    } else {
	    upScale = true;
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

    //now upscale or downscale according to resample param
    if (upScale == true) {
        if(resampleFormat == 1) {
            //Nearest neighbor to choose your up sampled pixel
        } else if {
            resampleFormat == 2 {
                //Bilinear/Cubic interpolation
           }
       } else {
            //resample format not inputted correctly
       }
   } else {

        
    }

    
    // Use a panel and label to display the image
    JPanel  panel = new JPanel ();
    panel.add (new JLabel (new ImageIcon (img)));
    
    JFrame frame = new JFrame("Display images");
    
    frame.getContentPane().add (panel);
    frame.pack();
    frame.setVisible(true);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

       try {

           //output created image
           File outputFile = new File("output.png");
           ImageIO.write(img, "png", outputFile); //todo: change this so that it uses the output image instead of the inputted img
       } catch(IOException e) {
           e.printStackTrace();
       }

   }

    public BufferedImage resampleImage(int width, int height, BufferedImage img) {

       return img;
    }


}