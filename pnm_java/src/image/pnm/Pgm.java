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
        //Simple thresholding
        int threshold = (int) (maxval * 0.45);
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

    public void set(int row, int col, short val)
    {
        data[row][col] = val;
    }

    public short get(int row, int col)
    {
        return data[row][col];
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

    public String toString()
    {
        StringBuffer s = new StringBuffer();
        s.append("PGM: ").append(cols).append("x").append(rows);
        return s.toString();
    }
}

