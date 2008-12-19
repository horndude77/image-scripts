package is.image;

import is.image.pnm.Pbm;
import is.image.pnm.Pgm;
import is.image.threshold.OtsuThresholder;

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
            BilevelImage image = Pbm.read(input_filename);
            double angleDegrees = FindSkew.findSkew(image);
            BilevelImage rotated = image.centerRotate(-angleDegrees);
            Pbm.write(output_filename, rotated);
        }
        else if(input_filename.matches(".*\\.pgm$"))
        {
            GrayscaleImage image = Pgm.read(input_filename);
            BilevelImage bwImage = image.toBilevelImage(new OtsuThresholder());
            double angleDegrees = FindSkew.findSkew(bwImage);
            GrayscaleImage rotated = image.centerRotate(-angleDegrees);
            Pgm.write(output_filename, rotated);
        }
    }
}

