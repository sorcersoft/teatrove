README for TeaServlet/1.4.x

Copyright (C) 1999-2001 GO.com  http://opensource.go.com
 
The TeaServlet is an open source product. Refer to the license, which is based
on the Apache license, for more details.

The TeaServlet provides a way to separate the presentation from the processing
of data through the use of tea templates and simple tea applications. The three
required components for setting up and running the teaservlet are: The java
classes including both the TeaServlet itself and any user defined apps,
the templates for providing the HTML like front end for the apps and the 
properties file with configuration information and initialization parameters.


Change History

1.4.4 to 1.4.5
===============================
- Modified the clustering code to ensure that all RMI communication occurs 
  on the backchannel.
- The RemoteCompiler now uses HttpClient from trove rather than handling
  sockets directly.
- Templates will now be shown in the Admin page even if not yet loaded. 
- Last template reload time and number of known templates is now displayed.
- Fixed bug in stealOutput that prevented it from ever working.

1.4.3 to 1.4.4
===============================
- MSIE 4.x doesn't seem to support the way the TeaServlet compresses output,
  even though it is a legal GZIP stream. Change made to isCompressionAccepted
  method of ApplicationRequestImpl.

1.4.2 to 1.4.3
===============================
- The getClassForName method in AdminApplication now uses the ClassLoader from
  the MergedContext.
- Automatic cluster discovery and RMI based cluster administration added. 
- TeaServlet also accepts "*.tea" style servlet pattern mappings. Any extension
  may be used.
- RegionCachingApplication supports gzip compression of cached regions.
  Browsers that accept GZIP content encoding may receive a compressed response.
  To enable, the gzip initialization parameter must be set 1 to 9.
- Created TeaServletEngine and TeaServletTransaction interfaces. Now any
  servlet can access and use TeaServlet services.

1.4.1 to 1.4.2
===============================
- Fixed dynamicTemplateCall to work with servlet containers that don't support
  servlet API 2.3

1.3.x to 1.4.1
===============================
- Added a new template.preload parameter that when set to false causes
  templates to load only when called, thus reducing memory usage.
- Applications can now plug into the admin pages by implementing AdminApp.
- Admin pages use a common admin page with dynamic calls to specific detail 
  pages.
- Enhancements to RegionCachingApplication. Regions are now cached using the
  Depot class, and a new nocache function is provided.

1.3.1 to 1.3.2
===============================
- Changes for Servlet API version 2.3.
- Added support for Plugins, which provide an easier way to configure in
  resources that need to be accessed from multiple Applications.

1.3.0 to 1.3.1
===============================
- Reduced excessive synchronization during template reload that prevented
  other requests from being serviced until the reload completed.
- Region caching application now weakly references templates in its keys,
  allowing old templates to be unloaded much sooner.
- Added three new admin templates: GetClass, ContextClassNames, and 
  ContextPrefixNames.  Together these templates provided support for the
  Kettle 4 project integration features.  The GetClass template uses a new
  AdminContext function called streamClassBytes which enables HTTP-based 
  class loading directly from the TeaServlet.  The ContextPrefixNames template
  utilizes a new property (contextPrefixName) of the ApplicationInfo class.

1.2.x to 1.3.0
===============================
- Context classes are no longer created immediately on every request. Instead,
  the Application.createContext method is called only when a defined function
  is first needed.
- The insert URL family of functions no longer use java.net.URLConnection.
  Instead, they use HttpClient in Trove. A new function, setURLTimeout,
  overrides the default timeout for accessing URLs.
- Bug fixed in admin pages when displaying applications that have no context.
- The io package has been deprecated, and all classes have been moved into
  Trove.

1.2.2 to 1.2.3
===============================
- FileByteData class is now thread safe. When added as a surrogate and then
  cached, one thread would open the file while another would close it.
- Added readFile and readURL functions. They return the file contents as a
  String, and they support different character encodings.
- Application names may be specified with a hyphen. The part before the hyphen
  serves as the optional function prefix. This allows multiple applications
  to appear to provide a unified set of functions.
- Templates may now be loaded over http.  Any template path beginning with 
  "http" will use the remote loading mechanism other templates will be loaded
  as before.
- Compiled templates will be synchronized with their source timestamp to ensure
  that templates are recompiled in a consistent manner despite clock offsets.

1.2.1 to 1.2.2
===============================
- Slight change in MergedApplication with respect to class loading. Sometimes
  it would load classes from the system class loader, bypassing any class
  loader imposed by the Servlet Engine.
- Deprecated ObjectIdentifier.
- TTL in RegionCachingApplication is specified in milliseconds now instead of
  seconds for consistency and greater precision.
- Improved concurrency in RegionCachingApplication by using more specific
  synchronization monitors. Also correctly supports arrays as secondary keys.
- ServletExceptions caught by TeaServlet now pass message on to sendError call.

1.2.0 to 1.2.1
===============================
- Calling setContentType on ApplicationResponse sets character encoding of
  character buffer. Before, only the template could change character encoding.
- Supports TemplateLoader.Template.getTemplateLoader of Tea 3.1.1.
- Performance optimizing InternedCharToByteBuffer was not properly being
  invoked. Now that it is, performace of TeaServlet is back to what it was
  before Barista 2.2.
- Calling insertFile on a non-existent or inaccessible file now logs an error
  at the point the insert is called. Before, the error would be logged when
  the template output was written, making it difficult to track down the
  culprit.
- Incorporated BeanDoc into the build process so that the context BeanInfos
  can be autogenerated.

1.1.1 to 1.2.0
===============================
- Added template reload for clustered servers.
- Changed many of the fields and the ContextImpl inner class from private to
  protected within AdminApplication simplifying extension of the TeaServlet.
- Added a getTeaServletClass() method to TeaServletAdmin to help determine the
  name of the class currently serving as the TeaServlet.
- Query string shown in error processing template message.
- Added exception guardian mode, enabled with template.exception.guardian
  property. When enabled, each statement in the template has an exception
  handler so that RuntimeExceptions don't cause the template to abort.
- Fixed bug that wouldn't report internal compiler errors in reload page.

1.1.0
===============================
- The first open source version of the TeaServlet, which was pulled out of the
  Barista product and cleaned up.
- TeaServlet runs in Servlet Engines that support version 2.1 or 2.2 of the
  Servlet API.
