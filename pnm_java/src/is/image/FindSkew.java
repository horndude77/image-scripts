package is.image;

import is.image.pnm.Pbm;
import is.image.pnm.Pgm;
import is.util.ConcurrentUtil;

import java.util.concurrent.ThreadPoolExecutor;

public class FindSkew
{
    /**
     * Perform the hough transform on the image.
     *
     * @param image The image.
     * @param lowAngle Start angle in radians.
     * @param highAngle End angle in radians.
     * @param step Step in radians.
     */
    public static int[][] hough(final BilevelImage image, final double lowAngle, final double highAngle, final double step)
    {
        //System.out.println("Performing hough...");
        final int rows = image.getRows();
        final int cols = image.getCols();
        final int distCount = (int) Math.sqrt(rows*rows + cols*cols);
        final int angleCount = (int) ((highAngle - lowAngle)/step);
        final int[][] h = new int[angleCount][distCount];

        //create sin, cos tables to avoid repeated calculations.
        final double[] sinTable = new double[angleCount];
        final double[] cosTable = new double[angleCount];
        for(int i=0; i<angleCount; ++i)
        {
            double theta = i*step + lowAngle;
            sinTable[i] = Math.sin(theta);
            cosTable[i] = Math.cos(theta);
        }

        ThreadPoolExecutor pool = ConcurrentUtil.createThreadPool();

        for(int rowc=0; rowc<rows; ++rowc)
        {
            final int row = rowc;
            pool.execute(new Runnable()
            {
                public void run()
                {
                    for(int col=0; col<cols; ++col)
                    {
                        if(image.get(row, col) == BilevelImage.BLACK)
                        {
                            for(int theta=0; theta<angleCount; ++theta)
                            {
                                int r = (int) (col*cosTable[theta] + row*sinTable[theta]);
                                if(r>=0) h[theta][r]++;
                            }
                        }
                    }
                }
            });
        }
        ConcurrentUtil.shutdownPoolAndAwaitTermination(pool);
        //System.out.println("Finished hough!");
        return h;
    }

    public static void writeHoughImage(String filename, int[][] h)
    {
        int rows = h.length;
        int cols = h[0].length;
        short maxval = 255;
        short[][] hshort = new short[rows][cols];
        //find max
        double max = 0;
        for(int row=0; row<rows;++row)
        {
            for(int col=0; col<cols; ++col)
            {
                if(h[row][col] > max) { max = h[row][col]; }
            }
        }

        //normalize
        for(int row=0; row<rows;++row)
        {
            for(int col=0; col<cols; ++col)
            {
                hshort[row][col] = (short) ((maxval/max) * h[row][col]);
            }
        }

        try
        {
            Pgm.write(filename, new GrayscaleImage(hshort, maxval));
        }
        catch(Exception e)
        {
            //ignore
        }
    }

    public static double findSkew(BilevelImage image)
    {
        System.out.println("Finding skew...");
        double lowAngle = -3.5, highAngle = 3.5, step = 0.05;
        int[][] h = hough(image, Math.toRadians(lowAngle), Math.toRadians(highAngle), Math.toRadians(step));
        //writeHoughImage("hough.pgm", h);
        //find largest value
        //System.out.println("Finding max angle...");
        /*
        int max = -1;
        int maxTheta = -1;
        for(int theta=0; theta<h.length; ++theta)
        {
            for(int r=0; r<h[theta].length; ++r)
            {
                if(h[theta][r] > max)
                {
                    maxTheta = theta;
                    max = h[theta][r];
                }
            }
        }
        */
        double[] squares = new double[h.length];
        for(int theta=0; theta<h.length; ++theta)
        {
            for(int r=0; r<h[theta].length; ++r)
            {
                double val = h[theta][r];
                squares[theta] += val*val;
            }
        }
        double max = -1;
        int maxTheta = -1;
        for(int i=0; i<squares.length; ++i)
        {
            if(squares[i] > max)
            {
                maxTheta = i;
                max = squares[i];
            }
        }

        double skew = -(maxTheta*step + lowAngle);
        System.out.println("Skew: "+skew);
        return skew;
    }

    public static double findSkew2(BilevelImage image)
    {
        //A more simple algorithm. Binary search for angle.
        System.out.println("Finding skew2...");

        double[] thetas = new double[5];
        thetas[0] = Math.toRadians(-5.0);
        thetas[4] = Math.toRadians(5.0);
        thetas[2] = (thetas[4] + thetas[0])/2.0;

        int[] ss = new int[5];
        ss[0] = s(thetas[0], image);
        ss[2] = s(thetas[2], image);
        ss[4] = s(thetas[4], image);

        for(int count=0; count<10; ++count)
        {
            thetas[1] = (thetas[2] + thetas[0])/2.0;
            ss[1] = s(thetas[1], image);
            thetas[3] = (thetas[4] + thetas[2])/2.0;
            ss[3] = s(thetas[3], image);

            //Find the max of the inner elements. It becomes the new midpoint
            //and the two surrounding angles become the new low and high.
            int max = -1;
            int maxIndex = 0;
            for(int i=1; i<thetas.length-1; ++i)
            {
                if(ss[i] > max)
                {
                    max = ss[i];
                    maxIndex = i;
                }
            }

            thetas[0] = thetas[maxIndex-1];
            ss[0] = ss[maxIndex-1];
            thetas[4] = thetas[maxIndex+1];
            ss[4] = ss[maxIndex+1];
            //This element must be set last to avoid overwriting a needed value early.
            thetas[2] = thetas[maxIndex];
            ss[2] = ss[maxIndex];
        }

        double skew = Math.toDegrees(-thetas[2]);
        System.out.println("Skew: "+skew);
        return skew;
    }

    public static int s(double theta, BilevelImage image)
    {
        final int rows = image.getRows();
        final int cols = image.getCols();

        int prev = 0;
        int total = 0;
        int val = 0;
        for(int row=0; row<rows; ++row)
        {
            double projectedRowSlope = Math.tan(theta);
            double projectedRow = (cols/2.0)*projectedRowSlope+row;
            total = 0;
            for(int col=0; col<cols; ++col)
            {
                total += image.getWhiteWhenOutOfRange((int) projectedRow, col);
                projectedRow += projectedRowSlope;
            }
            int diff = total - prev;
            val += diff*diff;
            prev = total;
        }

        return val;
    }
}
