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

				
				int rgb;
				
				for (int y=0; y<height; y++) {
					int previous = 0;
					
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;

						int durchschnitt = (r + g + b) / 3 + previous;
						
						if (durchschnitt <= 128) 
						{
							previous = durchschnitt - 0;
							rgb = 0;
						}
						else 
						{
							previous = durchschnitt - 255;
							rgb = 255;
						}
						
						int rn = rgb;
						int gn = rgb;
						int bn = rgb;

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
				 * fast weiß: r = 208, r = 206; b = 206 | array 0-1 | colors[0] | colors[1]
				 * grau: r = 151, g = 149, b = 149 | array 2-3 | colors[2] | colors[3]
				 * blau: r = 52, g = 104, b = 140 | array 4-6 | colors[4] | colors[5] | colors[6]
				 * braun: r = 116, g = 101, b = 90 | array 7-9 | colors[7] | colors[8] | colors[9]
				 * grau-schwarz: r = 72, g = 71, b = 68 | array 10-12 | colors[10] | colors[11] | colors[12]
				 * schwarz: r = 29, g = 33, b = 32 | array 13-15 | colors[13] | colors[14] | colors[15]
				 */
				int[] colors = {208, 206, 151, 149, 52, 104, 140, 116, 101, 90, 72, 71, 68, 29, 33, 32};
				
				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;
						
						int rn = 0, gn = 0, bn = 0;
						
		                    if (r <= 41) { // schwarz
		                        rn = colors[13];
		                        gn = colors[14];
		                        bn = colors[15];
		                    }
		                    else if(((r>41 && r<= 62)&& g>80)||((r>60 && r<= 100)&& b>100)){
		                    	rn = colors[4]; //blau
		                        gn = colors[5];
		                        bn = colors[6];
		                    }
		                    else if((r>41 && r<= 62)&& g<80){ //schwarz
		                    	rn = colors[13];
		                        gn = colors[14];
		                        bn = colors[15];
		                    }
		                     
		                    else if(r>62 && r<=94){ //grau-schwarz
		                    	rn = colors[10];
		                        gn = colors[11];
		                        bn = colors[12];
		                    }
		                    else if(r>94 && r<=134){ //braun
		                    	rn = colors[7];
		                        gn = colors[8];
		                        bn = colors[9];
		                    }
		                    else if(r>134 && r<=180){ //grau
		                    	rn = colors[2]; 
		                        gn = colors[3];
		                        bn = colors[3];
		                    }
		                    else if(r>180 && r<=256){ //weiß
		                    	rn = colors[0]; 
		                        gn = colors[1];
		                        bn = colors[1];
		                    }
						
						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
		}


		} // CustomWindow inner class
} 

