package image;

import image.pnm.Pbm;

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
    public static int[][] hough(Pbm image, double lowAngle, double highAngle, double step)
    {
        //System.out.println("Performing hough...");
        int rows = image.getRows();
        int cols = image.getCols();
        int distCount = (int) Math.sqrt(rows*rows + cols*cols);
        int angleCount = (int) ((highAngle - lowAngle)/step);
        int[][] h = new int[angleCount][distCount];

        //create sin, cos tables to avoid repeated calculations.
        double[] sinTable = new double[angleCount];
        double[] cosTable = new double[angleCount];
        for(int i=0; i<angleCount; ++i)
        {
            double theta = i*step + lowAngle;
            sinTable[i] = Math.sin(theta);
            cosTable[i] = Math.cos(theta);
        }

        for(int row=0; row<rows; ++row)
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
        return maxTheta*step + lowAngle;
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

