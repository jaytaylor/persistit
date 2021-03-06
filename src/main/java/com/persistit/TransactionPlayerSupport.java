/**
 * Copyright © 2012 Akiban Technologies, Inc.  All rights reserved.
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

package com.persistit;

import java.nio.ByteBuffer;

import com.persistit.JournalManager.TreeDescriptor;
import com.persistit.exception.PersistitException;
import com.persistit.exception.PersistitIOException;

interface TransactionPlayerSupport {
    Persistit getPersistit();

    TreeDescriptor handleToTreeDescriptor(int treeHandle);

    Volume handleToVolume(int volumeHandle);

    void read(long address, int size) throws PersistitIOException;

    ByteBuffer getReadBuffer();

    void convertToLongRecord(Value value, int treeHandle, long address, long commitTimestamp) throws PersistitException;
}

