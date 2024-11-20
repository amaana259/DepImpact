**DEPIMPACT (Week 6/Week 3):** 

---

- Last week, I was having difficulty understanding how to run the tool for .dot files (instead of .txt files), as my runs resulted in JVM Heap Memory errors.

- This week, I continued work on my VM image. I was able to compile the results for most of the datasets with .dot files.

- Compiled a Sheet with all of the results and comparison: [Work](https://docs.google.com/spreadsheets/d/1E0TT0AYXQ6it_D7pTOyLQqKlCkcLaQbQxC85G6OtyTM/edit?usp=sharing)

- Was able to complete the results for Trace, DataLeak, Shellshock, VPNFilter and FiveDirections.

- Was not able to complete results for Theia, First5Cases and Case67.

---

Last week, I ran into JVM Heap Memory issues while running Theia for example.

I tried increasing the memory allotted, but the dataset is far too large to process on my machine.

Not sure how to continue for large datasets like Theia and the First5Cases.

---

There also seems to be a dataset called Firefox-Compress.

Although I have compiled the results for these, I haven't found any mention of these datasets (and their results) in the paper, specifically the results tables included in the paper.

---

The manual also mentions another .jar file provided to calculate the metrics like FP, FN and to merge the different filtered out dependency graphs.

I assume this file is supposed to provide the metrics for RQ2 and the critical edges (attack entries too), however when run, this file does nothing.

Moreover, the paper mentions 4 RQs and answers them using 4 tables. However, after inspection, RQ1 and RQ3 are verifiable, however RQ2 and RQ4 are not verifiable through the scripts they have provided.

Would have to contact authors regarding this.

---
