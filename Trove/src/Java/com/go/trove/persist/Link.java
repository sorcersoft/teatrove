/*
 * Link.java
 * 
 * Copyright (c) 2002 Walt Disney Internet Group. All Rights Reserved.
 * 
 * Original author: Brian S O'Neill
 * 
 * $Workfile:: Link.java                                                      $
 *   $Author$
 * $Revision$
 *     $Date$
 */

package com.go.trove.persist;

import java.io.*;

/******************************************************************************
 * When a Link is saved into an ObjectRepository, the linked value is saved
 * in a separate ObjectRepository file. When a Link is read, the linked value
 * is not retrieved until the first time "get" is called.
 * <p>
 * If a Link is written to an ObjectOutputStream, but not by an
 * ObjectRepository, the linked value is dropped. Likewise, if a Link is read
 * from an ObjectInputStream, but not by an ObjectRepository, the linked value
 * is null.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision$-->, <!--$$JustDate:--> 02/02/20 <!-- $-->
 * @see ObjectRepository#local
 */
public class Link implements Externalizable {
    static final long serialVersionUID = 1;

    private transient volatile Object mValue;

    // Fields used for lazily retrieving value.
    private transient long mId;
    private transient volatile ObjectRepository mRepository;

    public Link() {
    }

    public Link(Object value) {
        mValue = value;
    }

    public Object get() throws IOException, ClassNotFoundException {
        if (mValue == null) {
            ObjectRepository repository = mRepository;
            if (repository != null) {
                mValue = repository.retrieveObject(mId);
                mRepository = null;
            }
        }
        return mValue;
    }

    public void set(Object value) {
        mRepository = null;
        mValue = value;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        Object value;
        try {
            value = get();
        }
        catch (IOException e) {
            value = null;
        }
        catch (ClassNotFoundException e) {
            value = null;
        }
        ObjectRepository rep = ObjectRepository.local.get();
        out.writeLong(rep == null ? 0 : rep.saveObject(value));
    }
    
    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException
    {
        mId = in.readLong();
        mRepository = ObjectRepository.local.get();
    }
}
