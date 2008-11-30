package image;

import image.pnm.Pbm;
import image.pnm.Pgm;

public class Deskew
{
    public static void main(String[] args)
        throws Exception
    {
        if(args.length < 2)
        {
            System.err.println("Usage: java image.Deskew <input pbm/pgm file> <output pbm/pgm file>");
            System.exit(-1);
        }
        String input_filename = args[0];
        String output_filename = args[1];

        if(input_filename.matches(".*\\.pbm$"))
        {
            Pbm image = new Pbm(input_filename);
            double angleDegrees = FindSkew.findSkew(image);
            Pbm rotated = image.centerRotate(-angleDegrees);
            rotated.write(output_filename);
        }
        else if(input_filename.matches(".*\\.pgm$"))
        {
            Pgm image = new Pgm(input_filename);
            Pbm bwImage = image.toPbm();
            double angleDegrees = FindSkew.findSkew(bwImage);
            Pgm rotated = image.centerRotate(-angleDegrees);
            rotated.write(output_filename);
        }
    }
}

