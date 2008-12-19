package is.image.pnm;

import is.image.BilevelImage;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.io.IOException;

public class Pbm
{
    public static final byte BLACK = 0x01;
    public static final byte WHITE = 0x00;

    /*
     * For a PBM the pixel ordering in the byte is most significant bit to 
     * least significant bit. Therefore the first pixel would be bit 0x80.
     */
    private static final int[] BITS =
    {
        0x80,
        0x40,
        0x20,
        0x10,
        0x08,
        0x04,
        0x02,
        0x01,
    };
    private static final byte[] MAGIC = "P4".getBytes();
    private static final byte[] MAGIC_RAW = "P1".getBytes();

    private static boolean isWhiteSpace(int b)
    {
        return b == 0x0A || b == 0x0D || b == 0x20 || b == 0x09;
    }

    private static boolean isDigit(int b)
    {
        return b >= 0x30 && b <= 0x39;
    }

    public static BilevelImage read(String filename)
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

    public static BilevelImage read(InputStream is)
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

        //Skip a single whitespace character
        b = is.read();

        //read data
        byte[][] data = new byte[rows][cols];
        if(raw)
        {
            for(int row=0; row<rows; ++row)
            {
                for(int col=0; col<cols; ++col)
                {
                    data[row][col] = (byte) b;

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
            int bitIndex = 0;
            for(int row=0; row<rows; ++row)
            {
                for(int col=0; col<cols; ++col)
                {
                    data[row][col] = ((b & BITS[bitIndex]) != 0) ? BLACK : WHITE;
                    ++bitIndex;
                    if(bitIndex == 8)
                    {
                        bitIndex = 0;
                        b = is.read();
                    }
                }
                //If there are a few extra bits left over in the row we skip
                //them.
                if(bitIndex != 0)
                {
                    bitIndex = 0;
                    b = is.read();
                }
            }
        }
        return new BilevelImage(data);
        //System.out.println("Finished reading!");
    }

    public static void write(String filename, BilevelImage image)
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

    public static void write(OutputStream os, BilevelImage image)
        throws IOException
    {
        //System.out.println("Writing image...");
        int rows = image.getRows();
        int cols = image.getCols();
        os.write("P4\n".getBytes());
        os.write((cols+" "+rows+"\n").getBytes());

        byte b = 0x00;
        int bitIndex = 0;
        byte[][] data = image.getData();
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
}
