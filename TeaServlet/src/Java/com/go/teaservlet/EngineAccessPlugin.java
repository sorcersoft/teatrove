/*
 * EngineAccessPlugin.java
 * 
 * Copyright (c) 2001 Walt Disney Internet Group. All Rights Reserved.
 * 
 * Original author: Jonathan Colwell
 * 
 * $Workfile:: EngineAccessPlugin.java                                        $
 *   $Author$
 * $Revision$
 *     $Date$
 */

package com.go.teaservlet;

import com.go.trove.util.plugin.Plugin;
import com.go.trove.util.plugin.PluginConfig;
import com.go.trove.util.plugin.PluginEvent;

/******************************************************************************
 * 
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:-->  1/30/02 <!-- $-->
 */
public class EngineAccessPlugin implements Plugin, EngineAccess {

    TeaServletEngine mEngine;

    EngineAccessPlugin(TeaServletEngine engine) {
        mEngine = engine;
    }

    // Plugin methods

    public void init(PluginConfig conf) {
    }

    public void destroy() {

    }

    public String getName() {
        return mEngine.getName();
    }

    public void pluginAdded(PluginEvent pe) {

    }

    // EngineAccess methods

    public TeaServletEngine getTeaServletEngine() {
        return mEngine;
    }
}
