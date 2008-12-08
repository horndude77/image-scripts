package image;

import image.pnm.Pbm;
import image.util.Pair;
import java.util.Stack;
import java.util.Set;
import java.util.HashSet;

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
        //Set<Pair<Integer, Integer>> seen = new HashSet<Pair<Integer, Integer>>();
        Stack<Pair<Integer, Integer>> stack = new Stack<Pair<Integer, Integer>>();
        stack.push(new Pair<Integer, Integer>(row, col));
        int rows = image.getRows();
        int cols = image.getCols();

        while(!stack.isEmpty())
        {
            Pair<Integer, Integer> p = stack.pop();
            //seen.add(p);
            int currRow = p.getFirst();
            int currCol = p.getSecond();

            if(currRow >= 0 && currRow < rows && currCol >= 0 && currCol < cols && assignments[currRow][currCol] == 0 && image.get(currRow, currCol) == Pbm.BLACK)
            {
                assignments[currRow][currCol] = blobNum;
                Pair<Integer, Integer> up = new Pair<Integer, Integer>(currRow-1, currCol);
                Pair<Integer, Integer> down = new Pair<Integer, Integer>(currRow+1, currCol);
                Pair<Integer, Integer> left = new Pair<Integer, Integer>(currRow, currCol-1);
                Pair<Integer, Integer> right = new Pair<Integer, Integer>(currRow, currCol+1);
                //if(!seen.contains(up))
                {
                    //seen.add(up);
                    stack.push(up);
                }
                //if(!seen.contains(down))
                {
                    //seen.add(down);
                    stack.push(down);
                }
                //if(!seen.contains(left))
                {
                    //seen.add(left);
                    stack.push(left);
                }
                //if(!seen.contains(right))
                {
                    //seen.add(right);
                    stack.push(right);
                }
            }
        }
    }
}

