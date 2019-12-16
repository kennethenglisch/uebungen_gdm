import ij.*;
import ij.io.*;
import ij.process.*;
import ij.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;


public class GRDM_U4_s0570727 implements PlugInFilter {

	protected ImagePlus imp;
	final static String[] choices = {"Wischen", "Weiche Blende", "Overlay", "Schieb-Blende", "Chroma Key", "Extra", "Extra 2"};

	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_RGB+STACK_REQUIRED;
	}
	
	public static void main(String args[]) {
		ImageJ ij = new ImageJ(); // neue ImageJ Instanz starten und anzeigen 
		ij.exitWhenQuitting(true);
		
//		IJ.open("/Users/barthel/HTW/internet/meineWebseite/veranstaltungen/GLDM/uebungen/uebung4/StackB.zip");
		IJ.open("/Users/kenneth/git/uebung1_gdm/StackB.zip");
		
		GRDM_U4_s0570727 sd = new GRDM_U4_s0570727();
		sd.imp = IJ.getImage();
		ImageProcessor B_ip = sd.imp.getProcessor();
		sd.run(B_ip);
	}

	public void run(ImageProcessor B_ip) {
		// Film B wird uebergeben
		ImageStack stack_B = imp.getStack();
		
		int length = stack_B.getSize();
		int width  = B_ip.getWidth();
		int height = B_ip.getHeight();
		
		// ermoeglicht das Laden eines Bildes / Films
		Opener o = new Opener();
		OpenDialog od_A = new OpenDialog("Auswählen des 2. Filmes ...",  "");
				
		// Film A wird dazugeladen
		String dateiA = od_A.getFileName();
		if (dateiA == null) return; // Abbruch
		String pfadA = od_A.getDirectory();
		ImagePlus A = o.openImage(pfadA,dateiA);
		if (A == null) return; // Abbruch

		ImageProcessor A_ip = A.getProcessor();
		ImageStack stack_A  = A.getStack();

		if (A_ip.getWidth() != width || A_ip.getHeight() != height)
		{
			IJ.showMessage("Fehler", "Bildgrößen passen nicht zusammen");
			return;
		}
		
		// Neuen Film (Stack) "Erg" mit der kleineren Laenge von beiden erzeugen
		length = Math.min(length,stack_A.getSize());

		ImagePlus Erg = NewImage.createRGBImage("Ergebnis", width, height, length, NewImage.FILL_BLACK);
		ImageStack stack_Erg  = Erg.getStack();

		// Dialog fuer Auswahl des Ueberlagerungsmodus
		GenericDialog gd = new GenericDialog("Überlagerung");
		gd.addChoice("Methode",choices,"");
		gd.showDialog();

		int methode = 0;		
		String s = gd.getNextChoice();
		if (s.equals("Wischen")) methode = 1;
		if (s.equals("Weiche Blende")) methode = 2;
		if (s.equals("Overlay")) methode = 3;
		if (s.equals("Schieb-Blende")) methode = 4;
		if (s.equals("Chroma Key")) methode = 5;
		if (s.equals("Extra")) methode = 6;
		if (s.equals("Extra 2")) methode = 7;

		// Arrays fuer die einzelnen Bilder
		int[] pixels_B;
		int[] pixels_A;
		int[] pixels_Erg;

		// Schleife ueber alle Bilder
		for (int z=1; z<=length; z++)
		{
			pixels_B   = (int[]) stack_B.getPixels(z);
			pixels_A   = (int[]) stack_A.getPixels(z);
			pixels_Erg = (int[]) stack_Erg.getPixels(z);

			int pos = 0;
			for (int y=0; y<height; y++)
				for (int x=0; x<width; x++, pos++)
				{
					int cA = pixels_A[pos];
					int rA = (cA & 0xff0000) >> 16;
					int gA = (cA & 0x00ff00) >> 8;
					int bA = (cA & 0x0000ff);

					int cB = pixels_B[pos];
					int rB = (cB & 0xff0000) >> 16;
					int gB = (cB & 0x00ff00) >> 8;
					int bB = (cB & 0x0000ff);

					if (methode == 1) // nun horizontal - changed x+1 -> y+1
					{
					if (y+1 > (z-1)*(double)width/(length-1))
						pixels_Erg[pos] = pixels_B[pos];
					else
						pixels_Erg[pos] = pixels_A[pos];
					}

					
					if (methode == 2) // Weiche Blende
					{	/** nach Formel (Folie 47)
						* 	@credits http://home.htw-berlin.de/~barthel/veranstaltungen/GLDM/vorlesungen/04_GLDM_Bildmanipulation1_Bildpunktoperatoren.pdf
						*/
						int trans = z - 1;
						int one = length - 1;

						int r = (trans * rA + (one - trans) * rB) / one;
						int g = (trans * gA + (one - trans) * gB) / one;
						int b = (trans * bA + (one - trans) * bB) / one;

					pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + ( b & 0xff);
					}
					
					if (methode == 3) // Overlay
					{ 	/** nach Formel (Folie 38)
						* 	@credits http://home.htw-berlin.de/~barthel/veranstaltungen/GLDM/vorlesungen/04_GLDM_Bildmanipulation1_Bildpunktoperatoren.pdf
						*/
						int r, g, b;

						if (rA <= 128)

							r = rA * rB / 128;

						else
							r = 255 - (255 - rA) * (255 - rB) / 128;

						if (gA <= 128)

							g = gA * gB / 128;

						else
							g = 255 - (255 - gA) * (255 - gB) / 128;

						if (bA <= 128)

							b = bA * bB / 128;

						else
							b = 255 - (255 - bA) * (255 - bB) / 128;

						pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + (b & 0xff);
					}

					if (methode == 4) // Schieb-Blende
					{
						int line = (z - 1) * width / (length - 1);

						if (x + 1 > line) {

							pixels_Erg[pos] = pixels_B[pos - line];
						}

						else

							pixels_Erg[pos] = pixels_A[pos];
					}

					if (methode == 5) // Chroma - Key
					{

						if (rA > 150 && bA < 140)
							pixels_Erg[pos] = pixels_B[pos];
						else
							pixels_Erg[pos] = pixels_A[pos];

					}

					if (methode == 6) // Jalousie
					{

						int distance = ((z - 1) * width / (length - 1));

						int num = 16;

						pixels_Erg[pos] = pixels_B[pos];

						if (y % num < distance / ( num-1 ))
							pixels_Erg[pos] = pixels_A[pos];
					}
					
					if (methode == 7 ) // Extra
					{
						float xPlusYDividedByZ = ( width + height ) / ((float)length -1 );
						
						
						int b =  (int) Math.ceil((z-1) * xPlusYDividedByZ) ;
					
						

						if ( x + y > b ) pixels_Erg[pos] = pixels_B[pos];
						else pixels_Erg[pos] = pixels_A[pos];
					}
					
				}
		}

		// neues Bild anzeigen
		Erg.show();
		Erg.updateAndDraw();

	}

}

