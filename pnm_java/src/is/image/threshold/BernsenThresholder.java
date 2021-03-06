package is.image.threshold;

import is.image.BilevelImage;
import is.image.GrayscaleImage;
import is.image.util.ArrayUtil;

public class BernsenThresholder
    implements Thresholder
{
    private int neighborhood;
    private int l;

    public BernsenThresholder(int neighborhood, int l)
    {
        this.neighborhood = neighborhood;
        this.l = l;
    }

    public BilevelImage threshold(GrayscaleImage input)
    {
        System.out.println("Applying Bernsen thresholding...");
        int rows = input.getRows();
        int cols = input.getCols();
        int maxval = input.getMaxval();
        short[][] data = input.getData();

        int n = (neighborhood-1)/2;
        BilevelImage out = new BilevelImage(rows, cols);
        short[][] max = ArrayUtil.maxNeighborhoods(data, neighborhood);
        short[][] min = ArrayUtil.minNeighborhoods(data, neighborhood);
        for(int row=0; row<rows; ++row)
        {
            //System.out.println("row: "+row);
            for(int col=0; col<cols; ++col)
            {
                int high = max[row][col];
                int low = min[row][col];
                if( (high - low) < l )
                {
                    //This indicates that the block is uniform. For now assume
                    //white background.
                    //System.out.println("Uniform section: " + (high-low) + " < " + l);
                    out.set(row, col, BilevelImage.WHITE);
                }
                else
                {
                    int threshold = (int) ((high+low)*0.4);
                    //System.out.println(threshold + " < " + data[row][col]);
                    if(data[row][col] > threshold)
                    {
                        //System.out.println("\tWHITE");
                        out.set(row, col, BilevelImage.WHITE);
                    }
                    else
                    {
                        //System.out.println("\tBLACK");
                        out.set(row, col, BilevelImage.BLACK);
                    }
                }
            }
        }
        return out;
    }
}
