#!/usr/bin/env ruby

PBM_LOGO='logos/logo_pbm1.pbm'
PBM_PERCENTAGE_CLOSE=25.0
PBM_ROW_START=0 #start on first non blank row
PBM_ROWS_TO_SEARCH=50
PBM_COL_START=0
PBM_COLS_TO_SEARCH=-1 #-1 here means to search all columns.
PBM_REMOVAL_METHOD='invert_logo'

PPM_LOGO='logos/logo_ppm1.pbm'
PPM_PERCENTAGE_CLOSE=25.0
PPM_ROW_START=0 #start on first non blank row
PPM_ROWS_TO_SEARCH=5
PPM_COL_START=1000
PPM_COLS_TO_SEARCH=600
#PPM_REMOVAL_METHOD='blank_rectangle'
PPM_REMOVAL_METHOD='invert_logo'

def remove_logo_pbm(image_filename, logo_filename, percentage_close, start_row, rows_to_search, start_col, cols_to_search, blank_method)
    out_filename = image_filename.sub(/.pbm/, 'out.pbm')
    return if(File.exists?(out_filename))
    puts "removing logo from #{image_filename}..."
    puts "#{File.dirname(File.expand_path($0))}/sub_image_remove #{image_filename} #{logo_filename} #{out_filename} #{percentage_close} #{start_row} #{rows_to_search} #{start_col} #{cols_to_search} #{blank_method}"
    out = `#{File.dirname(File.expand_path($0))}/sub_image_remove #{image_filename} #{logo_filename} #{out_filename} #{percentage_close} #{start_row} #{rows_to_search} #{start_col} #{cols_to_search} #{blank_method}`
    puts out
end

def remove_logo_ppm(image_filename, logo_filename, percentage_close, start_row, rows_to_search, start_col, cols_to_search, blank_method)
    pbm_filename = image_filename.sub(/ppm$/, 'pbm')
    `convert #{image_filename} #{pbm_filename}`
    remove_logo_pbm(pbm_filename, logo_filename, percentage_close, start_row, rows_to_search, start_col, cols_to_search, blank_method)
end

PREFIX='prefix'

pdf = ARGV[0]
`pdfimages "#{pdf}" #{PREFIX}`

pbm_files = Dir["#{PREFIX}*.pbm"].sort
pbm_files.each do |image|
    remove_logo_pbm(image, PBM_LOGO, PBM_PERCENTAGE_CLOSE, PBM_ROW_START, PBM_ROWS_TO_SEARCH, PBM_COL_START, PBM_COLS_TO_SEARCH, PBM_REMOVAL_METHOD)
end

ppm_files = Dir["#{PREFIX}*.ppm"].sort
ppm_files.each do |image|
    remove_logo_ppm(image, PPM_LOGO, PPM_PERCENTAGE_CLOSE, PPM_ROW_START, PPM_ROWS_TO_SEARCH, PPM_COL_START, PPM_COLS_TO_SEARCH, PPM_REMOVAL_METHOD)
end

Dir["#{PREFIX}*out.pbm"].each do |pbm|
    `convert -density 300 -units PixelsPerInch "#{pbm}" "#{pbm.sub(/pbm$/, 'tiff')}"`
end

tiff_file = pdf.sub(/\.pdf$/, '.tiff')
out_pdf_file = pdf.sub(/\.pdf$/, 'out.pdf')
`tiffcp #{Dir["#{PREFIX}*out.tiff"].sort.join(' ')} "#{tiff_file}"`
`tiff2pdf "#{tiff_file}" -t "#{pdf.sub(/\.pdf/, '')}" -z -o "#{out_pdf_file}"`
`rm #{PREFIX}*.pbm #{PREFIX}*.ppm #{PREFIX}*.tiff "#{tiff_file}"`

