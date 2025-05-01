# pdfsplit-forkjoinpool

Testing ForkJoinPool class with help of ChatGPT to create a JavaFX utility which accepts a pdf file input, and output splitted pdf pages in a new folder.


How to run:
1. Package the jar with mvn clean package
2. Download JavaFX sdk from https://gluonhq.com/products/javafx/ and unzip it somewhere like C:\Users\USER\Downloads\
3. Run java --module-path "C:\Users\USER\Downloads\openjfx-21.0.7_windows-x64_bin-sdk\javafx-sdk-21.0.7\lib" --add-modules javafx.controls -jar pdf-splitter-1.0-SNAPSHOT.jar
