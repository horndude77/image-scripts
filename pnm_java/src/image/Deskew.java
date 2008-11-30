package image;

import image.pnm.Pbm;

public class Deskew
{
    public static void main(String[] args)
        throws Exception
    {
        if(args.length < 2)
        {
            System.err.println("Usage: java image.Deskew <input pbm file> <output pbm file>");
            System.exit(-1);
        }
        String input_filename = args[0];
        String output_filename = args[1];
        Pbm image = new Pbm(input_filename);
        double angleDegrees = FindSkew.findSkew(image);
        Pbm rotated = image.centerRotate(angleDegrees);
        rotated.write(output_filename);
    }
}

