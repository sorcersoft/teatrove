README for Tea/3.2.x

Copyright (C) 1997-2001 GO.com  http://opensource.go.com
 
Tea is an open source product. Refer to the license, which is based on the 
Apache license, for more details.

The Tea template language is designed to be used by a hosting system, and
so it only delivers exports that can be imported into another product. Tea
isn't a stand-alone system. Most of the code in the Tea product supports the
compiler. A much smaller amount of code supports Tea in a runtime environment.

Change History

3.2.2 to 3.2.3
===============================
- Moved the TemplateAdapter to the engine package so precompiled templates 
  would work properly. TemplateSource implementations should use this class 
  to instead of creating TemplateLoaders directly.

3.2.1 to 3.2.2
===============================
- Fixed a bug in the TypeChecker. In "if (a == null and ...) {} else {}", the
  else scope would treat variable "a" as always being non-null.
- Added the com.go.tea.engine package to simplify the process of integrating 
  tea into other applications.
- Shared associative array instances are now created as unmodifiable. Although
  templates couldn't directly modify it, rogue Java functions could.

3.2.0 to 3.2.1
===============================
- Re-fixed a fix that from version 2.1.10: "Compiling large sets of templates
  would sometimes cause the VM to run out of memory."  The fix was to free up
  unused parsetrees during bulk compiles. This fix got undone in version 3.1.4,
  when the "preserveParseTree" method was added. If this method never got
  called, then all parse trees would be preserved, wasting memory.

3.1.5 to 3.2.0
===============================
- Fixed problem that allowed for infinite loops. Now The loop variable 
  cannot be modified or reused within that loop.
- Added new language keyword: 'break', which allows for breaking out of 
  foreach loops.
- Improved performance of date formatting and printing of unformatted integers.

3.1.4 to 3.1.5
===============================
- Added status reporting to Compiler. Allows a user interface tool to add a
  status listener to the Compiler that will be notified of compilation 
  progress.
- The Compiler now checks if the thread that it is running in has been 
  interrupted. If it has, then it returns the names of all the templates it 
  finished compiling. The Compiler only checks between files, so interrupting 
  within a file is not possible.
- Bug fix involving detached substitutions when passing its own substitutions.
- Added format accessor methods to Context.

3.1.3 to 3.1.4
===============================
- Fixed bug that would sometimes not apply a cast operation for the "isa"
  operator. This would cause a verify error when the template class was loaded.
- Added new constructor and new method to Compiler class so that template
  signatures can be saved and shared among Compiler instances. This is useful
  for development environments, like Kettle, offering performance improvements.

3.1.2 to 3.1.3
===============================
- Inner classes may be specified as template parameters and as argument for
  "isa" operator.
- Scanner has mode for emitting comments and other special tokens.
- The numberFormat function now accepts two additional arguments for setting
  infinity and NaN.
- Fixed a NullPointerException bug during type checking.
- Fixed exception guardian defect involving paren expressions.
- Fixed flaw in unicode escape processing.
- Number scanning in compiler is more to spec.
- Fixed NullPointerException when converting between number objects.
- Source file name listed in error messages and stack traces now includes
  directory components, relative to the root source directory.
- Performance enhancement for string concatenation involving two arguments.
- Deprecated io package since those classes moved to Trove.

3.1.1 to 3.1.2
===============================
- Exception guardian properly traverses into substitution blocks.
- Calling foreach on a null array or collection now skips past the loop without
  a NullPointerException being thrown.

3.1.0 to 3.1.1
===============================
- Added getTemplateLoader method to TemplateLoader.Template class.

3.0.0 to 3.1.0
===============================
- Fixed bug in "if" statement scope merging if variable was tested against
  null and then assigned a valid value if so.
- Fixed bug in comparison of primitive values against null. The comparison is
  legal, but sometimes it would cause an internal compiler error.
- Fixed bug in conversion of char properties to Strings. Conversion was
  incomplete and resulted in verify errors.
- Conversion of object numbers to other object numbers was being rejected.
- Added support for exception guardian mode of template compilation.
- Added BeanDoc to the build process for Tea. The Context BeanInfo classes
  are now auto-generated from the source files.  
- Deleted the BeanInfo and FunctionDescription.properties files from the 
  runtime package. They are no longer needed since BeanDoc now generates the 
  BeanInfo files.
