
### Self-assessment

Ever since this project first started, we've been implementing every  feature / fix suggested by the teacher, by coming back to the aspects that were criticized and updating them. We also commited to a lot of efforts to make this final delivery as good as possible: from refactoring the whole Ollir generation code to the implementation of every optimization and added extras With that in mind, we think that our project should be graded 20 out of 20 points, as we've successfuly completed every required task.

## Students

| Name             | Number        | Contribution | 
| ---------------- | ------------- | ------------ |
| Miguel Silva     |  up202007972  | 33%          |
| Martim Videira     |  up202006289  | 33%          |
| Matias Vaz |  up201900194  | 33%          |


# About Develop Optimization:

All the suggested optimizations were implemented in the final code, meaning:

### The -o flag, which forces:

- Constant Propagation

- Constant Folding



### The -r=k flag, which applies:

- Graph Colouring algorithm

- Minimum possible registers (**-r=0**)

- Tries to minimize to **K** register (with **-r!=0**)

- Report the mapping between the local variables of each method

- If the chosen **K** isn't enough, report a error. 

## Extras

### Warnings

- Variable Declaration Shadows Method Parameter.
- Variable Declaration Shadows Class Field.
- Assuming Type warning to help the programmer know he is responsible for type correctness.
- Unused Imports.
- Already Import module.

### Errors

- Though only supporting atribute `length` on arrays we have errors on accessing class attributes that don't exists using `this.attribute` syntax.
- Not a valid statement.

### Static Methods
- We support other static methods rathar than only supporting `main` and the correct semantic analysis is performed.

### Support For More Builtin Types

- We support more `String` builtin type and ast-to-ollir based optimizations apply to all builtin-types.(Although It is not supported by the Ollir Parser in use).

### Arrays Can Contain Types Other Than Ints


### Dead Code Elimination

- If statements.
- While loops.


### Parenthesis

- We simplify some expressions that need not parenthesis and constant folding works with parenthesied expressions.

# Compilers Project

For this project, you need to install [Java](https://jdk.java.net/), [Gradle](https://gradle.org/install/), and [Git](https://git-scm.com/downloads/) (and optionally, a [Git GUI client](https://git-scm.com/downloads/guis), such as TortoiseGit or GitHub Desktop). Please check the [compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html) for Java and Gradle versions.

## Project setup

There are some import folders in the repository. Your development source code is inside the subfolder named ``src/main``. Specifically, your initially application is in the folder ``src/main/pt/up/fe/comp2023``, and the grammar is in the subfolder ``src/main/antlr/comp2023/grammar``. Finally, the subfolder named ``test`` contains your unit tests.

## Compile and Running

To compile and install the program, run ``gradle installDist``. This will compile your classes and create a launcher script in the folder ``./build/install/jmm/bin``. For convenience, there are two script files in the root folder, one for Windows (``jmm.bat``) and another for Linux (``jmm``), that call this launcher script.

After compilation, a series of tests will be automatically executed. The build will stop if any test fails. Whenever you want to ignore the tests and build the program anyway, you can call Gradle with the flag ``-x test``.


## Tests

The base repository comes with two classes that contains unitary tests in the package ``pt.up.fe.comp``, ``TutorialTest`` and `` GrammarTest``. The tests in ``TutorialTest`` should all pass just using the provided code. ``GrammarTest`` contains tests for the complete Java-- grammar, and most should fail. By the end of Checkpoint 1, all tests should pass.

The class ``GrammarTest`` contains several static String variables at the beginning of the class where you should put the name of your rules for each type of rule that appears there. You have to set these variables to pass all tests.

To test the program, run ``gradle test``. This will execute the build, and run the JUnit tests in the ``test`` folder. If you want to see output printed during the tests, use the flag ``-i`` (i.e., ``gradle test -i``).

You can also see a test report by opening the file ``./build/reports/tests/test/index.html``.


### Reports
We also included in this project the class ``pt.up.fe.comp.jmm.report.Report``. This class is used to generate important reports, including error and warning messages, but also can be used to include debugging and logging information. E.g. When you want to generate an error, create a new Report with the ``Error`` type and provide the stage in which the error occurred.

### Parser Interface

We have included the interface ``pt.up.fe.comp.jmm.parser.JmmParser``, for which we already provide an example implementation in the file ``src/main/pt/up/fe/comp2023/SimpleParser.java``.

To configure the name of the class of the JmmParser implementation that should be automatically used for tests, use the file ``config.properties`` (more details below).

### Compilation Stages 

The project is divided in four compilation stages, that you will be developing during the semester. The stages are Parser, Analysis, Optimization and Backend, and for each of these stages there is a corresponding Java interface that you will have to implement (e.g. for the Parser stage, you have to implement the interface JmmParser).


### config.properties

The testing framework, which uses the class ``pt.up.fe.comp.TestUtils``, has methods to test each of the four compilation stages (e.g., ``TestUtils.parse()`` for testing the Parser stage). 

In order for the test class to find your implementations for the stages, it uses the file ``config.properties`` that is in root of your repository. It has four fields, one for each stage (i.e. ``ParserClass``, ``AnalysisClass``, ``OptimizationClass``, ``BackendClass``), and initially it only has one value, ``pt.up.fe.comp2023.SimpleParser``, associated with the first stage.

During the development of your compiler you will update this file in order to setup the classes that implement each of the compilation stages.
