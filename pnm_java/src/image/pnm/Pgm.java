package image.pnm;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;

public class Pgm
{
    public static final int NUM_PROCESSORS = Runtime.getRuntime().availableProcessors();

    private int rows, cols;
    short maxval;
    private short[][] data;

    private static final byte[] MAGIC = "P5".getBytes();
    private static final byte[] MAGIC_RAW = "P2".getBytes();

    public Pgm(int rows, int cols, short maxval)
    {
        this.rows = rows;
        this.cols = cols;
        this.maxval = maxval;
        this.data = new short[rows][cols];
    }

    public Pgm(InputStream is)
        throws IOException
    {
        read(is);
    }

    public Pgm(String filename)
        throws IOException
    {
        InputStream is = null;
        try
        {
            is = new BufferedInputStream(new FileInputStream(filename));
            this.read(is);
        }
        finally
        {
            if(is != null)
            {
                is.close();
            }
        }
    }

    public Pbm toPbm()
    {
        //Pbm out = RunningAverageAdaptiveThreshold(30, 0.75);
        //Pbm out = GlobalThreshold(0.45);
        Pbm out = NiblackThreshold(31, -0.2);
        postProcess(out, 75);
        return out;
    }

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
     *
     * @param averageLength Length of running average.
     * @param percentageOfThreshold Percentage of the average where the threshold is set.
     */
    private Pbm RunningAverageAdaptiveThreshold(int averageLength, double percentageOfThreshold)
    {
        double divisor = 6.0;
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
            for(int row=drow, col=dcol; row<rows && col<cols; row+=1, col+=1)
            {
                avg = avg - (avg - data[row][col])/averageLength;
                threshold[row][col] += avg/divisor;
            }

            //TODO: reverse direction

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
            for(int row=drow, col=dcol; row>0 && col<cols; row-=1, col+=1)
            {
                avg = avg - (avg - data[row][col])/averageLength;
                threshold[row][col] += avg/divisor;
            }

            //TODO: reverse direction

            if(dcol == 0)
            {
                drow -= 1;
            }
            else
            {
                dcol -= 1;
            }
        }

        //compute binary image.
        Pbm out = new Pbm(rows, cols);
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                if(data[row][col] > 0.85*threshold[row][col])
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

    private Pbm GlobalThreshold(double thresholdReal)
    {
        //Simple thresholding
        int threshold = (int) (maxval * thresholdReal);
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

    /**
     * @param boxSize The size of the box to use for the local threshold. Must be odd.
     * @param k Coefficient for the standard deviation.
     */
    private Pbm NiblackThreshold(int boxSize, double k)
    {
        //TODO: speed up!
        int n = (boxSize-1)/2;
        int count = boxSize*boxSize;
        Pbm out = new Pbm(rows, cols);
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                double mean = 0.0;

                int lowRow = Math.max(0, row-n);
                int lowCol = Math.max(0, col-n);
                int highRow = Math.min(rows-1, row+n);
                int highCol = Math.min(cols-1, col+n);

                for(int irow=lowRow; irow<=highRow; ++irow)
                {
                    for(int icol=lowCol; icol<=highCol; ++icol)
                    {
                        mean += data[irow][icol];
                    }
                }
                mean = mean/count;

                double stdev = 0x0;
                for(int irow=lowRow; irow<=highRow; ++irow)
                {
                    for(int icol=lowCol; icol<=highCol; ++icol)
                    {
                        double val = data[irow][icol] - mean;
                        stdev += val*val;
                    }
                }
                stdev = Math.sqrt(stdev/count);

                double threshold = mean + k*stdev;

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

    public void postProcess(Pbm out, double energyThreshold)
    {
        Pgm sobel = this.sobel();
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                if(out.get(row, col) == Pbm.BLACK)
                {
                    //count surrounding black pixels
                    int lowRow = Math.max(0, row-1);
                    int lowCol = Math.max(0, col-1);
                    int highRow = Math.min(rows-1, row+1);
                    int highCol = Math.min(cols-1, col+1);

                    int count = 0;
                    for(int irow=lowRow; irow<=highRow; ++irow)
                    {
                        for(int icol=lowCol; icol<=highCol; ++icol)
                        {
                            if(out.get(irow, icol) == Pbm.WHITE)
                            {
                                count += 1;
                            }
                        }
                    }
                    if(count >= 4 && sobel.get(row, col) < energyThreshold)
                    {
                        out.set(row, col, Pbm.WHITE);
                        //if(row > 0) --row;
                        //if(col > 0) --col;
                    }
                }
            }
        }
    }

