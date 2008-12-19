package is.image;

import is.image.BilevelImage;
import is.image.threshold.Thresholder;
import is.util.ConcurrentUtil;

import java.util.concurrent.ThreadPoolExecutor;

public class GrayscaleImage
    implements Comparable<GrayscaleImage>
{
    private int rows, cols;
    short maxval;
    private short[][] data;

    public GrayscaleImage(int rows, int cols, short maxval)
    {
        this.rows = rows;
        this.cols = cols;
        this.maxval = maxval;
        this.data = new short[rows][cols];
    }

    public GrayscaleImage(short[][] data, short maxval)
    {
        this.rows = data.length;
        this.cols = data[0].length;
        this.maxval = maxval;
        this.data = data;
    }

    public BilevelImage toBilevelImage(Thresholder thresholder)
    {
        return toBilevelImage(thresholder, false, 0.0);
    }

    public BilevelImage toBilevelImage(Thresholder thresholder, boolean postprocess, double energyThreshold)
    {
        BilevelImage out = thresholder.threshold(this);
        if(postprocess)
        {
            postProcess(out, energyThreshold);
        }
        return out;
    }

    public void postProcess(BilevelImage out, double energyThreshold)
    {
        System.out.println("Post-process...");
        GrayscaleImage sobel = this.sobel();
        boolean backup = false;
        for(int row=0; row<rows;++row)
        {
            for(int col=0; col<cols; ++col)
            {
                byte pixelInverse = out.get(row, col) == BilevelImage.BLACK ? BilevelImage.WHITE : BilevelImage.BLACK;
                int countThreshold = pixelInverse == BilevelImage.BLACK ? 5 : 4;
                //if(out.get(row, col) == BilevelImage.BLACK)
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
                            if(out.get(irow, icol) == pixelInverse)
                            {
                                count += 1;
                            }
                        }
                    }
                    if(count >= countThreshold && sobel.get(row, col) < energyThreshold)
                    {
                        out.set(row, col, pixelInverse);
                        backup = true;
                        //System.out.println("Changed pixel: ("+row+", "+col+")");
                        //if(row > 0) --row;
                        if(col > 0) col-=2;
                    }
                }
            }
            if(backup)
            {
                //System.out.println("Backing Up (row: "+row+")");
                if(row > 0) row-=2;
                backup = false;
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

    public short getMaxvalWhenOutOfRange(int row, int col)
    {
        if(row < 0 || row >= rows || col < 0 || col >= cols)
        {
            return maxval;
        }
        else
        {
            return data[row][col];
        }
    }

    public short getClosestWhenOutOfRange(int row, int col)
    {
        if(row < 0)
        {
            row = 0;
        }
        else if(row >= rows)
        {
            row = rows-1;
        }
        if(col < 0)
        {
            col = 0;
        }
        else if(col >= cols)
        {
            col = cols-1;
        }
        return data[row][col];
    }

    public double getLinearInterpolation(double ry, double rx)
    {
        int x = (int) Math.floor(rx);
        int y = (int) Math.floor(ry);
        //weights to use for picking value
        double wx = 1.0 - Math.abs(rx - x);
        double wy = 1.0 - Math.abs(ry - y);

        double val = 0.0;
        val += wx*wy*getMaxvalWhenOutOfRange(y, x);
        val += (1.0-wx)*wy*getMaxvalWhenOutOfRange(y, x+1);
        val += wx*(1.0-wy)*getMaxvalWhenOutOfRange(y+1, x);
        val += (1.0-wx)*(1.0-wy)*getMaxvalWhenOutOfRange(y+1, x+1);
        return Math.min(val, maxval);
    }

    public double getBicubicInterpolation(double ry, double rx)
    {
        int x = (int) Math.floor(rx);
        int y = (int) Math.floor(ry);

        double a = -0.75;
        double epislon = 0.00001;

        double dx1 = Math.abs(rx - x);
        double dx1p = 1.0-dx1;
        double dx2 = dx1+1.0;
        double dx2p = 3.0-dx2;
        double dy1 = Math.abs(ry - y);
        double dy1p = 1.0-dy1;
        double dy2 = dy1+1.0;
        double dy2p = 3.0-dy2;

        dx1 = dx1 < epislon ? 1.0 : dx1;
        dx1p = dx1p < epislon ? 1.0 : dx1p;
        dy1 = dy1 < epislon ? 1.0 : dy1;
        dy1p = dy1p < epislon ? 1.0 : dy1p;

        double wx1 = ((a+2.0)*dx1 - (a+3.0))*dx1*dx1 + 1.0;
        double wx1p = ((a+2.0)*dx1p - (a+3.0))*dx1p*dx1p + 1.0;
        double wx2 = a*(((dx2 - 5.0)*dx2 + 8.0)*dx2 - 4.0);
        double wx2p = a*(((dx2p - 5.0)*dx2p + 8.0)*dx2p - 4.0);
        double wy1 = ((a+2.0)*dy1 - (a+3.0))*dy1*dy1 + 1.0;
        double wy1p = ((a+2.0)*dy1p - (a+3.0))*dy1p*dy1p + 1.0;
        double wy2 = a*(((dy2 - 5.0)*dy2 + 8.0)*dy2 - 4.0);
        double wy2p = a*(((dy2p - 5.0)*dy2p + 8.0)*dy2p - 4.0);

        double val = 0.0;
        //center
        val += wx1*wy1*getMaxvalWhenOutOfRange(y, x);
        val += wx1p*wy1*getMaxvalWhenOutOfRange(y, x+1);
        val += wx1*wy1p*getMaxvalWhenOutOfRange(y+1, x);
        val += wx1p*wy1p*getMaxvalWhenOutOfRange(y+1, x+1);

        //corners
        val += wx2*wy2*getMaxvalWhenOutOfRange(y-1, x-1);
        val += wx2p*wy2*getMaxvalWhenOutOfRange(y-1, x+2);
        val += wx2*wy2p*getMaxvalWhenOutOfRange(y+2, x-1);
        val += wx2p*wy2p*getMaxvalWhenOutOfRange(y+2, x+2);

        //outer x middle y
        val += wx2*wy1*getMaxvalWhenOutOfRange(y, x-1);
        val += wx2p*wy1*getMaxvalWhenOutOfRange(y, x+2);
        val += wx2*wy1p*getMaxvalWhenOutOfRange(y+1, x-1);
        val += wx2p*wy1p*getMaxvalWhenOutOfRange(y+1, x+2);

        //outer y middle x
        val += wx1*wy2*getMaxvalWhenOutOfRange(y-1, x);
        val += wx1p*wy2*getMaxvalWhenOutOfRange(y-1, x+1);
        val += wx1*wy2p*getMaxvalWhenOutOfRange(y+2, x);
        val += wx1p*wy2p*getMaxvalWhenOutOfRange(y+2, x+1);

        return Math.min(val, maxval);
    }

    public int getRows()
    {
        return this.rows;
    }

    public int getCols()
    {
        return this.cols;
    }

    public short getMaxval()
    {
        return this.maxval;
    }

    public short[][] getData()
    {
        return this.data;
    }

    public GrayscaleImage centerRotate(double angle_degrees)
    {
        return this.rotate(angle_degrees, this.cols/2.0, this.rows/2.0);
    }

    public GrayscaleImage rotate(final double angle_degrees, final double cx, final double cy)
    {
        ThreadPoolExecutor pool = ConcurrentUtil.createThreadPool();

        final GrayscaleImage rotated = new GrayscaleImage(rows, cols, maxval);
        final short defaultBackground = maxval;

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
                        double rx = ((col-cx)*cosa - (row-cy)*sina) + cx;
                        double ry = ((col-cx)*sina + (row-cy)*cosa) + cy;
                        double val = getLinearInterpolation(ry, rx);

                        rotated.set(row, col, (short) val);
                    }
                }
            });
        }
        ConcurrentUtil.shutdownPoolAndAwaitTermination(pool);
        //System.out.println("Finished rotating!");
        return rotated;
    }

    public GrayscaleImage sobel()
    {
        double outer = 1.0;
        double center = 2.0;
        GrayscaleImage out = new GrayscaleImage(rows, cols, maxval);
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

    public int compareTo(GrayscaleImage other)
    {
        //compare based on size
        int size = rows * cols;
        int otherSize = other.rows * other.cols;
        if(size < otherSize)
        {
            return -1;
        }
        else if(size > otherSize)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    public String toString()
    {
        StringBuffer s = new StringBuffer();
        s.append("Grayscale Image: ").append(cols).append("x").append(rows);
        return s.toString();
    }
}

