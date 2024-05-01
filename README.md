# Deb'm - *De*compiler *b*ench*m*arking

# General description

This project is a BSc-project, written by three students. We included our paper
as 'paper.pdf' and refer to that paper to start with. Having read it, you will
have a good idea of what we've done and why. In this readme, we focus on
technical details.

# Hard requirements

### The producer
- The producer part shall produce series of c sourcecode files, each of one 
  is the basis of a single test. These shall all be compiled to LLVM-IR and 
  this shall be compiled to an executable (binary). Each c sourcecode will 
  be put in a separate folder and all the results (LLVM-IR's, binaries and 
  intermediate files such as .bc-files) shall be put there as well. Tests 
  shall be aggregated in container folders.
- The producer shall have the flexibility of supporting more than one 
  compiler (but requiring the compiler to be able to work with LLVM-IR).
- The producer shall have the flexibility to support different optimization 
  levels.
- The producer shall have the flexibility to support different target CPU 
  architectures.
- The producer shall make as many choices as possible with a certain 
  randomness. For example: when producing loops, it will randomly pick the 
  bounds of the loop variable.
- The producer shall produce c code that meets c grammar standards and is 
  compilable. The produced code may lead to deadlocks, endless loops, 
  endless recursion _etc_, as it is not intended to actually be run.
- The producer shall be extendable with new code building classes, thus 
  supporting new test features.
- The producer shall be able to run on any platform supporting Java.

### The assessor

- The assessor shall by default select a container to process at random.
- The assessor shall assess all test sets in a container and report 
  aggregated results.
- The assessor shall be able to cope with all decompilers that can be 
  invoked from command line.
- The assessor shall be expandable with new code assessing classes, thus 
  supporting new test feature.
- The assessor shall be able to run on any platform supporting Java.

### Notes

#### Expandability

We lack the resources to address all challenges a decompiler faces. We each have
focussed on one and thus the current version focuses on three: loops, data
structures and functions. We made the platform flexible, so that analysing other
features (variable type recognition, calculation readability, recognition of the
use of STL-functionality) can be easily added. We will go into the process of
expanding later.

#### Decompiler flexibility

The only requirements we set for the decompilers, is that they produce C code
and that they can be invoked in some way from the command line. We only require
the full path to a runnable file which takes as parameters a full path to a
source binary and a full path to a target file to put the decompilation result
in. As the runnable file needs not be an executable, but may also be a script, a
fairly simple wrapper script should work. We expect every decompiler researcher
to be able to write such a simple script. We have included the scripts we've
used in the scripts folder, so they can serve as inspiration.
We have compiled our binaries to run on Windows OS, but it is not so difficult
to introduce another compiler and/or options that make sure that the binary can
run on Linux OS or any other. The platform can thus also be used for decompilers
that target non-Windows OS's.

#### Multi-platform support
Naturally, we have made sure to cover the everlasting 
slash/backslash-problem that exists when accessing files on different OS's. 
When developing the software, two of us used Windows OS, one of us used Linux. 

# Soft requirements

We want Deb'm to be:

- fair;
- fairly easy to use.

### Fairness

A fair benchmark measures only on points that a decompiler van score on.
Example: as there is no way a decompiler can infer symbolic constants from a
binary, we don't score on this feature. But a decompiler van work out where
functions begin and end, so we do score on that feature.  
A fair benchmark scores on a set of features. If it is great in recognizing
functions, but rubbish on recognizing variables, the benchmark must show this,
so developers and researchers can neither fool, nor be fooled.  
A fair benchmark uses binaries that have real life situations to score upon. So,
we put more than one function in a binary, we use all kinds of loops etc.

### Fairly easy to use

We don´t do massive options tables, we don't have a massive user interface that
first needs to be learned. Both the producer and the assessor only takes up to
three arguments and that's it. It reports in HTML, which is easy to handle with
the availability of loads of standard software to do that job.

# Using the producer

### Don't use the producer...
...unless you add your own features. The whole idea of a benchmark is a 
fixed set of binaries. We have published our own set in the release section 
of this github repo and we advise you to use those.

### But if you do use the producer...
...read this:

#### Arguments
Invoke the producer from command line in your local Java environment. Use -h,
/h, -help, /help, -? or /? to get argument details.

# Using the assessor

### Assessor arguments
Invoke the assessor from command line in your local Java environment. Use -h,
/h, -help, /help, -? or /? to get argument details.

# Adding a test feature

### Introduction

To add a feature, implement the appropriate interfaces in one or two new
classes, preferably kept together in a package with all the helper classes,
enums _etc_.
You may combine producer and assessor functionality in one single class that
implements all the necessary interfaces, but we do not recommend it, as 
writing two classes will be easier to understand and manage. Both parts are 
invoked completely independent of each other, so there is no gain in sharing 
data between producer and assessor.  
You may also choose not to implement a producer
class, in which case your assessor can still study the c-source and the llvm’s
to discover what can be found in the binaries, as well study the decompiled
c-code. But you probably want to make sure certain code to be tested is
available in the source, in which case writing your own source producer would be
preferable.

