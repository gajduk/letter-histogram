Letter Histogram Demo
========================

Calculates and plots the histogram of individual letter frequencies in a text file.


Everything is implemented in native Java (including the plotting).
This little program uses the new AsynchronousFileChannel from NIO 2 to read super large files in chunks and draw the histogram in small increments.

---

How to use?

---

Click on choose file to open a file browse dialog, then select a text file.
Wait a minute for the file to load properly
The bar colors correspond to letter frequency:
    - red most frequent 
	- blue least frequent\n"+

    
Some sample images from the demo:


![alt tag](https://raw.githubusercontent.com/gajduk/letter-histogram/master/hist1.PNG)


![alt tag](https://raw.githubusercontent.com/gajduk/letter-histogram/master/hist2.PNG)
