package is.image;

import is.image.pnm.Pbm;
import is.image.pnm.Pgm;

public class Rotate
{
    public static void main(String args[])
    {
        if(args.length < 3)
        {
            System.out.println("Usage: image.Rotate <angle in degrees> <input filename> <output filename>");
            System.exit(-1);
        }

        double angle = Double.parseDouble(args[0]);
        String input_filename = args[1];
        String output_filename = args[2];

        if(input_filename.matches(".*\\.pbm$"))
        {
            BilevelImage image = null;
            try
            {
                image = Pbm.read(input_filename);
            }
            catch(java.io.IOException e)
            {
                e.printStackTrace();
                System.err.println("Unable to read file: "+input_filename);
                System.exit(-1);
            }

            BilevelImage rotated = image.centerRotate(angle);

            if(!output_filename.matches(".*\\.pbm$"))
            {
                output_filename += ".pbm";
            }

            try
            {
                Pbm.write(output_filename, rotated);
            }
            catch(java.io.IOException e)
            {
                e.printStackTrace();
                System.err.println("Unable to write file: "+output_filename);
                System.exit(-1);
            }
        }
        else if(input_filename.matches(".*\\.pgm$"))
        {
            GrayscaleImage image = null;
            try
            {
                image = Pgm.read(input_filename);
            }
            catch(java.io.IOException e)
            {
                e.printStackTrace();
                System.err.println("Unable to read file: "+input_filename);
                System.exit(-1);
            }

            GrayscaleImage rotated = image.centerRotate(angle);

            if(!output_filename.matches(".*\\.pgm$"))
            {
                output_filename += ".pgm";
            }

            try
            {
                Pgm.write(output_filename, rotated);
            }
            catch(java.io.IOException e)
            {
                e.printStackTrace();
                System.err.println("Unable to write file: "+output_filename);
                System.exit(-1);
            }
        }
        else
        {
            System.err.println("Unsupported filetype: "+input_filename);
            System.exit(-1);
        }
    }
}

