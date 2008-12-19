package is.image;

import is.image.threshold.OtsuThresholder;
import is.image.util.Histogram;

public class StaffDetection
{
    public static int determineStaffHeight(BilevelImage image)
    {
        System.out.println("Determining Staff Height...");
        int rows = image.getRows();
        int cols = image.getCols();

        //vertical runlength encoding histogram
        //The most common length is probably the staff height.
        int[] histogram = new int[rows];
        int currLength = 0;
        for(int col=0; col<cols; ++col)
        {
            currLength = 0;
            for(int row=0; row<rows; ++row)
            {
                if(image.get(row, col) == BilevelImage.BLACK)
                {
                    ++currLength;
                }
                else if(currLength > 0)
                {
                    ++histogram[currLength];
                    currLength = 0;
                }
            }
        }
        int staffHeight = 0;
        int staffHeightCount = 0;
        for(int i=0; i<histogram.length; ++i)
        {
            if(histogram[i] > staffHeightCount)
            {
                staffHeight = i;
                staffHeightCount = histogram[i];
            }
        }

        //Histogram.writeHistogramImage("histogram.pbm", histogram);
        System.out.println("Staff height: "+staffHeight);

        return staffHeight;
    }

    public static BilevelImage markStaves(BilevelImage image)
    {
        int rows = image.getRows();
        int cols = image.getCols();
        int staffHeight = determineStaffHeight(image);

        //make new pbm which marks these spots.
        BilevelImage staffSpots = new BilevelImage(rows, cols);
        int currLength = 0;
        for(int col=0; col<cols; ++col)
        {
            currLength = 0;
            for(int row=0; row<rows; ++row)
            {
                if(image.get(row, col) == BilevelImage.BLACK)
                {
                    ++currLength;
                }
                else if(currLength > 0 && Math.abs(currLength-staffHeight) < 1)
                {
                    staffSpots.set(row-currLength, col, BilevelImage.BLACK);
                    currLength = 0;
                }
                else if(currLength > 0)
                {
                    currLength = 0;
                }
            }
        }

        return staffSpots;
    }
}
