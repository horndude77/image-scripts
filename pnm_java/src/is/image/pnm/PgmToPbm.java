package is.image.pnm;

import is.image.BilevelImage;
import is.image.GrayscaleImage;
import is.image.threshold.*;

public class PgmToPbm
{
    public static void main(String[] args)
        throws Exception
    {
        if(args.length < 2)
        {
            System.out.println("Usage: image.pnm.PgmToPbm <input pgm> <output pbm>");
            System.exit(-1);
        }

        String inputPgm = args[0];
        String outputPbm = args[1];

        GrayscaleImage in = Pgm.read(inputPgm);
        //BilevelImage out = in.toBilevelImage(new GlobalThresholder(0.45));
        //BilevelImage out = in.toBilevelImage(new BernsenThresholder(75, 15));
        //BilevelImage out = in.toBilevelImage(new NiblackThresholder(75, -0.3));
        BilevelImage out = in.toBilevelImage(new RunningAverageThresholder(30, 0.88));
        //out.clean();
        Pbm.write(outputPbm, out);
    }
}

