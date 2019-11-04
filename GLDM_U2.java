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

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
     Opens an image window and adds a panel below the image
*/
public class GLDM_U2 implements PlugIn {

    ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;
	
	
    public static void main(String args[]) {
		//new ImageJ();
    	// Mac
//    	IJ.open("/Users/kenneth/Documents/Uni/2. Semester/GDM/Übung 2/orchid.jpg");
    	
    	// Windows
    	IJ.open("/Users/kenneth/git/uebung1_gdm/orchid.jpg");
    	
    	//IJ.open("Z:/Pictures/Beispielbilder/orchid.jpg");
		
		GLDM_U2 pw = new GLDM_U2();
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
    
    
    class CustomWindow extends ImageWindow implements ChangeListener {
         
        private JSlider jSliderBrightness;
		private JSlider jSliderContrast;
		private double brightness;
		private double contrast;

		CustomWindow(ImagePlus imp, ImageCanvas ic) {
            super(imp, ic);
            addPanel();
        }
    
        void addPanel() {
        	//JPanel panel = new JPanel();
        	Panel panel = new Panel();

            panel.setLayout(new GridLayout(4, 1));
            jSliderBrightness = makeTitledSilder("Helligkeit", 0, 255, 128);
            jSliderContrast = makeTitledSilder("Contrast", 0, 100, 50);
            panel.add(jSliderBrightness);
            panel.add(jSliderContrast);
            
            add(panel);
            
            pack();
         }
      
        private JSlider makeTitledSilder(String string, int minVal, int maxVal, int val) {
		
        	JSlider slider = new JSlider(JSlider.HORIZONTAL, minVal, maxVal, val );
        	Dimension preferredSize = new Dimension(width, 50);
        	slider.setPreferredSize(preferredSize);
			TitledBorder tb = new TitledBorder(BorderFactory.createEtchedBorder(), 
					string, TitledBorder.LEFT, TitledBorder.ABOVE_BOTTOM,
					new Font("Sans", Font.PLAIN, 11));
			slider.setBorder(tb);
			slider.setMajorTickSpacing((maxVal - minVal)/10 );
			slider.setPaintTicks(true);
			slider.addChangeListener(this);
			
			return slider;
		}
        
        private void setSliderTitle(JSlider slider, String str) {
			TitledBorder tb = new TitledBorder(BorderFactory.createEtchedBorder(),
				str, TitledBorder.LEFT, TitledBorder.ABOVE_BOTTOM,
					new Font("Sans", Font.PLAIN, 11));
			slider.setBorder(tb);
		}

		public void stateChanged( ChangeEvent e ){
			JSlider slider = (JSlider)e.getSource();

			if (slider == jSliderBrightness) {
				brightness = slider.getValue() - 128;
				String str = "Helligkeit " + brightness; 
				setSliderTitle(jSliderBrightness, str); 
			}
			
			if (slider == jSliderContrast) 
			{
				double value = slider.getValue();
				
				if (value <= 50) contrast = (value - 40);
				else if (value <= 60) contrast = (int) (value / 10) - 4;
				else if (value <= 70) contrast = (int) (value / 10) - 3;
				else if (value <= 80) contrast = (int) (value / 10) - 2;
				else if (value <= 90) contrast = (int) (value / 10) - 1;
				else contrast = (int) (value / 10);
				
				//contrast = slider.getValue();
				String str = "Contrast " + contrast; 
				setSliderTitle(jSliderContrast, str); 
			}
			
			changePixelValues(imp.getProcessor());
			
			imp.updateAndDraw();
		}

		
		private int[] transformToYUV(int red, int green, int blue) 
		{
					double bigY = 0.299 * red + 0.587 * green + 0.114 * blue; // Luminanz
					double u = (blue - bigY) * 0.493;  // U-Kanal
					double v = (red - bigY) * 0.877; // V-Kanal
					
					int[] yuv = {(int) bigY,(int) u,(int) v};
					
					return yuv;
		}
		
		private int[] transformToRGB(int bigY, int u, int v) 
		{
			double rnew = bigY + v / 0.877;
			double bnew = bigY + u / 0.493;
			double gnew = 1 / 0.587 * bigY - 0.299 / 0.587 * rnew - 0.114 / 0.587 * bnew;
			
			int[] rgb = {(int) rnew, (int) gnew, (int) bnew};
			
			return rgb;
		}
		
		private int changeContrast(int bigY, int u, int v) 
		{
			int bigY_new = (int) ((bigY - 128) * contrast + 128);
			
			return bigY_new;
		}
		
		private void changePixelValues(ImageProcessor ip) {
			
			// Array fuer den Zugriff auf die Pixelwerte
			int[] pixels = (int[])ip.getPixels();
			
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					int pos = y*width + x;
					int argb = origPixels[pos];  // Lesen der Originalwerte 
					
					int r = (argb >> 16) & 0xff;
					int g = (argb >>  8) & 0xff;
					int b =  argb        & 0xff;
					
					// rgb zu yuv transformieren
					int[] yuv = transformToYUV(r, g, b);
					int bigY = yuv[0];
					int u = yuv[1];
					int v = yuv[2];
					
					// adjust brightness
					bigY += (int) brightness;
					
					// adjust contrast
//					int yuv_contrast = changeContrast(bigY, u, v);
//					bigY = changeContrast(bigY, u, v);
//					u = yuv_contrast[1];
//					v = yuv_contrast[2];
					
					// yuv zu rgb transformieren
					int[] rgb = transformToRGB(bigY, u, v);
					int rn = rgb[0];
					int gn = rgb[1];
					int bn = rgb[2];
					
					// Farbueberlauf korrigieren
					if (rn < 0) rn = 0;
					else if (rn > 255) rn = 255;
					
					if (gn < 0) gn = 0;
					else if (gn > 255) gn = 255;
					
					if (bn < 0) bn = 0;
					else if (bn > 255) bn = 255;
					
					// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden
					
					pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
				}
			}
		}
		
    } // CustomWindow inner class
} 