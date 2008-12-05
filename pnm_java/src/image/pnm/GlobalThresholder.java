package image.pnm;

public class GlobalThresholder
    implements Thresholder
{
    private double thresholdPercentage;

    public GlobalThresholder(double thresholdPercentage)
    {
        this.thresholdPercentage = thresholdPercentage;
    }

    public Pbm threshold(Pgm input)
    {
        //Simple thresholding
        int rows = input.getRows();
        int cols = input.getCols();
        int maxval = input.getMaxval();
        short[][] data = input.getData();

        int threshold = (int) (maxval * thresholdPercentage);
        Pbm out = new Pbm(rows, cols);
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                if(data[row][col] > threshold)
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
