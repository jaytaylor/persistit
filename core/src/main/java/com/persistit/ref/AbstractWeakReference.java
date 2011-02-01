/*
 * Copyright (c) 2004 Persistit Corporation. All Rights Reserved.
 *
 * The Java source code is the confidential and proprietary information
 * of Persistit Corporation ("Confidential Information"). You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Persistit Corporation.
 *
 * PERSISTIT CORPORATION MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. PERSISTIT CORPORATION SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 * 
 * Created on Jun 17, 2004
 */
package com.persistit.ref;

import java.lang.ref.WeakReference;

/**
 * <p>
 * An abstract superclass for implementations of {@link PersistitReference} that
 * implement weak reference semantics. A concrete subclass implements the
 * method: <blockquote>
 * 
 * <pre>
 *      public Object lookup(Object id)
 * </pre>
 * 
 * </blockquote> which should return the Object associated with the supplied id,
 * or <tt>null</tt> if there is none. The meaning of the identifier, and the
 * mechanism used to look up and deserialize the associated object (the
 * referent), are implementation-specific.
 * </p>
 * <p>
 * This implementation differs from {@link AbstractReference} in that the
 * referent object is referenced through a <tt>java.lang.ref.WeakReference</tt>.
 * Thus the garbage collector may choose to discard the referent value, in which
 * case the <tt>get</tt> method will simply look it up again on demand.
 * </p>
 * 
 * @version 1.0
 */
public abstract class AbstractWeakReference implements PersistitReference {
    protected Object _id;
    protected transient WeakReference _weakReference;
    protected boolean _knownNull;

    /**
     * No-arg constructor supplied for object serialization/deserialization.
     * 
     */
    protected AbstractWeakReference() {
    }

    /**
     * Construct a reference to the referent Object with the supplied persistent
     * identifier. For correct operation, the referent Object must be equivalent
     * to the object that would result from invoking <tt>lookup</tt> on the
     * persistent identifier. the object that would be returned by the lookup
     * 
     * @param id
     *            The persistent identifier. The value of the id must be
     *            associated with a unique referent object, and must be stable
     *            over time.
     * @param referent
     *            The object identified by the id
     */
    protected AbstractWeakReference(Object id, Object referent) {
        _id = id;
        if (referent == null)
            _knownNull = true;
        else
            _weakReference = new WeakReference(referent);
    }

    /**
     * Construct a reference using the persistent identity of an object. A
     * subsequent invocation of the <tt>get</tt> method will cause the object to
     * be looked up and instantiated.
     * 
     * @param id
     */
    protected AbstractWeakReference(Object id) {
        _id = id;
        _weakReference = null;
        _knownNull = false;
    }

    /**
     * Gets the referent object. If the object has already been looked up, or if
     * this reference was created using the two-argument constructor, then this
     * merely returns the object. Otherwise this method attempts to look up and
     * instantiate the object using its persistent identifier.
     * 
     * @return The referent object.
     */
    public Object get() {
        if (_id == null) {
            throw new IllegalStateException("identifier not initialized");
        }
        if (_knownNull)
            return null;
        Object referent = null;
        if (_weakReference != null)
            referent = _weakReference.get();
        if (referent == null) {
            referent = lookup(_id);
            if (referent == null)
                _knownNull = true;
            else
                _weakReference = new WeakReference(referent);
        }
        return _weakReference;
    }

    /**
     * Look up and instantiate an object using its persistent identifier.
     * Typically this will be done by setting up a
     * {@link com.persistit.Exchange} and fetching its value.
     * 
     * @param id
     *            The identifier
     * @return The referent object.
     */
    protected abstract Object lookup(Object id);

}