- Fixed NullPointerException caused in JavaClassGenerator by certain recursive
  template calls.

2.3.x to 3.0.0
===============================
- First open source version of Tea.
- Packages renamed and re-organized to be based at com.go.tea.
- Templates must be called using the 'call' keyword.
- Some components moved to trove package.

2.3.5 to 2.3.6
===============================
- Fixed replaceFirst and replaceLast functions, which were defective since
  version 2.2.0.

2.3.4 to 2.3.5
===============================
- Fixed stack overflow defect in conversion of NewArrayExpression.
- Fixed ClassInjector defect that caused an infinite loop.

2.3.3 to 2.3.4
===============================
- Fixed a recursion defect in Type equality testing that occurs when an
  object's element type is the same as itself.

2.3.2 to 2.3.3
===============================
- Fixed defect in MergedClass in which the returned constructor has parameters
  which come from a different ClassLoader. This would cause an InternalError
  to be thrown. Class parameters are now reloaded from the correct ClassLoader
  before the class is generated.
- Fixed defect in DefaultContext that would leak memory whenever a date is
  printed for this first time using the default date format. This bug was
  introduced in version 2.3.1.
- MergedClass now accepts optional source class method prefixes in order to
  resolve ambiguities and to allow classes to be specified multiple times.
- Functions can be called with '.' in the name, just like template calls. The
  '.' characters are converted to '$'. With the MergedClass changes, a prefix
  that ends with '$' is ideal.
- Fixed all outstanding type checking defects. With applied changes, Scope and
  TypeChecker classes are a little bit smaller.
- Slight performance enchancement to string concatenation by making a more
  educated guess for StringBuffer initial capacities.
- For string conversion of null, the conversion is applied only if required.
  Otherwise, the value is left as null.
- Equality tests against a string and a non-string will force the non-string
  to be converted to a string unless it is null.
- Added additional method to Substitution so that substitution blocks can
  identify themselves within a template.

2.3.1 to 2.3.2
===============================
- Fixed defect that would prevent Kettle from functioning properly. Kettle
  does not allow use of the WeakHashMap.

2.3.0 to 2.3.1
===============================
- DefaultContext now correctly caches date formats specified with time zones.
- Fixed a bug in converting objects to non-null in if statement scopes.
- Locale can be set in standard context, which is applied to date and number
  formatting.

2.2.x to 2.3.0
===============================
- Values can now be returned from templates. The last output statement in a
  template becomes the return value. Templates are now functions instead of
  just procedures. No special action needs to be taken on the part of the
  template writer in order to support return values. CQ #722.
- If template ends with plain text, then the trailing whitespace is removed
  from it. CQ #713.
- Fixed a bug that would not apply the current null format in string
  concatenations. CQ #425.
- Fixed a bug that would convert null strings to "null" when passed to a called
  template.
- Filled out starwave.classfile package to fully support reading and
  disassembling Java class files.
- MergedClass utility is no longer dependent on WeakHashMap, making it
  usable under VMs that have imported the collections API, but don't support
  WeakHashMap.

2.2.1 to 2.2.2
===============================
- Fixed bug in encoding of large string literals that would cause a UTF
  data format exception to be thrown.

2.2.0 to 2.2.1
===============================
- TemplateLoader is simplified so that base ClassLoader has more control over
  where templates are loaded from.
- ClassInjector and FileCompiler support multiple source directories.
- Created new utility component, MergedClass, which makes it easier to merge
  multiple Tea runtime context instances into one.

2.1.x to 2.2.0
===============================
- Now builds using JDK1.2.
- Removed all deprecated API.
- Repackaged starwave.tea.classfile to starwave.classfile.
- JDK1.2 Collection API supported by foreach statement. Reverse looping only
  supported for collections that implement the List interface.
- Supports relational tests against classes that implement the JDK1.2 
  Comparable interface.
- Better support for handling types defined by interfaces.
- Overloaded replace function to also operate on a mapping of pattern
  replacement pairs.
- Standard template context class is now broken up into multiple interfaces and
  classes.
- FileCompiler has command-line interface.

2.1.14 to 2.1.15
===============================
- Fixed bug in ClassInjector that would cause template to sometimes not
  completely load.

2.1.13 to 2.1.14
===============================
- Overloaded dateFormat function to accept a time zone code, just like in
  Tea/2.2.x.

