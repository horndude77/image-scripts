package is.image;

import is.image.pnm.Pbm;
import is.image.pnm.Pgm;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;

public class FindSkew
{
    public static final int NUM_PROCESSORS = Runtime.getRuntime().availableProcessors();

    /**
     * Perform the hough transform on the image.
     *
     * @param image The image.
     * @param lowAngle Start angle in radians.
     * @param highAngle End angle in radians.
     * @param step Step in radians.
     */
    public static int[][] hough(final Pbm image, final double lowAngle, final double highAngle, final double step)
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

        ThreadPoolExecutor pool = new ThreadPoolExecutor(NUM_PROCESSORS, NUM_PROCESSORS, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

        for(int rowc=0; rowc<rows; ++rowc)
        {
            final int row = rowc;
            pool.execute(new Runnable()
            {
                public void run()
                {
                    for(int col=0; col<cols; ++col)
                    {
                        if(image.get(row, col) == Pbm.BLACK)
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
        try
        {
            pool.shutdown();
            pool.awaitTermination(1000, TimeUnit.SECONDS);
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
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
            (new Pgm(hshort, maxval)).write(filename);
        }
        catch(Exception e)
        {
            //ignore
        }
    }

    public static double findSkew(Pbm image)
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

    public static void main(String[] args)
        throws Exception
    {
        if(args.length < 1)
        {
            System.err.println("Usage: java image.FindSkew <pbm file>");
            System.exit(-1);
        }
        String filename = args[0];
        Pbm image = new Pbm(filename);
        double angleDegrees = findSkew(image);
        System.out.println(angleDegrees);
    }
}

