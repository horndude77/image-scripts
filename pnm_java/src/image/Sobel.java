package image;

import image.pnm.Pbm;
import image.pnm.Pgm;

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

        Pgm in = new Pgm(input_filename);
        Pgm sobel = in.sobel();
        sobel.write(output_filename);
    }
}

