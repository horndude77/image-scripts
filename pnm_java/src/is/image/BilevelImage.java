package is.image;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;

public class BilevelImage
{
    public static final byte BLACK = 0x01;
    public static final byte WHITE = 0x00;
    public static final int NUM_PROCESSORS = Runtime.getRuntime().availableProcessors();

    private int rows, cols;
    private byte[][] data;

    public BilevelImage(int rows, int cols)
    {
        this.rows = rows;
        this.cols = cols;
        this.data = new byte[rows][cols];
    }

    public BilevelImage(byte[][] data)
    {
        this.rows = data.length;
        this.cols = (rows>0) ? data[0].length : 0;
        this.data = data;
    }

    public void set(int row, int col, byte val)
    {
        data[row][col] = val;
    }

    public void invert(int row, int col)
    {
        if(data[row][col] == BilevelImage.BLACK)
        {
            data[row][col] = BilevelImage.WHITE;
        }
        else
        {
            data[row][col] = BilevelImage.BLACK;
        }
    }

    public byte get(int row, int col)
    {
        return data[row][col];
    }

    public byte getWhiteWhenOutOfRange(int row, int col)
    {
        if(row < 0 || row >= rows || col < 0 || col >= cols)
        {
            return WHITE;
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

    public byte[][] getData()
    {
        return this.data;
    }

    public BilevelImage centerRotate(double angle_degrees)
    {
        return this.rotate(angle_degrees, this.cols/2.0, this.rows/2.0);
    }

    public BilevelImage rotate(final double angle_degrees, final double cx, final double cy)
    {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(NUM_PROCESSORS, NUM_PROCESSORS, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        //System.out.println("Number of processors available: "+NUM_PROCESSORS);

        final BilevelImage rotated = new BilevelImage(rows, cols);

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
                        if(val >= 0.4)
                        {
                            rotated.set(row, col, BLACK);
                        }
                        else
                        {
                            rotated.set(row, col, WHITE);
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
        //System.out.println("Finished rotating!");
        return rotated;
    }

    public String toString()
    {
        StringBuffer s = new StringBuffer();
        s.append("Bilevel Image: ").append(cols).append("x").append(rows);
        /*
        s.append("\n");
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                s.append(data[row][col]);
            }
            if(row != (rows-1))
            {
                s.append("\n");
            }
        }
        */
        return s.toString();
    }
}

