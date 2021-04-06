# sigParser
method signature parser for courcework
Parses classfiles into csv tabels of fields and methods signatures


## Usage
java -cp [path_to_bin] ru.vafilonov.signatureParser <flags> classpath  

flags:  
  -o <outdir> - directory to save csv files  
  -s <separator sequence> - declare separator for csv, ";" by default  
  
Compiled sigParser can be found in bin/
