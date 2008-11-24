#!/bin/bash
#Deskews all b&w images in input pdf.
#
#Relies on pagetools to find skew: http://pagetools.sourceforge.net/
#
#Does not work with color images yet.
#

PDF=$1
SCRIPT_DIR=`dirname $0`
PREFIX=prefix

pdfimages $PDF $PREFIX

for i in $PREFIX*.pbm
do
    #clean image, make 10x13
    convert $i -negate -background white -gravity center -extent 3000x3900 $i

    #find skew
    skew=`$SCRIPT_DIR/pbm_findskew $i`
    #skew=`ruby -e "puts -$skew"`
    echo "$i skew: $skew"

    #rotate image
    #convert $i -distort ScaleRotateTranslate "$skew" $i
    $SCRIPT_DIR/pbm_rotate $i $skew rot_$i

    #convert to 300dpi tiff
    convert rot_$i -density 300 -units PixelsPerInch -compress group4 ${i%.pbm}.tiff
done

#create pdf file
OUT_TIFF=${PDF%.pdf}.tiff
OUT_PDF=${PDF%.pdf}_out.pdf
tiffcp $PREFIX*.tiff $OUT_TIFF
tiff2pdf $OUT_TIFF -t ${PDF%.pdf} -z -o $OUT_PDF
rm $PREFIX*.pbm $PREFIX*.tiff rot_$PREFIX.pbm

