package is.image.threshold;

import is.image.BilevelImage;
import is.image.GrayscaleImage;

/**
 * Computes the threshold function based on running averages. The running
 * averages of each row going left to right and right to left, and each
 * column going top to bottom and bottom to top are computed. These four
 * values are averaged at each pixel. The result is a descent adaptive
 * threshold.
 *
 * A short coming occurs at the intersection of horizontal and vertical
 * lines. The running average has seen black long enough to sometime make
 * these intersections white.
 */
public class RunningAverageThresholder
    implements Thresholder
{
    private int averageLength;
    private double percentageOfThreshold;

    /**
     * @param averageLength Length of running average.
     * @param percentageOfThreshold Percentage of the average where the threshold is set.
     */
    public RunningAverageThresholder(int averageLength, double percentageOfThreshold)
    {
        this.averageLength = averageLength;
        this.percentageOfThreshold = percentageOfThreshold;
    }

    public BilevelImage threshold(GrayscaleImage input)
    {
        System.out.println("Calculating Running Average thresholding...");
        int rows = input.getRows();
        int cols = input.getCols();
        int maxval = input.getMaxval();
        short[][] data = input.getData();

        double divisor = 8.0;
        double[][] threshold = new double[rows][cols];
        double avg;
        //horizontal
        for(int row=0; row<rows; ++row)
        {
            //left to right
            avg = maxval/2.0;
            for(int col=0; col<cols; ++col)
            {
                avg = avg - (avg - data[row][col])/averageLength;
                threshold[row][col] += avg/divisor;
            }

            //right to left
            avg = maxval/2.0;
            for(int col=cols-1; col>=0; --col)
            {
                avg = avg - (avg - data[row][col])/averageLength;
                threshold[row][col] += avg/divisor;
            }
        }
        //vertical
        for(int col=0; col<cols; ++col)
        {
            //top to bottom
            avg = maxval/2.0;
            for(int row=0; row<rows; ++row)
            {
                avg = avg - (avg - data[row][col])/averageLength;
                threshold[row][col] += avg/divisor;
            }

            //bottom to top
            avg = maxval/2.0;
            for(int row=rows-1; row>=0; --row)
            {
                avg = avg - (avg - data[row][col])/averageLength;
                threshold[row][col] += avg/divisor;
            }
        }

        //diagonals
        int drow=rows-1;
        int dcol=0;
        while(dcol<cols)
        {
            //down
            avg = maxval/2.0;
            int row, col;
            for(row=drow, col=dcol; row<rows && col<cols; row+=1, col+=1)
            {
                avg = avg - (avg - data[row][col])/averageLength;
                threshold[row][col] += avg/divisor;
            }

            avg = maxval/2.0;
            for(; row<rows && col<cols; row-=1, col-=1)
            {
                avg = avg - (avg - data[row][col])/averageLength;
                threshold[row][col] += avg/divisor;
            }

            if(drow == 0)
            {
                dcol += 1;
            }
            else
            {
                drow -= 1;
            }
        }

        drow=rows-1;
        dcol=cols-1;
        while(drow > 0)
        {
            //up
            avg = maxval/2.0;
            int row, col;
            for(row=drow, col=dcol; row>0 && col<cols; row-=1, col+=1)
            {
                avg = avg - (avg - data[row][col])/averageLength;
                threshold[row][col] += avg/divisor;
            }

            avg = maxval/2.0;
            for(; row>0 && col<cols; row+=1, col-=1)
            {
                avg = avg - (avg - data[row][col])/averageLength;
                threshold[row][col] += avg/divisor;
            }

            if(dcol == 0)
            {
                drow -= 1;
            }
            else
            {
                dcol -= 1;
            }
        }

        System.out.println("Applying Running Average thresholding...");
        //compute binary image.
        BilevelImage out = new BilevelImage(rows, cols);
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                if(data[row][col] > percentageOfThreshold*threshold[row][col])
                {
                    out.set(row, col, BilevelImage.WHITE);
                }
                else
                {
                    out.set(row, col, BilevelImage.BLACK);
                }
            }
        }
        return out;
    }
}
