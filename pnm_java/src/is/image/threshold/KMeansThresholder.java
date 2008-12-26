package is.image.threshold;

import is.image.BilevelImage;
import is.image.GrayscaleImage;

public class KMeansThresholder
    implements Thresholder
{
    private static final int MAX_ITERATIONS = 10;

    public BilevelImage threshold(GrayscaleImage input)
    {
        System.out.println("Performing K-Means Thresholding...");
        int rows = input.getRows();
        int cols = input.getCols();
        int maxval = input.getMaxval();

        double fMean = 0.25*maxval;
        double bMean = 0.75*maxval;
        double threshold = 0;
        double fTotal = 0, fSize = 0;
        double bTotal = 0, bSize = 0;

        for(int i=0; i<MAX_ITERATIONS && Math.abs(threshold-(fMean+bMean)/2.0) > 1.0; ++i)
        {
            threshold = (fMean+bMean)/2.0;
            fTotal = 0;
            fSize = 0;
            bTotal = 0;
            bSize = 0;
            for(int row=0; row<rows; ++row)
            {
                for(int col=0; col<cols; ++col)
                {
                    short val = input.get(row, col);
                    if(val > threshold)
                    {
                        bTotal += val;
                        ++bSize;
                    }
                    else
                    {
                        fTotal += val;
                        ++fSize;
                    }
                }
            }
            fMean = fTotal/fSize;
            bMean = bTotal/bSize;
        }

        threshold = threshold/maxval;
        System.out.println("Threshold: "+threshold);
        return (new GlobalThresholder(threshold)).threshold(input);
    }
}
