**DEPIMPACT (Week 5/Week 2):** 

---

- Last week, I installed VirtualBox and Vagrant, but was having issues importing the .ova file into VirtualBox. 

* This week, I managed to import the .ova file into VirtualBox using a .ovf 'redirection’ and then created a Vagrant Box to SSH into.

* After SSH’ing into the box, I could not view any files using ‘ls’.

* I entered ‘ls /home’ after which some files were viewable but a few of these files were password-protected.

* I then decided to open the VM image using VMWare, and not using Vagrant for the timebeing.

---

![](https://lh7-rt.googleusercontent.com/docsz/AD_4nXda1b2BYrrNdUeJSAN9XCG_PB_hxwd_Ld-REhKenvFHll_kQDm6n1x5XUAff8EcEnDtJylRyXAly37jbDfzrQ2lIGuIAxpUtBFXbjZ-PrP7aGoQhdUcUm1Lk75OcuSRmYe4Dbw-qw?key=GisAA35hsIkUUANf4mXQzKjZ)

As can be seen in the picture above, this was the ‘artifact’ home folder that opened when I launched the VM. It had two password-protected folders while the folder ‘DepImpact-artifact’ appeared to contain the files I required.

There was also an issue that if I left this VM idle for a while, it logged out and then required a password to log back into the VM account. No password or any instructions were provided regarding this.

It took me a day to find a work-around for this as I rebooted the VM in recovery mode and reset the password from a root shell.

---

![](https://lh7-rt.googleusercontent.com/docsz/AD_4nXfT2gokSvb2fuKImA18tnMtLOSIFdQ_ULm54Wh91zCeMSVa54O6jdB5_XMhrEHyvrIRfXgR6-rlRWQAvHXnl-0O35Lg9dpW8ffbV0Cu6oPj29vcVw11FA1pKcQ86azBPae674Ct7g?key=GisAA35hsIkUUANf4mXQzKjZ)

I extracted the log files provided in ‘allcases.zip’. I have yet to extract the ‘first5cases.zip’ since it is the largest and my VM ran out of memory; I plan to run this after I am done with the others.

I created Log Folders and Result folders to modularise the reproducibility process, as also instructed in the manual PDF provided.

![](https://lh7-rt.googleusercontent.com/docsz/AD_4nXduy6vLNiYooPCO307ym9K0-zdgG8L_pSV7Ly_wXSxQZE7jNxRWQq-cKTWsrIxDuN8bSKRaeU1WACE0UQayc2_m9D33W__NKnLd20xSBitdBtUDcBL4OfsSp7FqDXdlVAlZxml_Aw?key=GisAA35hsIkUUANf4mXQzKjZ)

![](https://lh7-rt.googleusercontent.com/docsz/AD_4nXeC0wclT3NQsrsEW5Nm1aZzMd6cxm7iG0fUibCc3UzyXz8QqTMXVm9484ECDLTb6BaG_ibwg8deQi1KaCyFhcPPDUPSV_-H015wQVk_1xOVzSkXuVIJqEKcZcczQahs57JUXO7fMA?key=GisAA35hsIkUUANf4mXQzKjZ)

---

After constructing the directory structures, the manual provided sample commands to generate the results for the log files. I was able to generate results for 4 datasets; namely Dataleak, Firefox, Shellshock and VPNFilter. Here is one sample stats file generated for the first Dataleak case:

![](https://lh7-rt.googleusercontent.com/docsz/AD_4nXcVI1gNCNEeUTRz8vY8yE9X563T4QXkb3T5aOk0FEqreuL3oxCxS3V38Czzy55yZSWYnr96Q9ekICUSu1tbhuz08CzQfa1xMFVrYU69hzF7V94Rwj38wv_X1ELtZ-ruqzl1wXn2rA?key=GisAA35hsIkUUANf4mXQzKjZ)

Although I haven’t looked quite closely yet, these stats files seem to answer how the authors got to the data included in the Excel sheets for their **Research Questions**, an issue that hadn’t been addressed through their provided GitHub code repositories.

---

However, I was not able to generate results for the other datasets. These were namely Theia, Trace and Fivedirections. These folders did not contain text log files, instead contained .dot files along with a parameter file.

![](https://lh7-rt.googleusercontent.com/docsz/AD_4nXc9GYC5sGTHjYo7DamFJ17kHQP2OknPIT04jlIfLRpZPyxyQdeCYJCnrG_Zs8yrNjFXE_jOZUqhifdH8iYxjjcxiqRb5uekclxwYYZF_0F7ytQ05c5Shqde0167ElGU2InT_crfYw?key=GisAA35hsIkUUANf4mXQzKjZ)

Running the sample command with this log file (.dot file) raised a Java Heap Memory Exception that I have been unable to solve. I plan to send an **email to the authors**, since raising a GitHub issue is not possible as the image is not on GitHub.

---

Here is a PNG file that I created of one of the filtered graphs using graphviz:

![](https://lh7-rt.googleusercontent.com/docsz/AD_4nXcExS12Ks2LxWm4-qMyiRkLrD9Kpmt-HW_nrE0CTNuEz_bYuJzGQmG70qTfH40Osu16J4xq0EfR9Q2DuJ9k5UxTkp-WmWLN_XLp8THPlx-PYtZpR5V0RITVaL2V3kdvX3IMUbiSfw?key=GisAA35hsIkUUANf4mXQzKjZ)

_It won’t be viewable here, but upon zooming in, the graph seems to be clear and smaller than the original dependency graph._