#!/usr/bin/env ruby -w

proj_root = File.expand_path(File.dirname(__FILE__))

checkout_dirs = Dir['checkouts/*'].map{ |sub_dir| proj_root + "/" + sub_dir}

puts "\033[31mPulling in submodules if necessary\033[0m"
`cd #{proj_root} ; git submodule init ; git submodule update`

puts "\033[31mPulling dependencies for project root (#{proj_root})...\033[0m"
`cd #{proj_root} ; lein deps`
if `cd #{proj_root} ; lein help`.match(/native-deps/)
  `cd #{proj_root} ; lein native-deps`
end

puts "\033[31mPulling and merging dependencies for checkouts:\033[0m"
checkout_dirs.each do |dir|
  puts "  - for #{dir}"
  `cd #{dir} ; lein deps`
  if `cd #{dir} ; lein help`.match(/native-deps/)
    `cd #{dir} ; lein native-deps`
  end
  `cd #{dir} ; cp -R #{dir}/lib/* #{proj_root}/lib`
  if(File.exists?("#{dir}/native"))
    `cp -R #{dir}/native #{proj_root}`
  end
end
