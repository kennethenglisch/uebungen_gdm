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
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
     Opens an image window and adds a panel below the image
*/
public class GLDM_U2_s0570727 implements PlugIn {

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
		
		GLDM_U2_s0570727 pw = new GLDM_U2_s0570727();
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
		private JSlider jSliderSaettigung;
		private JSlider jSliderHue;
		
		private double brightness;
		private double contrast;
		private double saettigung;
		private double hue;

		CustomWindow(ImagePlus imp, ImageCanvas ic) {
            super(imp, ic);
            addPanel();
            
            // init der Standardwerte um das Originalbild anzuzeigen
            brightness = 0;
            contrast = 5;
            saettigung = 4;
            hue = 90;
        }
    
        void addPanel() {
        	//JPanel panel = new JPanel();
        	Panel panel = new Panel();

            panel.setLayout(new GridLayout(4, 1));
            jSliderBrightness = makeTitledSlider("Helligkeit", 0, 255, 128);
//            jSliderContrast = makeTitledSlider("Kontrast", 0, 50, 5);
            
            jSliderContrast = makeTitledSlider("Kontrast", 0, 100, 50);
            
            jSliderSaettigung = makeTitledSlider("Saettigung", 0, 20, 4);
            jSliderHue = makeTitledSlider("Hue", 0, 360, 90);
        
            panel.add(jSliderBrightness);
            panel.add(jSliderContrast);
            panel.add(jSliderSaettigung);
            panel.add(jSliderHue);
            add(panel);
            
            pack();
         }
      
        private JSlider makeTitledSlider(String string, int minVal, int maxVal, int val) {
		
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
			
//			if (slider == jSliderContrast) 
//			{
//			    contrast = slider.getValue();
//				String str = "Kontrast " + contrast / 5; 
//				setSliderTitle(jSliderContrast, str); 
//			}

			// new slider in mid
			if (slider == jSliderContrast) 
			{
			    double d = slider.getValue();
			    if (d >= 50 && d < 60) contrast = d / 10 - 4;
			    else if (d >= 60) contrast = d / 5 - 10;
			    else contrast = (d / 2.5 / 20);
			    
				String str = "Kontrast " + contrast; 
				setSliderTitle(jSliderContrast, str); 
			}
			
			if (slider == jSliderSaettigung) 
			{
				saettigung = slider.getValue();
				String str = "Saettigung " + saettigung / 4;
				setSliderTitle(jSliderSaettigung, str);
			}
			
			if (slider == jSliderHue)
			{
				hue = slider.getValue();
				String str = "Hue " + hue;
				setSliderTitle(jSliderHue, str);
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
		
		private int changeContrast(int bigY) 
		{
			int bigY_new = (int) (contrast * (bigY - 127.5) + 127.5); // Formel VL Bildmanipulation I (Folie 13)
			
			return bigY_new;
		}
		
		private int[] changeSaettigung(int u, int v) 
		{
			int u_new = (int) (u * saettigung / 4); 
			int v_new = (int) (v * saettigung / 4);
			
			int[] uv = {u_new, v_new};
			
			return uv;
		}
		
		private int[] changeHue(int u, int v) 
		{
			int u_new = (int) (u * (Math.cos(Math.toRadians(hue)) + Math.sin(Math.toRadians(hue))));
			int v_new = (int) (v * (Math.sin(Math.toRadians(hue)) - Math.cos(Math.toRadians(hue))));
			
			int[] uv = {u_new, v_new};
			
			return uv;
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
					bigY = changeContrast(bigY);
					
					// adjust saettigung
					int[] uv_saettigung = changeSaettigung(u, v);
					u = uv_saettigung[0];
					v = uv_saettigung[1];
					
					// adjuts hue
					int[] uv_hue = changeHue(u, v);
					u = uv_hue[0];
					v = uv_hue[1];
					
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