# Microsoft Developer Studio Generated NMAKE File, Based on TroveZip.dsp
!IF "$(CFG)" == ""
CFG=TroveZip - Win32 Debug
!MESSAGE No configuration specified. Defaulting to TroveZip - Win32 Debug.
!ENDIF 

!IF "$(CFG)" != "TroveZip - Win32 Release" && "$(CFG)" != "TroveZip - Win32 Debug"
!MESSAGE Invalid configuration "$(CFG)" specified.
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "TroveZip.mak" CFG="TroveZip - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "TroveZip - Win32 Release" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE "TroveZip - Win32 Debug" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE 
!ERROR An invalid configuration is specified.
!ENDIF 

!IF "$(OS)" == "Windows_NT"
NULL=
!ELSE 
NULL=nul
!ENDIF 

!IF  "$(CFG)" == "TroveZip - Win32 Release"

OUTDIR=.\Release
INTDIR=.\Release
# Begin Custom Macros
OutDir=.\Release
# End Custom Macros

ALL : "$(OUTDIR)\com_go_trove_util_Deflater.dll"


CLEAN :
	-@erase "$(INTDIR)\adler32.obj"
	-@erase "$(INTDIR)\deflate.obj"
	-@erase "$(INTDIR)\Deflater.obj"
	-@erase "$(INTDIR)\trees.obj"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\version.res"
	-@erase "$(INTDIR)\zutil.obj"
	-@erase "$(OUTDIR)\com_go_trove_util_Deflater.dll"
	-@erase "$(OUTDIR)\com_go_trove_util_Deflater.exp"
	-@erase "$(OUTDIR)\com_go_trove_util_Deflater.lib"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP=cl.exe
CPP_PROJ=/nologo /MT /W3 /GX /O2 /I "zlib-1.1.3" /D "NDEBUG" /D "WIN32" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "TROVEZIP_EXPORTS" /Fp"$(INTDIR)\TroveZip.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 

.c{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cpp{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cxx{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.c{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cpp{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cxx{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

MTL=midl.exe
MTL_PROJ=/nologo /D "NDEBUG" /mktyplib203 /win32 
RSC=rc.exe
RSC_PROJ=/l 0x409 /fo"$(INTDIR)\version.res" /d "NDEBUG" 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\TroveZip.bsc" 
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /dll /incremental:no /pdb:"$(OUTDIR)\com_go_trove_util_Deflater.pdb" /machine:I386 /out:"$(OUTDIR)\com_go_trove_util_Deflater.dll" /implib:"$(OUTDIR)\com_go_trove_util_Deflater.lib" 
LINK32_OBJS= \
	"$(INTDIR)\adler32.obj" \
	"$(INTDIR)\deflate.obj" \
	"$(INTDIR)\Deflater.obj" \
	"$(INTDIR)\trees.obj" \
	"$(INTDIR)\zutil.obj" \
	"$(INTDIR)\version.res"

"$(OUTDIR)\com_go_trove_util_Deflater.dll" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ELSEIF  "$(CFG)" == "TroveZip - Win32 Debug"

OUTDIR=.\Debug
INTDIR=.\Debug
# Begin Custom Macros
OutDir=.\Debug
# End Custom Macros

ALL : "$(OUTDIR)\com_go_trove_util_Deflater.dll"


CLEAN :
	-@erase "$(INTDIR)\adler32.obj"
	-@erase "$(INTDIR)\deflate.obj"
	-@erase "$(INTDIR)\Deflater.obj"
	-@erase "$(INTDIR)\trees.obj"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\vc60.pdb"
	-@erase "$(INTDIR)\version.res"
	-@erase "$(INTDIR)\zutil.obj"
	-@erase "$(OUTDIR)\com_go_trove_util_Deflater.dll"
	-@erase "$(OUTDIR)\com_go_trove_util_Deflater.exp"
	-@erase "$(OUTDIR)\com_go_trove_util_Deflater.ilk"
	-@erase "$(OUTDIR)\com_go_trove_util_Deflater.lib"
	-@erase "$(OUTDIR)\com_go_trove_util_Deflater.pdb"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP=cl.exe
CPP_PROJ=/nologo /MTd /W3 /Gm /GX /ZI /Od /I "zlib-1.1.3" /D "_DEBUG" /D "WIN32" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "TROVEZIP_EXPORTS" /Fp"$(INTDIR)\TroveZip.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /GZ /c 

.c{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cpp{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cxx{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.c{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cpp{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cxx{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

MTL=midl.exe
MTL_PROJ=/nologo /D "_DEBUG" /mktyplib203 /win32 
RSC=rc.exe
RSC_PROJ=/l 0x409 /fo"$(INTDIR)\version.res" /d "_DEBUG" 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\TroveZip.bsc" 
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /dll /incremental:yes /pdb:"$(OUTDIR)\com_go_trove_util_Deflater.pdb" /debug /machine:I386 /out:"$(OUTDIR)\com_go_trove_util_Deflater.dll" /implib:"$(OUTDIR)\com_go_trove_util_Deflater.lib" /pdbtype:sept 
LINK32_OBJS= \
	"$(INTDIR)\adler32.obj" \
	"$(INTDIR)\deflate.obj" \
	"$(INTDIR)\Deflater.obj" \
	"$(INTDIR)\trees.obj" \
	"$(INTDIR)\zutil.obj" \
	"$(INTDIR)\version.res"

"$(OUTDIR)\com_go_trove_util_Deflater.dll" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ENDIF 


!IF "$(NO_EXTERNAL_DEPS)" != "1"
!IF EXISTS("TroveZip.dep")
!INCLUDE "TroveZip.dep"
!ELSE 
!MESSAGE Warning: cannot find "TroveZip.dep"
!ENDIF 
!ENDIF 


!IF "$(CFG)" == "TroveZip - Win32 Release" || "$(CFG)" == "TroveZip - Win32 Debug"
SOURCE=".\zlib-1.1.3\adler32.c"

"$(INTDIR)\adler32.obj" : $(SOURCE) "$(INTDIR)"
	$(CPP) $(CPP_PROJ) $(SOURCE)


SOURCE=".\zlib-1.1.3\deflate.c"

"$(INTDIR)\deflate.obj" : $(SOURCE) "$(INTDIR)"
	$(CPP) $(CPP_PROJ) $(SOURCE)


SOURCE=.\Deflater.cpp

"$(INTDIR)\Deflater.obj" : $(SOURCE) "$(INTDIR)"


SOURCE=".\zlib-1.1.3\trees.c"

"$(INTDIR)\trees.obj" : $(SOURCE) "$(INTDIR)"
	$(CPP) $(CPP_PROJ) $(SOURCE)


SOURCE=".\zlib-1.1.3\zutil.c"

"$(INTDIR)\zutil.obj" : $(SOURCE) "$(INTDIR)"
	$(CPP) $(CPP_PROJ) $(SOURCE)


SOURCE=.\version.rc

"$(INTDIR)\version.res" : $(SOURCE) "$(INTDIR)"
	$(RSC) $(RSC_PROJ) $(SOURCE)



!ENDIF 

