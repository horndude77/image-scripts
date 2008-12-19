package is.image.util;

import is.image.BilevelImage;
import is.image.pnm.Pbm;

public class Histogram
{
    public static void writeHistogramImage(String filename, int[] histogram)
    {
        int scale = 1000;
        int max = 0;
        for(int i=0; i<histogram.length; ++i)
        {
            if(histogram[i] > max)
            {
                max = histogram[i];
            }
        }
        max = (int) (max*1.05);

        int rows = scale;
        int cols = histogram.length;
        BilevelImage image = new BilevelImage(rows, cols);
        for(int col=0; col<cols; ++col)
        {
            for(int row=0; row<rows; ++row)
            {
                if(row > (histogram[col]*scale)/max)
                {
                    image.set(rows-row-1, col, BilevelImage.WHITE);
                }
                else
                {
                    image.set(rows-row-1, col, BilevelImage.BLACK);
                }
            }
        }
        try
        {
            Pbm.write(filename, image);
        }
        catch(Exception e)
        {
            //ignore
        }
    }
}
