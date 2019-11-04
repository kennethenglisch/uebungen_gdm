import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

//erste Uebung (elementare Bilderzeugung)

public class GLDM_U1_s0570727 implements PlugIn {
	
	final static String[] choices = {
		"Schwarzes Bild",
		"Gelbes Bild",
		"Belgische Fahne",
		"USA Fahne",
		"Fabverlauf | horizontal Schwarz/Rot , vertikal Schwarz/Blau",
		"Tschechiche Fahne",
		"Bangladeschische Fahne"
	};
	
	private String choice;
	
	public static void main(String args[]) {
		ImageJ ij = new ImageJ(); // neue ImageJ Instanz starten und anzeigen 
		ij.exitWhenQuitting(true);
		
		GLDM_U1_s0570727 imageGeneration = new GLDM_U1_s0570727();
		imageGeneration.run("");
	}
	
	public void run(String arg) {
		
		int width  = 566;  // Breite
		int height = 400;  // Hoehe
		
		// RGB-Bild erzeugen
		ImagePlus imagePlus = NewImage.createRGBImage("GLDM_U1", width, height, 1, NewImage.FILL_BLACK);
		ImageProcessor ip = imagePlus.getProcessor();
		
		// Arrays fuer den Zugriff auf die Pixelwerte
		int[] pixels = (int[])ip.getPixels();
		
		dialog();
		
		////////////////////////////////////////////////////////////////
		// Hier bitte Ihre Aenderungen / Erweiterungen
		
		if ( choice.equals("Schwarzes Bild") ) {
			generateBlackImage(width, height, pixels);
		}
		
		if ( choice.equals("Gelbes Bild") ) {
			generateYellowImage(width, height, pixels);
		}
		
		if ( choice.equals("Belgische Fahne") ) {
			generateBelgianFlag(width, height, pixels);
		}
		
		if ( choice.equals("USA Fahne") ) {
			generateUSAFlag(width, height, pixels);
		}
		
		if ( choice.equals("Fabverlauf | horizontal Schwarz/Rot , vertikal Schwarz/Blau") ) {
			generateHoriBlackRedVertiBlackBlue(width, height, pixels);
		} 
		
		if ( choice.equals("Tschechiche Fahne") ) {
			generateCzechFlag(width, height, pixels);
		}
		
		if ( choice.equals("Bangladeschische Fahne") ) {
			generateBangladeschFlag(width, height, pixels);
		}
		////////////////////////////////////////////////////////////////////
		
		// neues Bild anzeigen
		imagePlus.show();
		imagePlus.updateAndDraw();
	}