    public void set(int row, int col, short val)
    {
        data[row][col] = val;
    }

    public short get(int row, int col)
    {
        return data[row][col];
    }

    public short getZeroWhenOutOfRange(int row, int col)
    {
        if(row < 0 || row >= rows || col < 0 || col >= cols)
        {
            return 0;
        }
        else
        {
            return data[row][col];
        }
    }

    public int getRows()
    {
        return this.rows;
    }

    public int getCols()
    {
        return this.cols;
    }

    private boolean isWhiteSpace(int b)
    {
        return b == 0x0A || b == 0x0D || b == 0x20 || b == 0x09;
    }

    private boolean isDigit(int b)
    {
        return b >= 0x30 && b <= 0x39;
    }

    private void read(InputStream is)
        throws IOException
    {
        //System.out.println("Reading image...");

        boolean raw;
        int b;

        b = is.read();
        if(b != (0xff & MAGIC[0]))
        {
            int m1 = b;
            int m2 = is.read();
            throw new IOException("Bad magic number: " + (char) m1 + "" + (char) m2);
        }

        b = is.read();
        if(b == (0xff & MAGIC[1]))
        {
            raw = false;
        }
        else if(b == (0xff & MAGIC_RAW[1]))
        {
            raw = true;
        }
        else
        {
            throw new IOException("Bad magic number");
        }

        //skip whitespace
        b = is.read();
        while(isWhiteSpace(b))
        {
            b = is.read();
        }

        //Skip comment
        if(b == 0x23)
        {
            while(b != 0x0a)
            {
                b = is.read();
            }
            b = is.read();
        }

        //read width
        StringBuffer sb = new StringBuffer();
        while(isDigit(b))
        {
            sb.append((char) b);
            b = is.read();
        }
        cols = Integer.parseInt(sb.toString());

        //skip whitespace
        while(isWhiteSpace(b))
        {
            b = is.read();
        }

        //read height
        sb = new StringBuffer();
        while(isDigit(b))
        {
            sb.append((char) b);
            b = is.read();
        }
        rows = Integer.parseInt(sb.toString());

        //skip whitespace
        while(isWhiteSpace(b))
        {
            b = is.read();
        }

        //read maxval
        sb = new StringBuffer();
        while(isDigit(b))
        {
            sb.append((char) b);
            b = is.read();
        }
        maxval = Short.parseShort(sb.toString());

        //Skip a single whitespace character
        b = is.read();

        //read data
        b = is.read();
        data = new short[rows][cols];
        if(raw)
        {
            for(int row=0; row<rows; ++row)
            {
                for(int col=0; col<cols; ++col)
                {
                    //read pixel value
                    sb = new StringBuffer();
                    while(isDigit(b))
                    {
                        sb.append((char) b);
                        b = is.read();
                    }
                    data[row][col] = Short.parseShort(sb.toString());

                    //skip whitespace
                    while(isWhiteSpace(b))
                    {
                        b = is.read();
                    }
                }
            }
        }
        else
        {
            for(int row=0; row<rows; ++row)
            {
                for(int col=0; col<cols; ++col)
                {
                    data[row][col] = (short) is.read();
                }
            }
        }
        //System.out.println("Finished reading!");
    }

    public void write(String filename)
        throws IOException
    {
        OutputStream os = null;
        try
        {
            os = new BufferedOutputStream(new FileOutputStream(filename));
            this.write(os);
        }
        finally
        {
            if(os != null)
            {
                os.close();
            }
        }
    }