2.1.12 to 2.1.13
===============================
- Fixed a bug that would cause an internal compiler error when one template
  calls a template that had errors in its parameter declarations.

2.1.11 to 2.1.12
===============================
- Fixed yet another verification bug. This one had to do with variable
  references deeply nested inside substitution blocks.

2.1.10 to 2.1.11
===============================
- Added tons of new string manipulation functions to standard context, and
  added more versions of replace function. New and existing string handling
  functions are more robust with respect to handling null.
    toLowerCase(str)
    toUpperCase(str)
  * trim(str)
  * trimLeading(str)
  * trimTrailing(str)
  * startsWith(str, prefix)
  * endsWith(str, suffix)
    substring(str, fromIndex)
    substring(str, fromIndex, toIndex)
  * find(source, pattern)
  * find(source, pattern, fromIndex)
  * find(source, pattern, fromIndex, toIndex)
  * findFirst(source, pattern)
  * findFirst(source, pattern, fromIndex)
  * findLast(source, pattern)
  * findLast(source, pattern, toIndex)
    replace(source, pattern, replacement)
  * replace(source, pattern, replacement, fromIndex)
  * replace(source, pattern, replacement, fromIndex, toIndex)
  * replaceFirst(source, pattern, replacement)
  * replaceFirst(source, pattern, replacement, fromIndex)
  * replaceLast(source, pattern, replacement)
  * replaceLast(source, pattern, replacement, toIndex)
- Fixed bug that would cause a NullPointerException if left side of string
  concatenation evaluates to null.

2.1.9 to 2.1.10
===============================
- Additional method is created in generated class files:
  static String[] getTemplateParameterNames() returns the names of all the
  parameters passed to the execute method.
- Added replace function to the default runtime context.
- Added support for new language feature, the "isa" operator.
- Fixed array class loading bug.
- Re-wrote code that manages scopes in order to solve several outstanding
  class verification errors.
- Compiling large sets of templates would sometimes cause the VM to run out of
  memory. Compiler now frees up template parsetrees when no longer needed.

2.1.8 to 2.1.9
===============================
- Templates can now accept arrays as passed in parameters.

2.1.7 to 2.1.8
===============================
- Call cycle is now allowed. (you were right, Mark, they should be allowed)

2.1.6 to 2.1.7
===============================
- Fixed bug in string concatenation against an array access. The result was
  being casted to a string instead of being converted.

2.1.5 to 2.1.6
===============================
- Still more fixes for bug introduced in 2.1.3 with respect to parameters. This
  time, a method that accepts a "this" reference as the first parameter didn't
  get its other parameters incremented to the next slot. A typical case of
  a bug that was hidden because it depends on another bug.
- Relational expressions against boolean literals (i.e. a == true) are now
  correctly optimized.

2.1.4 to 2.1.5
===============================
- Parsetree walker wasn't visiting the second, optional range expression of 
  the foreach statement. This prevented Kettle's statement completion
  feature from working while typing the second range expression.
- More fixes for bug introduced in 2.1.3 with respect to parameters. Version
  2.1.4 didn't completely fix it. Parameter local variable slot numbers were 
  being re-assigned under certain conditions.

2.1.3 to 2.1.4
===============================
- Added readme.txt.
- Fixed bug introduced in 2.1.3 that produced invalid classfiles when number
  of passed in parameters is greater than the number of local variables used.
- Fixed bug introduced in 2.1.3 that caused arrays of arrays to be generated
  as multi-dimensional arrays.
- Fixed bug that prevented certain types (like java.util.Date) from converting
  to strings automatically when passed to functions that accept a string
  parameter.
- Code generation can be disabled from compiler. (Kettle requires this)
- Cleaned up TypeDescriptor class so that array types can be unambiguously
  described. (Related to array of arrays bug that was fixed)
- Unhandled exceptions in compiler are now sent to unhandledException in
  ThreadGroup instead of calling printStackTrace directly.
- Passing null to nullFormat function defaults to "null".

2.1.0 to 2.1.3
===============================
- Tea changes from version 2.1.0 to 2.1.3 are not documented.

2.0.0 to 2.1.0
===============================
- Added support for looping through ranges via the foreach statement.
- Other changes not documented.

2.0.0
===============================
- The first version of Tea is 2.0.0.
