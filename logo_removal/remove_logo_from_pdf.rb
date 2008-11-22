#!/usr/bin/env ruby

def remove_logo_pbm(image_filename, logo_filename, percentage_close, start_row, rows_to_search, start_col, cols_to_search, blank_method)
    out_filename = image_filename.sub(/.pbm/, 'out.pbm')
    return if(File.exists?(out_filename))
    puts "removing logo from #{image_filename}..."
    out = `#{File.dirname(File.expand_path($0))}/pbm_logo_removal #{image_filename} #{logo_filename} #{out_filename} #{percentage_close} #{start_row} #{rows_to_search} #{start_col} #{cols_to_search} #{blank_method}`
    puts out
end

def remove_logo_ppm(image_filename, logo_filename, percentage_close, start_row, rows_to_search, start_col, cols_to_search, blank_method)
    pbm_filename = image_filename.sub(/ppm$/, 'pbm')
    `convert #{image_filename} #{pbm_filename}`
    remove_logo_pbm(pbm_filename, logo_filename, percentage_close, start_row, rows_to_search, start_col, cols_to_search, blank_method)
end

PBM_LOGO='logo_pbm.pbm'
PPM_LOGO='logo_ppm.pbm'
PREFIX='prefix'

pdf = ARGV[0]
`pdfimages #{pdf} #{PREFIX}`

pbm_files = Dir["#{PREFIX}*.pbm"].sort
pbm_files.each do |image|
    remove_logo_pbm(image, PBM_LOGO, 1.0, 0, 200, 0, -1, 'invert_logo')
end

ppm_files = Dir["#{PREFIX}*.ppm"].sort
ppm_files.each do |image|
    remove_logo_ppm(image, PPM_LOGO, 10.0, 0, 100, 1040, 20, 'blank_rectangle')
end

Dir["#{PREFIX}*out.pbm"].each do |pbm|
    `convert -density 300 -units PixelsPerInch #{pbm} #{pbm.sub(/pbm$/, 'tiff')}`
end

tiff_file = pdf.sub(/\.pdf$/, '.tiff')
out_pdf_file = pdf.sub(/\.pdf$/, 'out.pdf')
`tiffcp #{Dir["#{PREFIX}*out.tiff"].sort.join(' ')} #{tiff_file}`
`tiff2pdf #{tiff_file} -t #{pdf.sub(/\.pdf/, '')} -z -o #{out_pdf_file}`
`rm #{PREFIX}*.pbm #{PREFIX}*.ppm #{PREFIX}*.tiff`

