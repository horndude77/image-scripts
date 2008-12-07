package image;

import image.pnm.Pbm;
import image.util.Pair;
import java.util.Stack;

public class BlobRemover
{
    public static void removeBlobs(Pbm image, int minBlobSize)
    {
        int rows = image.getRows();
        int cols = image.getCols();
        int[][] assignments = new int[rows][cols];
        int blobNum = 0;

        //assign pixel to a blob
        System.out.println("Assigning blob numbers...");
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                if(assignments[row][col] == 0 && image.get(row, col) == Pbm.BLACK)
                {
                    ++blobNum;
                    assignBlob(image, assignments, row, col, blobNum);
                }
            }
        }

        //count pixels in each blob
        System.out.println("Calculating blob sizes...");
        int[] blobSizes = new int[blobNum+1];
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                int currBlobNum = assignments[row][col];
                blobSizes[currBlobNum]++;
            }
        }

        //remove small blobs
        System.out.println("Removing small blobs...");
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                int currBlobNum = assignments[row][col];
                if(blobSizes[currBlobNum] < minBlobSize)
                {
                    image.set(row, col, Pbm.WHITE);
                }
            }
        }
    }

    private static void assignBlob(Pbm image, int[][] assignments, int row, int col, int blobNum)
    {
        Stack<Pair<Integer, Integer>> s = new Stack<Pair<Integer, Integer>>();
        s.push(new Pair<Integer, Integer>(row, col));
        int rows = image.getRows();
        int cols = image.getCols();

        while(!s.isEmpty())
        {
            Pair<Integer, Integer> p = s.pop();
            int currRow = p.getFirst();
            int currCol = p.getSecond();

            if(currRow >= 0 && currRow < rows && currCol >= 0 && currCol < cols && assignments[currRow][currCol] == 0 && image.get(currRow, currCol) == Pbm.BLACK)
            {
                assignments[currRow][currCol] = blobNum;
                s.push(new Pair<Integer, Integer>(currRow-1, currCol));
                s.push(new Pair<Integer, Integer>(currRow+1, currCol));
                s.push(new Pair<Integer, Integer>(currRow, currCol-1));
                s.push(new Pair<Integer, Integer>(currRow, currCol+1));
            }
        }
    }
}

