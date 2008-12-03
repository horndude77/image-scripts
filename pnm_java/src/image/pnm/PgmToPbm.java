package image.pnm;

public class PgmToPbm
{
    public static void main(String[] args)
        throws Exception
    {
        if(args.length < 2)
        {
            System.out.println("Usage: image.pnm.PgmToPbm <input pgm> <output pbm>");
            System.exit(-1);
        }

        String inputPgm = args[0];
        String outputPbm = args[1];

        Pgm in = new Pgm(inputPgm);
        Pbm out = in.toPbm();
        out.clean();
        out.write(outputPbm);
    }
}

