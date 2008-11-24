/*
 * Searches through an image (pbm only for now) for another image then makes
 * that section white.
 *
 * Author: Jay Anderson
 *
 * This program comes with ABSOLUTELY NO WARRANTY. Use at your own risk.
 */

#include <pbm.h>
#include <math.h>

#define INFINITY 0x7fffffff

void rotate(bit** image, int image_rows, int image_cols, bit** output, double angle_degrees, double center_x, double center_y)
{
    double angle_radians = angle_degrees*(M_PI/180.0);
    double sin_a = sin(angle_radians);
    double cos_a = cos(angle_radians);

    int row, col;
    /* centered image coordinates */
    double xr, yr;
    /* double indices of target pixel */
    double xi_d, yi_d;
    /* integer indices of target pixel */
    int xi, yi;
    /*
     * weights target pixel. Use the 4 surrounding pixels to get the intensity
     * of the rotated pixel.
     */
    double wx, wy;
    /* intensity of new target pixel */
    double val;
    for(row=0; row<image_rows; ++row)
    {
        for(col=0; col<image_cols; ++col)
        {
            xr = col-center_x;
            yr = row-center_y;
            xi_d = xr*cos_a - yr*sin_a;
            yi_d = xr*sin_a + yr*cos_a;
            xi = floor(xi_d);
            yi = floor(yi_d);
            wx = 1.0 - (xi_d-xi);
            wy = 1.0 - (yi_d-yi);
            val = 0.0;

            if(yi >= 0 && xi >= 0 && yi < image_rows && xi < image_cols)
            {
                val += wx*wy*(1.0 - image[yi][xi]);
            }
            else
            {
                /* Pixels outside the frame are defaulted to white. */
                val += wx*wy;
            }

            if(yi >= 0 && (xi+1) >= 0 && yi < image_rows && (xi+1) < image_cols)
            {
                val += (1.0-wx)*wy*(1.0 - image[yi][xi+1]);
            }
            else
            {
                val += (1.0-wx)*wy;
            }

            if((yi+1) >= 0 && xi >= 0 && (yi+1) < image_rows && xi < image_cols)
            {
                val += wx*(1.0-wy)*(1.0 - image[yi+1][xi]);
            }
            else
            {
                val += wx*(1.0-wy);
            }

            if((yi+1) >= 0 && (xi+1) >= 0 && (yi+1) < image_rows && (xi+1) < image_cols)
            {
                val += (1.0-wx)*(1.0-wy)*(1.0 - image[yi+1][xi+1]);
            }
            else
            {
                val += (1.0-wx)*(1.0-wy);
            }

            /* set value */
            if(val >= 0.5)
            {
                output[row][col] = PBM_WHITE;
            }
            else
            {
                output[row][col] = PBM_BLACK;
            }
        }
    }
}

int main(int argc, char** argv)
{
    if(argc < 3)
    {
        printf("Usage: pbm_logo_removal <image> <logo image> <output filename> <start row> <number of rows to search> <start col> <number of columns to search>\n");
        exit(-1);
    }

    /* Ugh! Too many arguments. */
    char* target_filename = argv[1];
    double rotation_degrees = atof(argv[2]);
    char* output_filename = argv[3];
    FILE* file;

    /* load image */
    file = fopen(target_filename, "r");
    int image_rows;
    int image_cols;
    bit** image = pbm_readpbm(file, &image_cols, &image_rows);
    fclose(file);

    /* perform rotation */
    bit** rotated_image = pbm_allocarray(image_cols, image_rows);
    rotate(image, image_rows, image_cols, rotated_image, rotation_degrees, 0.0, 0.0);

    /* write new image */
    printf("Writing output image...\n");
    file = fopen(output_filename, "w");
    pbm_writepbm(file, rotated_image, image_cols, image_rows, 0);
    fclose(file);

    printf("Done!\n");

    pbm_freearray(image, image_rows);
    pbm_freearray(rotated_image, image_rows);
}

