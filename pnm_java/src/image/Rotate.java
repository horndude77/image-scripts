package image;

import image.pnm.Pbm;

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
            Pbm image = null;
            try
            {
                image = new Pbm(input_filename);
            }
            catch(java.io.IOException e)
            {
                e.printStackTrace();
                System.err.println("Unable to read file: "+input_filename);
                System.exit(-1);
            }

            Pbm rotated = image.centerRotate(angle);

            if(!output_filename.matches(".*\\.pbm$"))
            {
                output_filename += ".pbm";
            }

            try
            {
                rotated.write(output_filename);
            }
            catch(java.io.IOException e)
            {
                e.printStackTrace();
                System.err.println("Unable to write file: "+output_filename);
                System.exit(-1);
            }
        }
    }
}