	private void generateBlackImage(int width, int height, int[] pixels) {
		// Schleife ueber die y-Werte
		for (int y=0; y<height; y++) {
			// Schleife ueber die x-Werte
			for (int x=0; x<width; x++) {
				int pos = y*width + x; // Arrayposition bestimmen
				
				int r = 0;
				int g = 0;
				int b = 0;
				
				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}
	}
	
	private void generateYellowImage(int width, int height, int[] pixels) 
	{	
		// Schleife ueber die y-Werte
		for (int y = 0; y < height; y++) 
		{
			// Schleife ueber die x-Werte
			for (int x = 0; x < width; x++) 
			{
				int pos = y * width + x; // Arrayposition bestimmen
				
				int r = 255;
				int g = 255;
				int b = 0;
				
				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}
	}
	
	private void generateBelgianFlag(int width, int height, int[] pixels)
	{
		int border1 = width / 3; // linker Strich
		int border2 = border1 * 2; // mittlerer Strich
		int border3 = border1 * 3; // rechter Strich
		
		int r = 0, g = 0, b = 0;
		
		// Schleife über die y-Werte
		for (int y = 0; y < height; y++) 
		{
			// Schleife über die x-Werte
			for (int x = 0; x < width; x++) 
			{
				int pos = y * width + x; // Arrayposition bestimmen
				
				if (x <= border1) 
				{
					r = 0;
					g = 0;
					b = 0;
				}
				
				if (border1 < x && x <= border2) 
				{
					r = 255;
					g = 212;
					b = 0;
				}
				
				if (border2 <x && x <= border3) 
				{
					r = 224;
					g = 49;
					b = 42;
				}
				
				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
			}
		}
		
	}
	
	private void generateUSAFlag(int width, int height, int[] pixels) 
	{
		int r = 0, g = 0, b = 0;
		
		// Schleife über die y-Werte
		for (int y = 0; y < height; y++) 
		{	
			// Schleife über die x-Werte
			for (int x = 0; x < width; x++) 
			{
				int pos = y * width + x; // Arrayposition bestimmen
				
				// white background
				r = 255;
				g = 255;
				b = 255;
				
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
			}
		}
		
		// red stripes
		double stripeheight = height / 13.0;
				
			// stripehoehe beachten, da auch weiße streifen gleich groß sind 
			// wird jeder zweite 'uebersprungen'
			for (int z = 0; z < height; z += 2 * stripeheight) 
			{
				// Schleife über die y-Werte
				for (int y = z; y < (z + stripeheight); y++) 
				{
					// Schleife über die x-Werte
					for (int x = 0; x < width; x++) 
					{
						int pos = y * width + x; // Arrayposition bestimmen
							
						r = 178;
						g = 34;
						b = 52;
							
						// Werte zurueckschreiben
						pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
					}
				}
			}
				
		// blue part
			// Schleife über die y-Werte
			for (int y = 0; y < (7 * stripeheight - 1.5); y++) 
			{
				// Schleife über die X-Werte
				for (int x = 0; x < (width / 2) -20; x++) 
				{
					int pos = y * width + x; // Arrayposition bestimmen
						
					r = 60;
					g = 59;
					b = 110;
						
					// Werte zurueckschreiben
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
				}
			}
	}
	
	private void generateHoriBlackRedVertiBlackBlue(int width, int height, int[] pixels) 
	{
		int r = 0, g = 0, b = 0;
		
		// Schleife über die y-Werte
		for (int y = 0; y < height; y++)
		{
			// Schleife über die x-Werte
			for (int x = 0; x < width; x++)
			{
				int pos = y * width + x; // Arrayposition bestimmen
						
				r = 255 * x / width;
				g = 0;
				b = 255 * y / height;
				
				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;	
			}
		}
	}
	
	private void generateCzechFlag(int width, int height, int[] pixels) 
	{
		int r = 0, g = 0, b = 0;
		
		int border1 = height / 2;
		int border2 = border1 * 2;
		
		int halfHeight = height / 2;
		int count = 0;
		
		// Schleife über die y-Werte
		for (int y = 0; y < height; y++)
		{
			// Schleife über die x-Werte
			for (int x = 0; x < width; x++)
			{
				int pos = y * width + x; // Arrayposition bestimmen
								
				if(y <= border1 || border2 < y)
				{
					 r = 255;
					 g = 255;
					 b = 255;
				}
				if(border1 < y && y <= border2)
				{
					 r = 197;
					 g = 47;
					 b = 40;	
				}

				if(x <= count)
				{
					r = 33;
					g = 69;
					b = 122;
				}
				
				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
			
			
			if( y >= halfHeight)
			{
				count--;
			} 
			else
			{
				count++;	
			}
		}	
	}
	
	
	private void generateBangladeschFlag(int width, int height, int[] pixels) 
	{
		int r = 0, g = 0, b = 0;
		int radius = 125;

		
		int placeX =  (width / 5) * 2 ;
		int centerY = height / 2;
		
		// Schleife über die y-Werte
		for(int y = 0; y < height; y++)
		{
			// Schleife über die x-Werte
			for(int x = 0; x < width; x++)
			{
				int pos = y * width + x; // Arrayposition bestimmen
				
				int deltaY = Math.abs(centerY - y);
				int deltaX = Math.abs(placeX - x);
				
				double c = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
				
				// pixel im radius
				if(c < radius)
				{
					r = 208;
					g = 60;
					b = 67;
				}
				
				// pixel außerhalb vom radius
				if(c > radius)
				{
					r = 43;
					g = 100;
					b = 77;
				}
				
				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
				
			}
		}
	}
	
	private void dialog() {
		// Dialog fuer Auswahl der Bilderzeugung
		GenericDialog gd = new GenericDialog("Bildart");
		
		gd.addChoice("Bildtyp", choices, choices[0]);
		
		
		gd.showDialog();	// generiere Eingabefenster
		
		choice = gd.getNextChoice(); // Auswahl uebernehmen
		
		if (gd.wasCanceled())
			System.exit(0);
	}
}

