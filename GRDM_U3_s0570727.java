import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
     Opens an image window and adds a panel below the image
 */
public class GRDM_U3_s0570727 implements PlugIn {

	ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;

	String[] items = {"Original", "Rot-Kanal", "Negativ", "Graustufen", "Binär", "Binär 5-Graustufen", "Binär 10-Graustufen" ,"Fehlerdiffusion", "Sepia", "6-Color"};


	public static void main(String args[]) {

//		IJ.open("/users/barthel/applications/ImageJ/_images/orchid.jpg");
		

		IJ.open("/Users/kenneth/git/uebung1_gdm/Bear.jpg");
		//IJ.open("Z:/Pictures/Beispielbilder/orchid.jpg");

		GRDM_U3_s0570727 pw = new GRDM_U3_s0570727();
		pw.imp = IJ.getImage();
		pw.run("");
	}

	public void run(String arg) {
		if (imp==null) 
			imp = WindowManager.getCurrentImage();
		if (imp==null) {
			return;
		}
		CustomCanvas cc = new CustomCanvas(imp);

		storePixelValues(imp.getProcessor());

		new CustomWindow(imp, cc);
	}


	private void storePixelValues(ImageProcessor ip) {
		width = ip.getWidth();
		height = ip.getHeight();

		origPixels = ((int []) ip.getPixels()).clone();
	}


	class CustomCanvas extends ImageCanvas {

		CustomCanvas(ImagePlus imp) {
			super(imp);
		}

	} // CustomCanvas inner class


	class CustomWindow extends ImageWindow implements ItemListener {

		private String method;
		
		CustomWindow(ImagePlus imp, ImageCanvas ic) {
			super(imp, ic);
			addPanel();
		}

		void addPanel() {
			//JPanel panel = new JPanel();
			Panel panel = new Panel();

			JComboBox cb = new JComboBox(items);
			panel.add(cb);
			cb.addItemListener(this);

			add(panel);
			pack();
		}

		public void itemStateChanged(ItemEvent evt) {

			// Get the affected item
			Object item = evt.getItem();

			if (evt.getStateChange() == ItemEvent.SELECTED) {
				System.out.println("Selected: " + item.toString());
				method = item.toString();
				changePixelValues(imp.getProcessor());
				imp.updateAndDraw();
			} 

		}

		private int calcGraustufe(int binNumber, int rgb_value) 
		{
			/*
			 * n = Anzahl abschnitte (bins)
			 * binSize = (int) (255 / n)
			 * v = 200
			 * q = (255 / n - 1) * (v / 255/n)
			 */
			int binSize = (int) (255 / binNumber);
			
			return (255 / (binNumber - 1)) * (rgb_value / binSize);
		}

		private void changePixelValues(ImageProcessor ip) {

			// Array zum Zurückschreiben der Pixelwerte
			int[] pixels = (int[])ip.getPixels();

			if (method.equals("Original")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						
						pixels[pos] = origPixels[pos];
					}
				}
			}
			
			if (method.equals("Rot-Kanal")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						//int g = (argb >>  8) & 0xff;
						//int b =  argb        & 0xff;

						int rn = r;
						int gn = 0;
						int bn = 0;

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
			
			if (method.equals("Negativ")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;

						int rn = 255 - r;
						int gn = 255 - g;
						int bn = 255 - b;

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
			
			if (method.equals("Graustufen")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;

						int durchschnitt = (r + g + b) / 3;
						
						int rn = durchschnitt;
						int gn = durchschnitt;
						int bn = durchschnitt;

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
			
			if (method.equals("Binär")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;

						int durchschnitt = (r + g + b) / 3;
						
//						if (durchschnitt <= 128) durchschnitt = 0;
//						else durchschnitt = 255;
						
						int rgb = calcGraustufe(2, durchschnitt);
						
						int rn = rgb;
						int gn = rgb;
						int bn = rgb;

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
			
			if (method.equals("Binär 5-Graustufen")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;

						int durchschnitt = (r + g + b) / 3;
						
//						if (durchschnitt <= 128) durchschnitt = 0;
//						else durchschnitt = 255;
						
						int rgb = calcGraustufe(5, durchschnitt);
						
						int rn = rgb;
						int gn = rgb;
						int bn = rgb;

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
			
			if (method.equals("Binär 10-Graustufen")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;

						int durchschnitt = (r + g + b) / 3;
						
//						if (durchschnitt <= 128) durchschnitt = 0;
//						else durchschnitt = 255;
						
						int rgb = calcGraustufe(10, durchschnitt);
						
						int rn = rgb;
						int gn = rgb;
						int bn = rgb;

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
			
			if (method.equals("Fehlerdiffusion")) {

				int previous;
				
				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;

						int durchschnitt = (r + g + b) / 3;
						
						int diff = 0;
						
						if (durchschnitt <= 128) diff = durchschnitt;
						else if (durchschnitt > 128) durchschnitt = durchschnitt - 255;
						
						previous = diff;
						
						if (durchschnitt + previous <= 128) durchschnitt = 0;
						else if (durchschnitt + previous > 128) durchschnitt = 255;
						
						int rn = durchschnitt;
						int gn = durchschnitt;
						int bn = durchschnitt;

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
			
			if (method.equals("Sepia")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;

						int durchschnitt = (r + g + b) / 3;
						
						int rn = (int) (durchschnitt * 0.8);
						int gn = (int) (durchschnitt * 0.55);
						int bn = (int) (durchschnitt * 0.3);

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
			
			if (method.equals("6-Color")) {
				/*
				 * fast-schwarz: r = 35, r = 37; b = 36 | m = 36 | array 0-2
				 * dunkelbraun: r = 100, g = 90, b = 82 | m = 91 | array 3-5
				 * dunkelblau: r = 60, g = 99, b = 134 | m = 98 | array 6-8
				 * hellblau: r = 110, g = 133, b = 159 | m = 134 | array 9-11
				 * hellbraun: r = 156, g = 144, b = 135 | m = 145 | array 12-14
				 * beige: r = 209, g = 207, b = 208 | m = 208 | array 15- 18
				 */
				int[] colors = {35, 37, 36, 100, 90, 82, 60, 99, 134, 110, 133, 159, 156, 144, 135, 209, 207, 208};
				
				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;
						
//						int durchschnitt = (r + g + b) / 3;  
						int rn = 0, gn = 0, bn = 0;
						
						if (r <= 41) {
							rn = 35; // fast-schwarz
							gn = 37;
							bn = 36;
						}
						else if(((r>41 && r<= 62)&& g>80)||((r>60 && r<= 100)&& b>100)){
							rn = 60; // dunkelblau
							gn = 99;
							bn = 134;
						}
						else if((r>41 && r<= 62)&& g<80){
							rn = 35; //fast-schwarz
							gn = 37;
							bn = 36;
						}
						
						else if(r>62 && r<=94){
							rn = 110; //hellblau
							gn = 133;
							bn = 159;
						}
						else if(r>94 && r<=134){
							rn = 100; //dunkelbraun
							gn = 90;
							bn = 82;
						}
						else if(r>134 && r<=180){
							rn = 156; //hellbraun
							gn = 144;
							bn = 135;
						}
						else if(r>180 && r<=256){
							rn = 209; //beige
							gn = 207;
							bn = 208;
						}
						

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
			
		}


	} // CustomWindow inner class
} 