### The producing part

Every producer must implement **IFeature**. This interface shows the class really is
a producer and contains the basic functions for determining whether the
producer is satisfied with the work it has done. This is important, as the c
generator keeps on asking the producer classes for pieces of code until all the
classes are satisfied.  
The producer class always has a reference to the c generator class, so whenever
it wants, for example, to call a random function, it can ask the c generator to
provide one that satisfies its needs. The c generator will then ask a producing
class to make one. The same goes for structs and global variables. Every
producer is asked for the includes it uses and the c generator will make sure
every include is included only once.  
In order to actually generate pieces of code, one or more of these interfaces
must be implemented: **IExpressionGenerator**, **IFunctionGenerator**,
**IGlobalVariableGenerator**, **IStatementGenerator**, or **IStructGenerator**. If you want
to mark all functions in the code in your own way, you must implement
**IFunctionBodyInjector**.  
The next step is to let the c generator know your new class exists, do this by
adding your class to the c generator constructor:  
```java
class CGnenerator {
    public CGenerator() {
        // [...]

        // fill array of feature-objects
        var functionProducer = new FunctionProducer(this);
        features.add(functionProducer);
        features.add(new DataStructuresFeature(this));
        features.add(new LoopProducer(this));

        // [...]

    }
}
```
Finally, change the version number:
```java
public class MetaData {
    public static String Version = "1.0.0";
}
```

### The assessing part

The framework takes care of all the I/O-fuss: it selects a container (either 
randomly or the one the user passed as argument), reads all
the tests and presents a neat set of information, one by one, to all the
assessor classes. We use ANTLR to access the llvm’s and decompiled c files and
your class gets instances of the appropriate lexers and parsers to work with.
Each feature class returns a set of test results for the specific binary, which
the framework will aggregate over all binaries tested.  
Your assessor class only needs to implement one interface: **IAssessor**. It
contains the necessary calling points. It also comes with the TestResult
abstract class, which is a universal way to gather test results of any kind.
We’ve so far implemented several child class, the most basic ones being child 
classes to simply count something, for both situations in which we do or do 
not the count limit. Other child classes
implement quality scores on a scale of 0…10 and recall-statistics. Take your
pick, or make your own. As long as you implement the TestResult class, the
framework can process the outcome.  
After you’ve created your assessor class, make sure it is known to the
framework and add it to the code in the function Assessor.getFeatureDefaults(), 
pretty much the same as on the producer side. Also make sure you edit the
assessor options code (Main.handleCLIParameters()).
When writing your own tests, don't forget to add them to ETestCategories, 
and describe them there.

# The code marker system

In our paper we've explained the use of code markers. We only outline some 
technical issues here.  
The base class for code markers is abstract and its manipulation methods are 
protected. It handles the core functionality, which comes down to:  
- managing a Map<String, String> which maps property names to property values;
- producing the final string to be put in the code, making sure to escape 
  characters where necessary and adding the code marker GUID and CRC16;
- converting a string back to a code marker object.  

It is highly recommended to write your own child class. This class can use 
enumerations for property names and convert the real values (booleans, 
integers, floats) back and forth to the string representation they will have 
in the code marker string.  
If you want to experiment quickly, you can use the BaseCodeMarker class, 
which makes the property manipulation methods public.

# Third party software/sources

### ANTLR

ANTLR is used for parsing LLVM-IR and C.
License: https://www.antlr.org/license.html (BSD license)  
C and LLVM grammars
used: https://github.com/antlr/grammars-v4/blob/master/c/C.g4 (BSD license)
and https://github.com/antlr/grammars-v4/blob/master/llvm-ir/LLVMIR.g4 
(MIT license)  

### CRC16

Implementation based on the FIT-file format
documentation, https://developer.garmin.com/fit/overview/

### Orthogonal arrays

We used the Orthogonal Array Package to calculate some OA's we needed. We 
only distribute the result. The package is published under a BSD style 
license, https://oapackage.readthedocs.io/en/latest/oapackage-intro.html

### clang/LLVM
We used clang (https://clang.llvm.org/) as our c compiler, version **** 
(Windows OS).

# Deb'm licence

We publish our work primarily under the CRAPL-license
(https://matt.might.net/articles/crapl/), the text of which 
can be found in CRAPL-LICENSE.txt. In reference to article III.5 of the 
CRAPL-license, we grant rights according to the GNU-license, the text of 
which can be found in GNU-LICENSE.txt,

# Acknowledgements

We thank Nico Naus (Open University of The Netherlands) for all the help he 
gave us during our project.