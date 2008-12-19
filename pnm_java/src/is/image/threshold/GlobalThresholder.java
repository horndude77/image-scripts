package is.image.threshold;

import is.image.BilevelImage;
import is.image.GrayscaleImage;

public class GlobalThresholder
    implements Thresholder
{
    private double thresholdPercentage;

    public GlobalThresholder(double thresholdPercentage)
    {
        this.thresholdPercentage = thresholdPercentage;
    }

    public BilevelImage threshold(GrayscaleImage input)
    {
        //Simple thresholding
        int rows = input.getRows();
        int cols = input.getCols();
        int maxval = input.getMaxval();
        short[][] data = input.getData();

        int threshold = (int) (maxval * thresholdPercentage);
        BilevelImage out = new BilevelImage(rows, cols);
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                if(data[row][col] > threshold)
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
