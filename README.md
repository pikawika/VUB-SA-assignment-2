# Assignment 2 of Software Architecture course

This is the GitHub repository for the second assignment of the Software Architecture course @ VUB 2020-2021. This assignment was made in the second examination period.

## Table of contents

> - [Student info](#student-info)
> - [Important files and folders](#important-files-and-folders)
> - [Notes on the code](#notes-on-the-code)
> - [Running the code](#running-the-code)
> - [Validated output](#validated-output)

## Student info
- **Name**: Bontinck Lennert
- **Email**: lennert.bontinck@vub.be
- **StudentID**: 568702
- **Affiliation**: VUB - Master Computer Science: AI

## Important files and folders
- [Assignment PDF](assignment.pdf)
- [Report containing explanation on solution of the assignment](Lennert-Bontinck-SA2.pdf)
- [Written code folder](code/)

## Notes on the code

- The code has been developed on MacOS Big Sur by using the IntelliJ IDEA 2021.1.3 Ultimate Edition and the "Scala" plugin by JetBrains.
   - It was validated to work on a Windows 10 machine as well using the same software.
- For this project the following version of base software are used (same as WPOs)
   - JRE and JDK 1.8.291
   - sbt 1.3.13
- The sbt build will provide the Scala Play dependencies.

## Running the code

- Open the ```build.sbt``` file available under ```code\Lennert-Bontinck-SA2\build.sbt``` with the the IntelliJ IDEA.
- Select ```Open as Project``` and select ```Trust Project```.
- The IntelliJ IDEA should build the ```build.sbt``` file providing the dependencies. If all base software was installed with the same versions as used for this assignment, it should provide the correct SDKs as well.
- By default, the Scala Play compiler is disabled in the IntelliJ IDEA, enable it via the IDEA settings: ```Languages & Frameworks``` -> ```Play2``` -> ```Use Play2 compiler for this project```. Press ```Apply``` followed by ```OK```
- Rebuilt the project.
- Using the terminal inside the IntelliJ IDEA , execute ```sbt run```.
- Using your internet browser, navigate to [localhost:9000](http://localhost:9000/).

## Validated output

XXX