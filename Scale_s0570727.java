import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class Scale_s0570727 implements PlugInFilter {
	ImagePlus imp;

	public static void main(String args[]) {

		IJ.open("./component.jpg");

		Scale_s0570727 sc = new Scale_s0570727();
		sc.imp = IJ.getImage();
		ImageProcessor B_ip = sc.imp.getProcessor();
		sc.run(B_ip);
	}

	public int setup(String arg, ImagePlus imp) {
		if (arg.equals("about")) {
			showAbout();
			return DONE;
		}
		return DOES_RGB + NO_CHANGES;
		// kann RGB-Bilder und veraendert das Original nicht
	}

	public void run(ImageProcessor ip) {

		String[] dropdownmenue = { "Kopie", "Pixelwiederholung", "Bilinear" };

		GenericDialog gd = new GenericDialog("scale");
		gd.addChoice("Methode", dropdownmenue, dropdownmenue[0]);
		gd.addNumericField("Hoehe:", 500, 0);
		gd.addNumericField("Breite:", 400, 0);

		gd.showDialog();

		int newHeight = (int) gd.getNextNumber(); // fuer das neue skalierte Bild
		int newWidth = (int) gd.getNextNumber();  // fuer das neue skalierte Bild

		int width = ip.getWidth(); // Breite bestimmen
		int height = ip.getHeight(); // Hoehe bestimmen

		ImagePlus neu = NewImage.createRGBImage("Skaliertes Bild", newWidth, newHeight, 1, NewImage.FILL_BLACK); // black für Kopie, falls Bild kleiner als Fläche

		ImageProcessor newIP = neu.getProcessor();

		int[] pix = (int[]) ip.getPixels(); // get original Pixel
		int[] newPix = (int[]) newIP.getPixels(); // get new Pixels

		double scaleHeight = height / (newHeight * 1.0);
		double scaleWidth = width / (newWidth * 1.0);

		String method = gd.getNextChoice();


		if (method.equals("Kopie")) {
			// Schleife ueber das neue Bild
			for (int newY = 0; newY < newHeight; newY++) {
				for (int newX = 0; newX < newWidth; newX++) {
					int y = newY;
					int x = newX;

					if (y < height && x < width) {
						int newPos = newY * newWidth + newX;
						int pos = y * width + x;

						newPix[newPos] = pix[pos];
					}
				}
			}
		}

		// http://home.htw-berlin.de/~barthel/veranstaltungen/GLDM/vorlesungen/06_GLDM_Bildmanipulation3_geometrische.pdf
		if (method.equals("Pixelwiederholung")) {

			// Schleife ueber das neue Bild
			for (int newY = 0; newY < newHeight; newY++) {
				for (int newX = 0; newX < newWidth; newX++) {
					
					// gerundete Werte für x und y
					int x = (int) Math.round(newX * scaleWidth);
					int y = (int) Math.round(newY * scaleHeight);

					if (y < height && x < width) {
						int newPos = newY * newWidth + newX;
						int pos = y * width + x;

						newPix[newPos] = pix[pos];

					}
				}
			}
		}

		if (method.equals("Bilinear")) {
			int PointA, PointB, PointC, PointD;
			
			// Skalierungswerte, brauchte height-1 und width-1 sonst IndexOutOfBounds-Fehler
			
			scaleHeight = (height - 1) / (newHeight * 1.0);
			scaleWidth = (width - 1) / (newWidth * 1.0);
			// Schleife ueber das neue Bild
			for (int newY = 0; newY < newHeight; newY++) {
				for (int newX = 0; newX < newWidth; newX++) {
					int y = (int) (newY * scaleHeight);
					int x = (int) (newX * scaleWidth);

					// v und h sind die Nachkommastelle vom skalierten Y-Wert/X-Wert
					double v = (newY * scaleHeight) - y;
					double h = (newX * scaleWidth) - x;

					// Alte Position
					int position = y * width + x;

					// Die Punkte A, B, C und D und ihre ARGB-Werte
					PointA = pix[position];
					int[] argbA = { ((PointA >> 16) & 0xff), ((PointA >> 8) & 0xff), (PointA & 0xff) };
					PointB = pix[position + 1];
					int[] argbB = { ((PointB >> 16) & 0xff), ((PointB >> 8) & 0xff), (PointB & 0xff) };
					PointC = pix[position + width];
					int[] argbC = { ((PointC >> 16) & 0xff), ((PointC >> 8) & 0xff), (PointC & 0xff) };
					PointD = pix[position + width + 1];
					int[] argbD = { ((PointD >> 16) & 0xff), ((PointD >> 8) & 0xff), (PointD & 0xff) };

					int r, g, b;

					// Formel aus: http://home.htw-berlin.de/~barthel/veranstaltungen/GLDM/vorlesungen/07_GLDM_Bildmanipulation3_geometrische_2.pdf
					// bzw. http://home.htw-berlin.de/~barthel/veranstaltungen/GLDM/vorlesungen/06_GLDM_Bildmanipulation3_geometrische.pdf
					// P = A * (1-h) * (1-V) + B * h * (1-v) + C * (1-h) * v + D * h * v
					r = (int) (argbA[0] * (1 - h) * (1 - v) + argbB[0] * h * (1 - v) + argbC[0] * (1 - h) * v
							+ argbD[0] * h * v);
					g = (int) (argbA[1] * (1 - h) * (1 - v) + argbB[1] * h * (1 - v) + argbC[1] * (1 - h) * v
							+ argbD[1] * h * v);
					b = (int) (argbA[2] * (1 - h) * (1 - v) + argbB[2] * h * (1 - v) + argbC[2] * (1 - h) * v
							+ argbD[2] * h * v);


					// Berechnung der neuen Position
					int newPos = newY * newWidth + newX;

					// Zurückschreiben der Werte
					newPix[newPos] = 0xFF << 24 | r << 16  | g << 8  | b;

				}
			}
		}

		// neues Bild anzeigen
		neu.show();
		neu.updateAndDraw();
	}

	void showAbout() {
		IJ.showMessage("");
	}
}
