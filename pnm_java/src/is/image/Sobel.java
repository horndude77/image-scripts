package is.image;

import is.image.pnm.Pbm;
import is.image.pnm.Pgm;

public class Sobel
{
    public static void main(String args[])
        throws Exception
    {
        if(args.length < 2)
        {
            System.out.println("Usage: image.Rotate <input filename> <output filename>");
            System.exit(-1);
        }

        String input_filename = args[0];
        String output_filename = args[1];

        GrayscaleImage in = Pgm.read(input_filename);
        GrayscaleImage sobel = in.sobel();
        Pgm.write(output_filename, sobel);
    }
}

