package image.pnm;

import image.util.ArrayUtil;

public class NiblackThresholder
    implements Thresholder
{
    private int neighborhood;
    private double k;

    /**
     * @param neighborhood The size of the box to use for the local threshold. Must be odd.
     * @param k Coefficient for the standard deviation.
     */
    public NiblackThresholder(int neighborhood, double k)
    {
        this.neighborhood = neighborhood;
        this.k = k;
    }

    public Pbm threshold(Pgm input)
    {
        System.out.println("Calculating Niblack thresholding...");
        int rows = input.getRows();
        int cols = input.getCols();
        int maxval = input.getMaxval();
        short[][] data = input.getData();

        double[][] dataFloat = ArrayUtil.toDoubleArray(data);
        double[][] stdev = ArrayUtil.stdevNeighborhood(dataFloat, neighborhood);
        double[][] mean = ArrayUtil.meanNeighborhood(dataFloat, neighborhood);
        double[][] threshold = ArrayUtil.add(dataFloat, mean, ArrayUtil.multiplyEach(dataFloat, stdev, k));

        System.out.println("Applying Niblack thresholding...");
        Pbm out = new Pbm(rows, cols);
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                if(data[row][col] > threshold[row][col])
                {
                    out.set(row, col, Pbm.WHITE);
                }
                else
                {
                    out.set(row, col, Pbm.BLACK);
                }
            }
        }

        return out;
    }
}
