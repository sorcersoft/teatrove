README for Trove/1.4.x

Copyright (c) 1997-2000 GO.com  http://opensource.go.com
 
Trove is an open source product. Refer to the license, which is based on the
Apache license, for more details.

Trove contains a rich cache of useful utilities used by both Tea and the 
TeaServlet.


Change History

1.4.2 to 1.4.3
===============================
- Changed HttpClient to retry once after catching an IOException when writing
  the request as well as reading the response line.
- Changed HttpClient so it doesn't throw a SocketException if available() is
  called after all data has been read from the InputStream.

1.4.1 to 1.4.2
===============================
- Fixed Depot defect: calling remove wouldn't immediately remove the value.
- Fixed BasicObjectRepository defect: calling replaceObject would sometimes
  not release a lock.

1.4.0 to 1.4.1
===============================
- Added a DistributedFactory to util so Depots can check with their neighbors 
  for valid objects before attempting to create their own copy.

1.3.x to 1.4.0
===============================
- Moved PropertyMapFactory into util.
- Net classes modified to be more compatible with jdk1.4.
- DistributedSocketFactory now supports random socket factory selection.
- Added BeanPropertyAccessor.
- Renamed TroveZip.dll to com_go_trove_util_Deflater.dll.
- Fixed CodeBuilder stack adjust for double[] and long[] return types.
- Fixed InstructionList for multiple double[] and long[] parameter types.
- Major changes made to classfile package. TypeDescriptor and MethodDescriptor
  classes replaced with TypeDesc and MethodDesc. The new classes are much
  better and are no longer dependent on class literals for primitives. Multi-
  dimensional array creation is simplified with the addition of an other
  newObject method to CodeAssembler. CodeAssembler now requires TypeDesc
  instances in place of Class instances.
- Bugs fixed in Depot relating to bad inter-thread communication. The Depot
  would sometimes return null values when it shouldn't.
- MergedClass generates different hashcode in name now. Although the value is
  different from previous releases, it is no longer dependent on the
  implementation of HashSet. When switching JVMs, the value is now consistent.

1.3.0 to 1.3.1
===============================
- Fixed a bug where PropertyMap.subMapKeySet() would only return submap names
  that contained more submap items.
- Fixed a couple minor bugs in classfile package. The stack adjust was
  incorrect in CodeBuilder for long shifts, and native methods couldn't be
  built.
- Depot cache size may now be specified as zero. This causes it to use just a
  SoftHashMap for caching.
- ThreadPool defect fix that caused the available count to slowly grow.
- Added accessors to Pair class.

1.2.4 to 1.3.0
===============================
- Added findResource methods to the DelegateClassLoader and ClassInjector.
- Added FastCharToByteBuffer. Converts to iso-8859-1 faster.
- Improved performance of HttpHeaderMap date formatting.
- Added FastDateFormat class, which supports the same patterns as
  SimpleDateFormat.
- Added Perishable interface to Depot.
- Added Deflater classes, similar to the ones in java.util.zip, except more
  support is provided for flushing data. Native C++ code is required for this,
  and the library is named TroveZip.
- Added WrappedCache class.

1.2.3 to 1.2.4
===============================
- Fixed thread safety defect in Cache class when MRU is shared.
- Fixed parsing defect in HttpHeaderMap of Set-Cookie header

1.2.2 to 1.2.3
===============================
- Added drain method CharToByteBuffer.
- Depot service method restores thread name when done. If the TQ doesn't
  recycle the thread, the thread name would keep on growing.
- Added SortedArrayList class.

1.2.1 to 1.2.2
===============================
- Depot supports automatic time based invalidation.
- Added removeAll method to Depot with a filter.
- HttpClient now closes PostData InputStream after doing POST.
- Introduced new set of classes for loading and configuring plugins.

1.2.0 to 1.2.1
===============================
- Fixed MergedClass defect that prevented merged classes and their class
  loaders from being unloaded.
- Fixed minor defect in TransactionQueue when the max thread limit is reached.
  Queued transactions wouldn't get serviced until either new ones are enqueued
  or until all serving transactions finished.
- Added filtered invalidateAll method in Depot.

1.1.x to 1.2.0
===============================
- Added net package, providing socket pooling and a HTTP client that works
  with it. Some classes in this package require Java 2, version 1.3.
- Added io package, moving those classes over from TeaServlet and Tea.
- MergedClass has an additional constructor for supporting lazy instantiation
  of merged classes.

1.1.0 to 1.1.1
===============================
- Fixed defect in BeanComparator that could cause stack overflow error because
  some internal Comparators were being shared when they shouldn't be.
- Finished off ClassFile reading functionality. Inner classes are now read, and
  custom attributes can be defined.

1.0.x to 1.1.0
===============================
- Introduced Depot class, which adds another layer of support for caching.
- Introduced MultiKey class, which makes it easier to create compound keys.
- Added tq package which contains support for the TransactionQueue scheduler.
- Added workaround in MergedClass to prevent interface static initializers
  from being wrapped. This bug is fixed in JDK1.3.

1.0.2 to 1.0.3
===============================
- Log event dispatch routine no longer locks out other threads from dispatching
  events at the same time. This can improve performance in database logging.
- UsageMap and UsageSet have newer, faster implementations.
- Cache can now piggyback onto another Cache so that the MRU may be shared.
- SoftHashMap and Cache support null values.

1.0.1 to 1.0.2
===============================
- New simplified implementation of PropertyMap fixes a few defects. Ordering of
  keys is now more consistent and views operate correctly.
- CodeBuilder in the ClassFile API performs correct flow analysis of exception
  handlers, variables, and jsr instructions.
- Fixed minor thread safety issue in ClassInjector.

1.0.0 to 1.0.1
===============================
- Fixed bug in Cache class. It wasn't ensuring that the newest values were
  those being guarded by the MRU.

1.0.0
===============================
- The first released version of Trove, consisting of classes moved from other
  packages.
