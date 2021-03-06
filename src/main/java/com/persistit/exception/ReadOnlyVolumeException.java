/**
 * Copyright © 2005-2012 Akiban Technologies, Inc.  All rights reserved.
 * 
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This program may also be available under different license terms.
 * For more information, see www.akiban.com or contact licensing@akiban.com.
 * 
 * Contributors:
 * Akiban Technologies, Inc.
 */

package com.persistit.exception;

/**
 * Thrown when an application attempts to modify data in a
 * {@link com.persistit.Volume} that is open in read-only mode.
 * 
 * @version 1.0
 */
public class ReadOnlyVolumeException extends PersistitException {
    private static final long serialVersionUID = 4169989056763364382L;

    public ReadOnlyVolumeException() {
        super();
    }

    public ReadOnlyVolumeException(String msg) {
        super(msg);
    }
}
