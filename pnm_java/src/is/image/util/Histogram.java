package is.image.util;

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
        Pbm image = new Pbm(rows, cols);
        for(int col=0; col<cols; ++col)
        {
            for(int row=0; row<rows; ++row)
            {
                if(row > (histogram[col]*scale)/max)
                {
                    image.set(rows-row-1, col, Pbm.WHITE);
                }
                else
                {
                    image.set(rows-row-1, col, Pbm.BLACK);
                }
            }
        }
        try
        {
            image.write(filename);
        }
        catch(Exception e)
        {
            //ignore
        }
    }
}
