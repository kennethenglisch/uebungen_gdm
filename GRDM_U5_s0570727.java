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
public class GRDM_U5_s0570727 implements PlugIn {

	ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;

	String[] items = {"Original", "Weichzeichnen", "Hochpassgefiltert", "Verst‰rkte Kanten"};


	public static void main(String args[]) {

//		IJ.open("/users/barthel/applications/ImageJ/_images/bear.jpg");
//		IJ.open("/Users/kenneth/git/uebung1_gdm/sail.jpg");
		IJ.open("/Users/kenneth/git/uebungen_gdm/sail.jpg");
		//IJ.open("Z:/Pictures/Beispielbilder/orchid.jpg");

		GRDM_U5_s0570727 pw = new GRDM_U5_s0570727();
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

		/**
		 * [y - 1 * width + x - 1][y - 1 * width + x][y - 1 * width + x + 1]
		 * [y * width + x - 1][y * width + x][y * width + x + 1]
		 * [y + 1 * width + x - 1][y + 1 * width + x][y + 1 * width + x + 1]
		 * 
		 * @param y
		 * @param x
		 * @return
		 */
		
		private int[] kernel(int y, int x) 
		{
			int[] kernel = new int[9];
			int count = 0;
			
			if (y == 0 || y == height - 1 || x == 0 || x == width - 1) 
			{
				for (int i = 0; i <= 8; i++)
					kernel[i] = 0;
			}
			else 
			{
				for (int i = -1; i <= 1; i++)
					for (int j = -1; j <= 1; j++) 
					{
						int pos = (y + i) * width + (x + j);
						kernel[count] = origPixels[pos];
						count++;
					}
			}
			
			return kernel;
		}
		
		private int argb(Character color, int argb) 
		{
			int rgb;
			
			switch(color) 
			{
				case 'r':	rgb = (argb >> 16) & 0xff;
							break;
							
				case 'g':	rgb = (argb >> 8) & 0xff;
							break;
				
				case 'b':	rgb = argb 		  & 0xff;
							break;
							
				default: 	rgb = -1;
							break;
			}		
			
			return rgb;
			
		}
		
		
		private void changePixelValues(ImageProcessor ip) {

			// Array zum Zur√ºckschreiben der Pixelwerte
			int[] pixels = (int[])ip.getPixels();

			if (method.equals("Original")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						
						pixels[pos] = origPixels[pos];
					}
				}
			}
			
			if (method.equals("Weichzeichnen")) {
				
				double param = 1.0 / 9.0;

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						
						int[] kernel = kernel(y, x);
						
						int rn = 0, gn = 0, bn = 0;
						
						for (int i = 0; i < kernel.length; i++) 
						{
							rn += param * argb('r' ,kernel[i]);
							gn += param * argb('g' ,kernel[i]);
							bn += param * argb('b' ,kernel[i]);
						}
						
						

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn << 8) | bn;
					}
				}
			}
				
			if (method.equals("Hochpassgefiltert")) {
				double[] param = { -1.0 / 9, -1.0 / 9, -1.0 / 9, 
								   -1.0 / 9, 8.0 / 9, -1.0 / 9, 
								   -1.0 / 9, -1.0 / 9, -1.0 / 9 };
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte

						int[] kernel = kernel(y, x);

						int rn = 0, gn = 0, bn = 0;
						
						// with offset for high pass filter 
						for (int i = 0; i < kernel.length; i++) {
							rn += param[i] * argb('r', kernel[i]) + 128;
							gn += param[i] * argb('g', kernel[i]) + 128;
							bn += param[i] * argb('b', kernel[i]) + 128;

						}

						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;
					}
				}
			}
			if (method.equals("Verst‰rkte Kanten")) {
				double[] param = { -1.0 / 9, -1.0 / 9, -1.0 / 9, 
								   -1.0 / 9, 17.0 / 9, -1.0 / 9, 
								   -1.0 / 9, -1.0 / 9, -1.0 / 9 };
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte

						int[] kernel = kernel(y, x);

						int rn = 0, gn = 0, bn = 0;

						for (int i = 0; i < kernel.length; i++) {
							rn += param[i] * argb('r', kernel[i]);
							gn += param[i] * argb('g', kernel[i]);
							bn += param[i] * argb('b', kernel[i]);

						}
						
						// check for overflow
						if (rn < 0) {
							rn = 0;
						} else if (rn > 255) {
							rn = 255;
						}

						if (gn < 0) {
							gn = 0;
						}

						else if (gn > 255) {
							gn = 255;
						}

						if (bn < 0) {
							bn = 0;
						}

						else if (bn > 255) {
							bn = 255;
						}
							pixels[pos] = (0xFF<<24) | (rn<<16) | (gn << 8) | bn;
						}
					}
				}
			
		}


	} // CustomWindow inner class
} 
