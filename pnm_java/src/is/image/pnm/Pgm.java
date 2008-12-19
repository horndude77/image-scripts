package is.image.pnm;

import is.image.BilevelImage;
import is.image.GrayscaleImage;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.io.IOException;

public class Pgm
{
    private static final byte[] MAGIC = "P5".getBytes();
    private static final byte[] MAGIC_RAW = "P2".getBytes();

    public static GrayscaleImage read(String filename)
        throws IOException
    {
        InputStream is = null;
        try
        {
            is = new BufferedInputStream(new FileInputStream(filename));
            return read(is);
        }
        finally
        {
            if(is != null)
            {
                is.close();
            }
        }
    }

    private static boolean isWhiteSpace(int b)
    {
        return b == 0x0A || b == 0x0D || b == 0x20 || b == 0x09;
    }

    private static boolean isDigit(int b)
    {
        return b >= 0x30 && b <= 0x39;
    }

    public static GrayscaleImage read(InputStream is)
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
        int cols = Integer.parseInt(sb.toString());

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
        int rows = Integer.parseInt(sb.toString());

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
        short maxval = Short.parseShort(sb.toString());

        //Skip a single whitespace character

        //read data
        short[][] data = new short[rows][cols];
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
        return new GrayscaleImage(data, maxval);
        //System.out.println("Finished reading!");
    }

    public static void write(String filename, GrayscaleImage image)
        throws IOException
    {
        OutputStream os = null;
        try
        {
            os = new BufferedOutputStream(new FileOutputStream(filename));
            write(os, image);
        }
        finally
        {
            if(os != null)
            {
                os.close();
            }
        }
    }

    public static void write(OutputStream os, GrayscaleImage image)
        throws IOException
    {
        //System.out.println("Writing image...");
        int rows = image.getRows();
        int cols = image.getCols();
        short maxval = image.getMaxval();
        short[][] data = image.getData();
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
}
