# Microsoft Developer Studio Generated NMAKE File, Based on SystemFileBuffer.dsp
!IF "$(CFG)" == ""
CFG=SystemFileBuffer - Win32 Debug
!MESSAGE No configuration specified. Defaulting to SystemFileBuffer - Win32 Debug.
!ENDIF 

!IF "$(CFG)" != "SystemFileBuffer - Win32 Release" && "$(CFG)" != "SystemFileBuffer - Win32 Debug"
!MESSAGE Invalid configuration "$(CFG)" specified.
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "SystemFileBuffer.mak" CFG="SystemFileBuffer - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "SystemFileBuffer - Win32 Release" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE "SystemFileBuffer - Win32 Debug" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE 
!ERROR An invalid configuration is specified.
!ENDIF 

!IF "$(OS)" == "Windows_NT"
NULL=
!ELSE 
NULL=nul
!ENDIF 

!IF  "$(CFG)" == "SystemFileBuffer - Win32 Release"

OUTDIR=.\Release
INTDIR=.\Release
# Begin Custom Macros
OutDir=.\Release
# End Custom Macros

ALL : "$(OUTDIR)\com_go_trove_file_SystemFileBuffer.dll"


CLEAN :
	-@erase "$(INTDIR)\SystemFileBuffer.obj"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\version.res"
	-@erase "$(OUTDIR)\com_go_trove_file_SystemFileBuffer.dll"
	-@erase "$(OUTDIR)\com_go_trove_file_SystemFileBuffer.exp"
	-@erase "$(OUTDIR)\com_go_trove_file_SystemFileBuffer.lib"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP=cl.exe
CPP_PROJ=/nologo /MT /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "SYSTEMFILEBUFFER_EXPORTS" /Fp"$(INTDIR)\SystemFileBuffer.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 

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
BSC32_FLAGS=/nologo /o"$(OUTDIR)\SystemFileBuffer.bsc" 
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /dll /incremental:no /pdb:"$(OUTDIR)\com_go_trove_file_SystemFileBuffer.pdb" /machine:I386 /out:"$(OUTDIR)\com_go_trove_file_SystemFileBuffer.dll" /implib:"$(OUTDIR)\com_go_trove_file_SystemFileBuffer.lib" 
LINK32_OBJS= \
	"$(INTDIR)\SystemFileBuffer.obj" \
	"$(INTDIR)\version.res"

"$(OUTDIR)\com_go_trove_file_SystemFileBuffer.dll" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ELSEIF  "$(CFG)" == "SystemFileBuffer - Win32 Debug"

OUTDIR=.\Debug
INTDIR=.\Debug
# Begin Custom Macros
OutDir=.\Debug
# End Custom Macros

ALL : "$(OUTDIR)\com_go_trove_file_SystemFileBuffer.dll"


CLEAN :
	-@erase "$(INTDIR)\SystemFileBuffer.obj"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\vc60.pdb"
	-@erase "$(INTDIR)\version.res"
	-@erase "$(OUTDIR)\com_go_trove_file_SystemFileBuffer.dll"
	-@erase "$(OUTDIR)\com_go_trove_file_SystemFileBuffer.exp"
	-@erase "$(OUTDIR)\com_go_trove_file_SystemFileBuffer.ilk"
	-@erase "$(OUTDIR)\com_go_trove_file_SystemFileBuffer.lib"
	-@erase "$(OUTDIR)\com_go_trove_file_SystemFileBuffer.pdb"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP=cl.exe
CPP_PROJ=/nologo /MTd /W3 /Gm /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "SYSTEMFILEBUFFER_EXPORTS" /Fp"$(INTDIR)\SystemFileBuffer.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /GZ /c 

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
BSC32_FLAGS=/nologo /o"$(OUTDIR)\SystemFileBuffer.bsc" 
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /dll /incremental:yes /pdb:"$(OUTDIR)\com_go_trove_file_SystemFileBuffer.pdb" /debug /machine:I386 /out:"$(OUTDIR)\com_go_trove_file_SystemFileBuffer.dll" /implib:"$(OUTDIR)\com_go_trove_file_SystemFileBuffer.lib" /pdbtype:sept 
LINK32_OBJS= \
	"$(INTDIR)\SystemFileBuffer.obj" \
	"$(INTDIR)\version.res"

"$(OUTDIR)\com_go_trove_file_SystemFileBuffer.dll" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ENDIF 


!IF "$(NO_EXTERNAL_DEPS)" != "1"
!IF EXISTS("SystemFileBuffer.dep")
!INCLUDE "SystemFileBuffer.dep"
!ELSE 
!MESSAGE Warning: cannot find "SystemFileBuffer.dep"
!ENDIF 
!ENDIF 


!IF "$(CFG)" == "SystemFileBuffer - Win32 Release" || "$(CFG)" == "SystemFileBuffer - Win32 Debug"
SOURCE=.\SystemFileBuffer.cpp

"$(INTDIR)\SystemFileBuffer.obj" : $(SOURCE) "$(INTDIR)"


SOURCE=.\version.rc

"$(INTDIR)\version.res" : $(SOURCE) "$(INTDIR)"
	$(RSC) $(RSC_PROJ) $(SOURCE)



!ENDIF 

