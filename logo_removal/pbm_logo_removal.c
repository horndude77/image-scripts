/*
 * Searches through an image (pbm only for now) for another image then makes
 * that section white.
 *
 * Author: Jay Anderson
 *
 * This program comes with ABSOLUTELY NO WARRANTY. Use at your own risk.
 */

#include <pbm.h>

#define INFINITY 0x7fffffff

int score_rect(bit** logo, int logo_rows, int logo_cols, bit** image, int image_rows, int image_cols, int row_zero, int col_zero, int cutoff)
{
    if(row_zero+logo_rows >= image_rows || col_zero+logo_cols >= image_cols)
    {
        return INFINITY;
    }

    int score = 0;
    int row;
    int col;
    for(row=0; row<logo_rows; ++row)
    {
        for(col=0; col<logo_cols; ++col)
        {
            score += (logo[row][col] ^ image[row+row_zero][col+col_zero]);
        }

        if(score > cutoff)
        {
            return INFINITY;
        }
    }
    return score;
}

void blank_rectangle(bit** image, int image_rows, int image_cols, int row_zero, int col_zero, int rows, int cols)
{
    int row, col;
    for(row=0; row<rows && row+row_zero<image_rows; ++row)
    {
        for(col=0; col<cols && col+col_zero<image_cols; ++col)
        {
            image[row+row_zero][col+col_zero] = PBM_WHITE;
        }
    }
}

int main(int argc, char** argv)
{
    if(argc < 3)
    {
        printf("Usage: pbm_logo_removal <image> <logo image> <output filename>\n");
        exit(-1);
    }

    char* logo_filename = argv[2]; //"logo.pbm";
    char* target_filename = argv[1]; //"test.pbm";
    char* output_filename = argv[3]; //"test_out.pbm";
    FILE* file;

    /* load image */
    file = fopen(target_filename, "r");
    int image_rows;
    int image_cols;
    bit** image = pbm_readpbm(file, &image_cols, &image_rows);
    fclose(file);
    printf("image: %dx%d\n", image_rows, image_cols);

    /* load logo */
    file = fopen(logo_filename, "r");
    int logo_rows;
    int logo_cols;
    bit** logo = pbm_readpbm(file, &logo_cols, &logo_rows);
    fclose(file);
    printf("logo: %dx%d\n", logo_rows, logo_cols);

    /* search for logo */
    /* This is stupid and slow, but it might just be good enough. */
    printf("Beginning search...\n");
    int cutoff = (logo_rows*logo_cols)/50; /* Be at least within 2%. */
    int best = cutoff;
    int best_row = -1;
    int best_col = -1;
    int start_row = 0;
    int start_col = 0;

    int row, col, score;
    for(row=start_row; row<start_row+500 && best > 0; ++row)
    {
        for(col=start_col; col<start_col+image_cols && best > 0; ++col)
        {
            score = score_rect(logo, logo_rows, logo_cols, image, image_rows, image_cols, row, col, best);
            if(score < best)
            {
                printf("New best score %d (%d, %d)\n", score, row, col);
                best = score;
                best_row = row;
                best_col = col;
            }
        }
    }
    printf("Search done!\n");

    /* blank logo */
    printf("Blanking logo...\n");
    blank_rectangle(image, image_rows, image_cols, best_row, best_col, logo_rows, logo_cols);

    /* write new image */
    printf("Writing output image...\n");
    file = fopen(output_filename, "w");
    pbm_writepbm(file, image, image_cols, image_rows, 0);
    fclose(file);

    printf("Done!\n");

    pbm_freearray(image, image_rows);
    pbm_freearray(logo, logo_rows);
}

