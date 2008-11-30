package image;

import image.pnm.Pbm;

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
    public static int[][] hough(final Pbm image, double lowAngle, double highAngle, double step)
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

    public static double findSkew(Pbm image)
    {
        double lowAngle = -3.5, highAngle = 3.5, step = 0.05;
        int[][] h = hough(image, Math.toRadians(lowAngle), Math.toRadians(highAngle), Math.toRadians(step));
        //find largest value
        //System.out.println("Finding max angle...");
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
        //System.out.println("Found max angle!");
        return -(maxTheta*step + lowAngle);
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

