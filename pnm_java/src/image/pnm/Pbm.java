package image.pnm;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;

public class Pbm
{
    public static final byte BLACK = 0x01;
    public static final byte WHITE = 0x00;
    public static final int NUM_PROCESSORS = Runtime.getRuntime().availableProcessors();

    private int rows, cols;
    private byte[][] data;

    /*
     * For a PBM the pixel ordering in the byte is most significant bit to 
     * least significant bit. Therefore the first pixel would be bit 0x80.
     */
    private static final byte[] BITS =
    {
        (byte) 0x80,
        (byte) 0x40,
        (byte) 0x20,
        (byte) 0x10,
        (byte) 0x08,
        (byte) 0x04,
        (byte) 0x02,
        (byte) 0x01,
    };
    private static final byte[] MAGIC = "P4".getBytes();
    private static final byte[] MAGIC_RAW = "P1".getBytes();

    public Pbm(int rows, int cols)
    {
        this.rows = rows;
        this.cols = cols;
        this.data = new byte[rows][cols];
    }

    public Pbm(InputStream is)
        throws IOException
    {
        read(is);
    }

    public Pbm(String filename)
        throws IOException
    {
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(filename);
            this.read(fis);
        }
        finally
        {
            if(fis != null)
            {
                fis.close();
            }
        }
    }

    public void set(int row, int col, byte val)
    {
        data[row][col] = val;
    }

    public byte get(int row, int col)
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

    private boolean isWhiteSpace(byte b)
    {
        return b == 0x0A || b == 0x0D || b == 0x20 || b == 0x09;
    }

    private boolean isDigit(byte b)
    {
        return b >= 0x30 && b <= 0x39;
    }

    private void read(InputStream is)
        throws IOException
    {
        //System.out.println("Reading image...");
        boolean raw;
        //Do my own buffering.
        int buffer_size = 1024;
        byte[] buffer = new byte[buffer_size];
        int index = 0;
        int read_size = is.read(buffer);
        if(read_size < 2)
        {
            throw new IOException("File ended early");
        }
        else
        {
            index = 2;
        }

        if(buffer[0] != MAGIC[0])
        {
            throw new IOException("Bad magic number: " + (char) buffer[0] + "" + (char) buffer[1]);
        }

        if(buffer[1] == MAGIC[1])
        {
            raw = false;
        }
        else if(buffer[1] == MAGIC_RAW[1])
        {
            raw = true;
        }
        else
        {
            throw new IOException("Bad magic number");
        }

        //skip whitespace
        //TODO: I'm doing this a lot. Fix it! Really I just need a buffered
        //input stream that lets me go back one character.
        while(isWhiteSpace(buffer[index]))
        {
            ++index;
            if(index == read_size)
            {
                index = 0;
                read_size = is.read(buffer);
            }
        }

        //read width
        StringBuffer sb = new StringBuffer();
        while(isDigit(buffer[index]))
        {
            sb.append((char) buffer[index]);
            ++index;
            if(index == read_size)
            {
                index = 0;
                read_size = is.read(buffer);
            }
        }
        cols = Integer.parseInt(sb.toString());

        //skip whitespace
        while(isWhiteSpace(buffer[index]))
        {
            ++index;
            if(index == read_size)
            {
                index = 0;
                read_size = is.read(buffer);
            }
        }

        //read height
        sb = new StringBuffer();
        while(isDigit(buffer[index]))
        {
            sb.append((char) buffer[index]);
            ++index;
            if(index == read_size)
            {
                index = 0;
                read_size = is.read(buffer);
            }
        }
        rows = Integer.parseInt(sb.toString());

        //Skip a single whitespace character
        ++index;
        if(index == read_size)
        {
            index = 0;
            read_size = is.read(buffer);
        }

        //read data
        if(raw)
        {
            for(int row=0; row<rows; ++row)
            {
                for(int col=0; col<cols; ++col)
                {
                    data[row][col] = buffer[index];
                    ++index;
                    if(index == read_size)
                    {
                        index = 0;
                        read_size = is.read(buffer);
                    }

                    //skip whitespace
                    while(isWhiteSpace(buffer[index]))
                    {
                        ++index;
                        if(index == read_size)
                        {
                            index = 0;
                            read_size = is.read(buffer);
                        }
                    }
                }
            }
        }
        else
        {
            int bitIndex = 0;
            data = new byte[rows][cols];
            for(int row=0; row<rows; ++row)
            {
                for(int col=0; col<cols; ++col)
                {
                    data[row][col] = ((buffer[index] & BITS[bitIndex]) != 0) ? BLACK : WHITE;
                    ++bitIndex;
                    if(bitIndex == 8)
                    {
                        bitIndex = 0;
                        ++index;
                        if(index == read_size)
                        {
                            index = 0;
                            read_size = is.read(buffer);
                        }
                    }
                }
                bitIndex = 0;
                ++index;
                if(index == read_size)
                {
                    index = 0;
                    read_size = is.read(buffer);
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
        os.write("P4\n".getBytes());
        os.write((cols+" "+rows+"\n").getBytes());

        byte b = 0x00;
        int bitIndex = 0;
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                if(data[row][col] == BLACK)
                {
                    b |= BITS[bitIndex];
                }
                ++bitIndex;
                if(bitIndex == 8)
                {
                    os.write(b);
                    b = 0x00;
                    bitIndex = 0;
                }
            }
            //Fill out last byte in the row if needed.
            if(bitIndex > 0)
            {
                os.write(b);
                b = 0x00;
                bitIndex = 0;
            }
        }
        //System.out.println("Finished writing!");
    }

    public Pbm centerRotate(double angle_degrees)
    {
        return this.rotate(angle_degrees, this.cols/2.0, this.rows/2.0);
    }

    public Pbm rotate(double angle_degrees, double cx, double cy)
    {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(NUM_PROCESSORS, NUM_PROCESSORS, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        //System.out.println("Number of processors available: "+NUM_PROCESSORS);

        final Pbm rotated = new Pbm(rows, cols);

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
                        double rx = col*cosa - row*sina;
                        double ry = col*sina + row*cosa;
                        //integer location in original
                        int x = (int) rx;
                        int y = (int) ry;
                        //weights to use for picking value
                        double wx = 1.0 - (rx - x);
                        double wy = 1.0 - (ry - y);

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
        s.append("PBM: "+cols+"x"+rows+"\n");
        /*
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                s.append(data[row][col]);
            }
            s.append("\n");
        }
        */
        return s.toString();
    }

    public static void main(String[] args)
        throws Exception
    {
        int rows = 1000;
        int cols = 500;
        Pbm img = new Pbm(rows, cols);
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                if(row == col)
                {
                    img.set(row, col, Pbm.BLACK);
                }
                if(row == rows/2)
                {
                    img.set(row, col, Pbm.BLACK);
                }
                else
                {
                    //img.set(row, col, Pbm.BLACK);
                }
            }
        }
        System.out.println(img);
        System.out.println(img);
        FileOutputStream fos = new FileOutputStream("test.pbm");
        img.write(fos);

        img = img.rotate(10.0, 0.0, 0.0);
        System.out.println(img);
        fos = new FileOutputStream("test_rot.pbm");
        img.write(fos);
        fos.close();
    }
}

