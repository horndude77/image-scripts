package image;

import image.pnm.*;

public class Main
{
    public static void main(String[] args)
    {
        try
        {
            int index = 0;
            String inputFilename;
            String outputFilename;
            boolean verbose = false;

            Thresholder thresholder = null;
            boolean postprocess = false;
            double energyThreshold = 100.0;

            boolean deskew = false;
            boolean rotate = false;
            double rotation = 0.0;

            boolean subimageRemoval = false;
            String subimageFilename = null;
            int startRow = 0;
            int rowsToSearch = -1;
            int startCol = 0;
            int colsToSearch = -1;
            double cutoffPercentage = -1.0;
            String removalMethod = "blank_rectangle";

            inputFilename = args[index++];
            outputFilename = args[index++];
            for(;index < args.length; ++index)
            {
                if("-verbose".equals(args[index]))
                {
                    //TODO: make this do something.
                    verbose = true;
                }
                else if("-rotate".equals(args[index]))
                {
                    rotate = true;
                    ++index;
                    rotation = Double.parseDouble(args[index]);
                }
                else if("-deskew".equals(args[index]))
                {
                    deskew = true;
                }
                else if("-bilevel".equals(args[index]))
                {
                    ++index;
                    if("global".equals(args[index]))
                    {
                        ++index;
                        double threshold = Double.parseDouble(args[index]);
                        thresholder = new GlobalThresholder(threshold);
                    }
                    else if("bernsen".equals(args[index]))
                    {
                        ++index;
                        int neighborhood = Integer.parseInt(args[index]);
                        ++index;
                        int l = Integer.parseInt(args[index]);
                        thresholder = new BernsenThresholder(neighborhood, l);
                    }
                    else if("niblack".equals(args[index]))
                    {
                        ++index;
                        int neighborhood = Integer.parseInt(args[index]);
                        ++index;
                        double k = Double.parseDouble(args[index]);
                        thresholder = new NiblackThresholder(neighborhood, k);
                    }
                    else if("runningaverage".equals(args[index]))
                    {
                        ++index;
                        int averageLength = Integer.parseInt(args[index]);
                        ++index;
                        double percentageOfThreshold = Double.parseDouble(args[index]);
                        thresholder = new RunningAverageThresholder(averageLength, percentageOfThreshold);
                    }
                    else
                    {
                        System.out.println("Unknown binarization method: "+args[index]+".");
                        usage();
                    }
                }
                else if("-postprocess".equals(args[index]))
                {
                    ++index;
                    postprocess = true;
                    energyThreshold = Double.parseDouble(args[index]);
                }
                else if("-remove_sub_image".equals(args[index]))
                {
                    ++index;
                    subimageRemoval = true;
                    subimageFilename = args[index];
                    ++index;
                    cutoffPercentage = Double.parseDouble(args[index]);
                    ++index;
                    removalMethod = args[index];
                    if("-rows".equals(args[index]))
                    {
                        ++index;
                        startRow = Integer.parseInt(args[index]);
                        ++index;
                        rowsToSearch = Integer.parseInt(args[index]);
                    }
                    if("-cols".equals(args[index]))
                    {
                        ++index;
                        startCol = Integer.parseInt(args[index]);
                        ++index;
                        colsToSearch = Integer.parseInt(args[index]);
                    }
                }
            }

            //Perform operations
            if(inputFilename.matches(".*\\.pbm$"))
            {
                Pbm image = null;
                try
                {
                    image = new Pbm(inputFilename);
                }
                catch(java.io.IOException e)
                {
                    e.printStackTrace();
                    System.err.println("Unable to read file: "+inputFilename);
                    System.exit(-1);
                }

                if(!outputFilename.matches(".*\\.pbm$"))
                {
                    outputFilename += ".pbm";
                }

                if(deskew)
                {
                    double angleDegrees = FindSkew.findSkew(image);
                    image = image.centerRotate(-angleDegrees);
                }
                if(rotate)
                {
                    image = image.centerRotate(rotation);
                }
                if(subimageRemoval)
                {
                    Pbm sub = null;
                    try
                    {
                        sub = new Pbm(subimageFilename);
                    }
                    catch(java.io.IOException e)
                    {
                        e.printStackTrace();
                        System.err.println("Unable to read file: "+subimageFilename);
                        System.exit(-1);
                    }

                    rowsToSearch = (rowsToSearch == -1) ? image.getRows() : rowsToSearch;
                    colsToSearch = (colsToSearch == -1) ? image.getCols() : colsToSearch;

                    int cutoff = (int) ( (cutoffPercentage/100.0) * sub.getRows() * sub.getCols());
                    int[] pos = SubImageRemoval.findImage(image, sub, startRow, rowsToSearch, startCol, colsToSearch, cutoff);
                    if("invert_logo".equals(removalMethod))
                    {
                        SubImageRemoval.invertSubImage(image, sub, pos[0], pos[1]);
                    }
                    else if("blank_rectangle".equals(removalMethod))
                    {
                        SubImageRemoval.blankRectangle(image, pos[0], pos[1], sub.getRows(), sub.getCols());
                    }
                }

                try
                {
                    image.write(outputFilename);
                }
                catch(java.io.IOException e)
                {
                    e.printStackTrace();
                    System.err.println("Unable to write file: "+outputFilename);
                    System.exit(-1);
                }
            }
            else if(inputFilename.matches(".*\\.pgm$"))
            {
                Pbm output = null;
                Pgm image = null;
                try
                {
                    image = new Pgm(inputFilename);
                }
                catch(java.io.IOException e)
                {
                    e.printStackTrace();
                    System.err.println("Unable to read file: "+inputFilename);
                    System.exit(-1);
                }

                if(thresholder == null && !outputFilename.matches(".*\\.pgm$"))
                {
                    outputFilename += ".pgm";
                }
                if(deskew)
                {
                    Pbm bwImage = image.toPbm(new GlobalThresholder(0.45));
                    double angleDegrees = FindSkew.findSkew(bwImage);
                    image = image.centerRotate(-angleDegrees);
                }
                if(rotate)
                {
                    image = image.centerRotate(rotation);
                }
                if(thresholder != null)
                {
                    output = image.toPbm(thresholder, postprocess, energyThreshold);
                }

                try
                {
                    if(output != null)
                    {
                        output.write(outputFilename);
                    }
                    else
                    {
                        image.write(outputFilename);
                    }
                }
                catch(java.io.IOException e)
                {
                    e.printStackTrace();
                    System.err.println("Unable to write file: "+outputFilename);
                    System.exit(-1);
                }
            }
        }
        catch(IndexOutOfBoundsException e)
        {
            e.printStackTrace();
            usage();
        }
    }

    public static void usage()
    {
        System.out.println("Usage: java image.Main <input filename> <output filename> [options]");
        System.out.println("Options:");
        System.out.println("\t-verbose");
        System.out.println("\t-rotate <degrees>");
        System.out.println("\t-deskew");
        System.out.println("\t-bilevel <method> <method args>");
        System.out.println("\t\tglobal <threshold>");
        System.out.println("\t\tbernsen <neighborhood size (odd number)> <l>");
        System.out.println("\t\tniblack <neighborhood size (odd number)> <k>");
        System.out.println("\t\trunningaverage <running average length>");
        System.out.println("\t\t-postprocess <threshold 0-255>");
        System.out.println("\t-remove_sub_image <sub image filename> <cutoff percentage> <removal method>");
        System.out.println("\t\t-rows <start row> <rows to search>");
        System.out.println("\t\t-cols <start column> <columns to search>");
    }
}
