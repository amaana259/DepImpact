## **DepImpact**

Main repository: https://github.com/amaana259/DepImpact

Source code repository: https://github.com/amaana259/depimpactsrc

## **Steps**

- Fork repository (link provided on the Provenance Github repo).
- Go through the directories.
- Decompile Java File.
- Run Java File on provided attack log.
- Construct graphs for each run on the top ranked entry nodes.
- Consulted results provided in excel files.

**Done on the new repository.**

- Cloned the source code repository (link provided by Ahmad).
- Simulated DepImpact on the attack logs (in progress).
- Results verification (in progress).

---

## **Issues Faced**:

- Main repository does not have well-defined instructions on how to run the code.

- The repository also does not explain the source code structure, there is only the main .jar file provided with no further instructions.

- There is only one provided attack log file which is a simple password crack attack. Running the .jar file on this log file with the provided parameters, produces a set of .dot files (graphs). These graphs each belong to a specific run of the .jar file where each run implements forward causality analysis from one of the 6 top-ranked entry nodes. 

- To visualise the graphs, I had to install a library called graphviz and had issues installing the relevant dependencies. After successful installation, graphs produced are very large in size and not human-readable. No further results are provided after running the .jar file, only the graphs with little context and readability.

- The Java file had to be decompiled for which I had to install a separate decompiler. The decompiled Java file contains further Java files which appeared to be dependencies and source code files, but I could not figure out which were which. I separated 4 directories that seemed to be the source files, but being unfamiliar with Java code, I could not decipher these code files and understand what component of the DepImpact tool is which and if they even were present in the code files. However, after looking at the source code repository, the repo also had the same 4 directories listed as comprising the source code.

- Again, no clear instructions or well-definition of the code structure, since I had to find it out/guess for the main repository by myself.

- A ‘results’ directory was also present that had 4 excel files, each of which applied to one of the authors’ 4 research questions. However, how these excel files were constructed was not defined; they were simply provided with no further explanations or connections to the code files provided.

- Tried cloning the new repository but faced several issues:

    **git clone --depth 1 https://github.com/amaana259/depimpactsrc.git
        Cloning into 'depimpactsrc'...
        remote: Enumerating objects: 776, done.
        remote: Counting objects: 100% (776/776), done.
        remote: Compressing objects: 100% (522/522), done.
        error: RPC failed; curl 18 transfer closed with outstanding read data remaining
        error: 325 bytes of body are still expected
        fetch-pack: unexpected disconnect while reading sideband packet
        fatal: early EOF
        fatal: fetch-pack: invalid index-pack output.**

    - Tried cloning the repo through Github Desktop.

    - Tried cloning the repo using HTTPs through commands on terminal.

    - Tried generating a SSH key-pair and cloning the repo through SSH instead of HTTPs and cloned the repository.

- Large size of source code repository, datasets for attack logs are very large in size and had issues clearing required memory on my personal device.

---

## **How much was run?**

- The provided code in the repository was fully run. The .jar file was decompiled and run on the provided attack log file. The results however were not clear as to how they were obtained and how this specific run explained their findings in the paper.

- A new source code repository was provided which appears to have attack logs and pre-processing files well defined. I am currently in progress of cloning this repository and attempting to run the tool on these new logs.

---

## **Code Quality**

- **Code structure**: Undefined in main repository, well-defined in source code repository.
Only one .jar file provided in main repository that had to be decompiled but no further divisions for dependencies and source code files.

- **Readability**: Difficult to read result graphs produced by code on main repository.

- **Commenting and documentation**: None in main repository, a README.md provided in the source code repository on how to run the results.

- **Optimization**: Unable to optimise .jar file in main repository due to unfamiliarity and no clear context defined on the code functionalities.

- **Adherence to best practices**: No for main repository, Yes for source code repository.

---

## **Dataset Availability**

**For Main repository**:

- **Dataset used**: One attack log provided for password crack attack.

- **Format**: .log and .params/config file provided.

- **Availability**: Only one attack log.

- **Any issues or challenges**: Paper describes 10 types of attacks, 7 of which were performed on local hosts, and 3 of which were procured from the DARPA dataset. No information on what specific attacks (and logs) were used for the findings in the paper; the paper mentions that the POI events were procured from the attacks on their own and not set POI events, neither provided to us.

**For source code repository**:

- **Dataset used**: Attack logs and parameter files provided.

- **Format**: .log and .params/config file provided in a large tarball file.

- **Availability**: Several attacks provided.

- **Any issues or challenges**: Attacks provided in separate repository, not available without request. 

---

## **Code Explanations**

- **Why was a particular algorithm or model chosen?**  

    No particular training model as little to none deep learning/training is involved. Algorithm-wise, a set of algorithms was applied to reduce a dependency graph to its critical component given the POI event. Implementations of these algorithms not clear in the main repository .jar file.

- **How were parameters set or tuned?**

    Edge merges were done with a threshold of 10s, meaning any edges that fit into this timeframe were merged, to reduce the overall size of the dependency graphs. Three types of relevances; data flow amount relevance, temporal relevance and concentration ratio were used to compute dependency weights for application of backward and forward causality analysis/algorithms.


- **Explanation of important functions or classes.**

    Not explained well in code.

---

## **Conclusion**

- **Main takeaways:**

    Look at source code repository and test on the provided attack logs, see if results are consistent with the excel files/results provided in main repository. Also, look at how the data/statistics in the excel files are obtained, since no clear instructions are given as to how they are.

- **Final thoughts or recommendations:**

    Test on other attack logs obtained from public datasets. See if results are similar to the ones provided.

- **Next steps for improvement:**

    One idea is to merge MAGIC and DepImpact to cross-evaluate the performance of the two. The idea is to use the anomaly detection prowess of MAGIC to predict if anomalies/malicious events in attack logs are present, then see if these events when passed as a POI event, help find a critical component/attack sequence in a larger dependency graph.

    Similarly, take a critical component obtained through a run of DepImpact and run the audit logs through MAGIC. See if the MAGIC is able to predict if any malicious events are present and if the POI event is detectable.