    public void write(OutputStream os)
        throws IOException
    {
        //System.out.println("Writing image...");
        os.write("P5\n".getBytes());
        os.write((cols+" "+rows+"\n").getBytes());
        os.write((maxval+"\n").getBytes());

        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                os.write(data[row][col]);
            }
        }
        //System.out.println("Finished writing!");
    }

    public Pgm centerRotate(double angle_degrees)
    {
        return this.rotate(angle_degrees, this.cols/2.0, this.rows/2.0);
    }

    public Pgm rotate(final double angle_degrees, final double cx, final double cy)
    {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(NUM_PROCESSORS, NUM_PROCESSORS, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        //System.out.println("Number of processors available: "+NUM_PROCESSORS);

        final Pgm rotated = new Pgm(rows, cols, maxval);

        double angle = Math.toRadians(angle_degrees);
        final double sina = Math.sin(angle);
        final double cosa = Math.cos(angle);

        for(int rowc=0; rowc<rows; ++rowc)
        {
            final int row = rowc;
            pool.execute(new Runnable()
            {
                public void run()
                {
                    //System.out.println("Working on row: "+row);
                    for(int col=0; col<cols; ++col)
                    {
                        //location in original
                        double rx = (col-cx)*cosa - (row-cy)*sina;
                        double ry = (col-cx)*sina + (row-cy)*cosa;
                        //integer location in original
                        int x = (int) (rx+cx);
                        int y = (int) (ry+cy);
                        //weights to use for picking value
                        double wx = 1.0 - (rx - (x - cx));
                        double wy = 1.0 - (ry - (y - cy));

                        double val = 0.0;
                        if(x > 0 && x < cols && y > 0 && y < rows)
                        {
                            val += wx*wy*data[y][x];
                        }
                        if((x+1) > 0 && (x+1) < cols && y > 0 && y < rows)
                        {
                            val += (1.0-wx)*wy*data[y][x+1];
                        }
                        if(x > 0 && x < cols && (y+1) > 0 && (y+1) < rows)
                        {
                            val += wx*(1.0-wy)*data[y+1][x];
                        }
                        if((x+1) > 0 && (x+1) < cols && (y+1) > 0 && (y+1) < rows)
                        {
                            val += (1.0-wx)*(1.0-wy)*data[y+1][x+1];
                        }
                        rotated.set(row, col, (short) Math.round(val));
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
        //System.out.println("Finished rotating!");
        return rotated;
    }

    public Pgm sobel()
    {
        double outer = 1.0;
        double center = 2.0;
        Pgm out = new Pgm(rows, cols, maxval);
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                double gx = 0.0;
                double gy = 0.0;
                gx += center*getZeroWhenOutOfRange(row, col-1);
                gx += outer*getZeroWhenOutOfRange(row+1, col-1);
                gx += outer*getZeroWhenOutOfRange(row-1, col-1);
                gx -= center*getZeroWhenOutOfRange(row, col+1);
                gx -= outer*getZeroWhenOutOfRange(row+1, col+1);
                gx -= outer*getZeroWhenOutOfRange(row-1, col+1);
                gy += center*getZeroWhenOutOfRange(row-1, col);
                gy += outer*getZeroWhenOutOfRange(row-1, col-1);
                gy += outer*getZeroWhenOutOfRange(row-1, col+1);
                gy -= center*getZeroWhenOutOfRange(row+1, col);
                gy -= outer*getZeroWhenOutOfRange(row+1, col-1);
                gy -= outer*getZeroWhenOutOfRange(row+1, col+1);
                double val = Math.sqrt(gx*gx + gy*gy);
                out.set(row, col, (short) val);
            }
        }
        return out;
    }

    public String toString()
    {
        StringBuffer s = new StringBuffer();
        s.append("PGM: ").append(cols).append("x").append(rows);
        return s.toString();
    }
}

