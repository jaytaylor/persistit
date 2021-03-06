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

package com.persistit.encoding;

/**
 * <p>
 * Combines the {@link KeyCoder}, {@link KeyRenderer}, {@link ValueCoder} and
 * {@link ValueRenderer} into a single interface that allows Persistit to store
 * and retrieve arbitrary objects - even non-Serializable objects - without
 * byte-code enhancement, without incurring the space or time overhead of Java
 * serialization, or the need to modify the class to perform custom
 * serialization. During initialization, an application typically associates an
 * <code>ObjectCoder</code> with each the <code>Class</code> of each object that
 * will be stored in or fetched from the Persistit database. The
 * <code>ObjectCoder</code> implements all of the logic necessary to encode and
 * decode the state of objects of that class to and from Persistit storage
 * structures. Although Persistit is not designed to provide transparent
 * persistence, the <code>ObjectCoder</code> interface simplifies object
 * persistence code.
 * </p>
 * 
 * 
 * @version 1.0
 */
public interface ObjectCoder extends KeyRenderer, ValueRenderer {
}
