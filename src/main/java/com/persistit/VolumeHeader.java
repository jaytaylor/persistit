/**
 * Copyright © 2011-2012 Akiban Technologies, Inc.  All rights reserved.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.persistit.exception.CorruptVolumeException;
import com.persistit.exception.InvalidVolumeSpecificationException;
import com.persistit.exception.PersistitException;
import com.persistit.exception.PersistitIOException;
import com.persistit.util.Util;

/**
 * A volume header represents the header information at the beginning of each
 * volume file.
 */
class VolumeHeader {
    /**
     * Signature value - human and machine readable confirmation that this file
     * resulted from Persistit.
     */
    private final static byte[] SIGNATURE = Util.stringToBytes("PERSISTIT VOLUME");
    /**
     * Minimum possible size for a volume
     */
    private final static int SIZE = Buffer.MIN_BUFFER_SIZE;
    /**
     * Current product version number.
     */
    private final static int CURRENT_VERSION = 221;

    /**
     * Minimum product version that can handle Volumes created by this version.
     */
    private final static int MIN_SUPPORTED_VERSION = 210;

    /**
     * Minimum product version that can handle Volumes created by this version.
     */
    private final static int MAX_SUPPORTED_VERSION = 299;

    static boolean verifySignature(final byte[] bytes) {
        return Util.bytesEqual(bytes, 0, SIGNATURE);
    }

    static boolean putSignature(final byte[] bytes) {
        return Util.changeBytes(bytes, 0, SIGNATURE);
    }

    static int getVersion(final byte[] bytes) {
        return Util.getInt(bytes, 16);
    }

    static void putVersion(final byte[] bytes) {
        Util.putInt(bytes, 16, CURRENT_VERSION);
    }

    static int getPageSize(final byte[] bytes) {
        return Util.getInt(bytes, 20);
    }

    static void putPageSize(final byte[] bytes, final int value) {
        Util.putInt(bytes, 20, value);
    }

    static long getTimestamp(final byte[] bytes) {
        return Util.getLong(bytes, 24);
    }

    static boolean changeTimestamp(final byte[] bytes, final long value) {
        return Util.changeLong(bytes, 24, value);
    }

    static long getId(final byte[] bytes) {
        return Util.getLong(bytes, 32);
    }

    static void putId(final byte[] bytes, final long value) {
        Util.putLong(bytes, 32, value);
    }

    static long getReadCounter(final byte[] bytes) {
        return Util.getLong(bytes, 40);
    }

    static boolean changeReadCounter(final byte[] bytes, final long value) {
        return Util.changeLong(bytes, 40, value);
    }

    static long getWriteCounter(final byte[] bytes) {
        return Util.getLong(bytes, 48);
    }

    static boolean changeWriteCounter(final byte[] bytes, final long value) {
        return Util.changeLong(bytes, 48, value);
    }

    static long getGetCounter(final byte[] bytes) {
        return Util.getLong(bytes, 56);
    }

    static boolean changeGetCounter(final byte[] bytes, final long value) {
        return Util.changeLong(bytes, 56, value);
    }

    static long getOpenTime(final byte[] bytes) {
        return Util.getLong(bytes, 64);
    }

    static boolean changeOpenTime(final byte[] bytes, final long value) {
        return Util.changeLong(bytes, 64, value);
    }

    static long getCreateTime(final byte[] bytes) {
        return Util.getLong(bytes, 72);
    }

    static boolean changeCreateTime(final byte[] bytes, final long value) {
        return Util.changeLong(bytes, 72, value);
    }

    static long getLastReadTime(final byte[] bytes) {
        return Util.getLong(bytes, 80);
    }

    static boolean changeLastReadTime(final byte[] bytes, final long value) {
        return Util.changeLong(bytes, 80, value);
    }

    static long getLastWriteTime(final byte[] bytes) {
        return Util.getLong(bytes, 88);
    }

    static boolean changeLastWriteTime(final byte[] bytes, final long value) {
        return Util.changeLong(bytes, 88, value);
    }

    static long getLastExtensionTime(final byte[] bytes) {
        return Util.getLong(bytes, 96);
    }

    static boolean changeLastExtensionTime(final byte[] bytes, final long value) {
        return Util.changeLong(bytes, 96, value);
    }

    /**
     * +1 because the stored form of volumes created before 2.6.1 recorded
     * "highestUsedPage" rather than "nextAvailablePage" in this slot.
     * 
     * @param bytes
     * @return page address of the next available page
     */
    static long getNextAvailablePage(final byte[] bytes) {
        return Util.getLong(bytes, 104) + 1;
    }

    /**
     * -1 because the stored form of volumes created before 2.6.1 recorded
     * "highestUsedPage" rather than "nextAvailablePage" in this slot.
     * 
     * @param bytes
     * @param value
     *            next available page
     * @return whether the supplied value is different from the previously
     *         stored value
     */
    static boolean changeNextAvailablePage(final byte[] bytes, final long value) {
        return Util.changeLong(bytes, 104, value - 1);
    }

    static long getExtendedPageCount(final byte[] bytes) {
        return Util.getLong(bytes, 112);
    }

    static boolean changeExtendedPageCount(final byte[] bytes, final long value) {
        return Util.changeLong(bytes, 112, value);
    }

    static long getExtensionPages(final byte[] bytes) {
        return Util.getLong(bytes, 120);
    }

    static boolean changeExtensionPages(final byte[] bytes, final long value) {
        return Util.changeLong(bytes, 120, value);
    }

    static long getMaximumPages(final byte[] bytes) {
        return Util.getLong(bytes, 128);
    }

    static boolean changeMaximumPages(final byte[] bytes, final long value) {
        return Util.changeLong(bytes, 128, value);
    }

    static long getDirectoryRoot(final byte[] bytes) {
        return Util.getLong(bytes, 144);
    }

    static boolean changeDirectoryRoot(final byte[] bytes, final long value) {
        return Util.changeLong(bytes, 144, value);
    }

    static long getGarbageRoot(final byte[] bytes) {
        return Util.getLong(bytes, 152);
    }

    static boolean changeGarbageRoot(final byte[] bytes, final long value) {
        return Util.changeLong(bytes, 152, value);
    }

    static long getfetchCounter(final byte[] bytes) {
        return Util.getLong(bytes, 160);
    }

    static boolean changeFetchCounter(final byte[] bytes, final long value) {
        return Util.changeLong(bytes, 160, value);
    }

    static long getTraverseCounter(final byte[] bytes) {
        return Util.getLong(bytes, 168);
    }

    static boolean changeTraverseCounter(final byte[] bytes, final long value) {
        return Util.changeLong(bytes, 168, value);
    }

    static long getStoreCounter(final byte[] bytes) {
        return Util.getLong(bytes, 176);
    }

    static boolean changeStoreCounter(final byte[] bytes, final long value) {
        return Util.changeLong(bytes, 176, value);
    }

    static long getRemoveCounter(final byte[] bytes) {
        return Util.getLong(bytes, 184);
    }

    static boolean changeRemoveCounter(final byte[] bytes, final long value) {
        return Util.changeLong(bytes, 184, value);
    }

    static long getInitialPages(final byte[] bytes) {
        return Util.getLong(bytes, 192);
    }

    static boolean changeInitialPages(final byte[] bytes, final long value) {
        return Util.changeLong(bytes, 192, value);
    }

    static long getGlobalTimestamp(final byte[] bytes) {
        return Util.getLong(bytes, 200);
    }

    static boolean changeGlobalTimestamp(final byte[] bytes, final long value) {
        return Util.changeLong(bytes, 200, value);
    }

    /**
     * Validate that the header conforms to the volume header specification and
     * if so, read the pageSize and version values from it and populate the
     * corresponding fields of the supplied <code>VolumeSpecification</code>.
     * CorruptVolumeExceptions are thrown when an inconsistency is observed.
     * 
     * @return <code>true</code> if and only if there already exists a Volume
     *         file.
     * @throws PersistitException
     * @throws IOException
     * @throws InvalidVolumeSpecificationException
     * @throws CorruptVolumeException
     * @throws PersistitIOException
     */
    public static boolean verifyVolumeHeader(final VolumeSpecification specification, final long systemTimestamp) throws CorruptVolumeException,
            InvalidVolumeSpecificationException, PersistitIOException {
        try {
            final File file = new File(specification.getPath());
            if (file.exists()) {
                if (file.isFile()) {
                    final FileInputStream stream = new FileInputStream(file);
                    final byte[] bytes = new byte[SIZE];
                    int readSize = stream.read(bytes);
                    if (readSize < SIZE) {
                        throw new CorruptVolumeException("Volume file " + file + " too short: " + readSize);
                    }
                    /*
                     * Check out the fixed Volume file and learn the buffer
                     * size.
                     */
                    if (!verifySignature(bytes)) {
                        throw new CorruptVolumeException("Invalid signature");
                    }

                    int version = getVersion(bytes);
                    if (version < MIN_SUPPORTED_VERSION || version > MAX_SUPPORTED_VERSION) {
                        throw new CorruptVolumeException("Version " + version
                                + " is not supported by Persistit version " + Persistit.version());
                    }
                    int pageSize = getPageSize(bytes);
                    long nextAvailablePage = getNextAvailablePage(bytes);
                    long id = getId(bytes);
                    long totalPages = file.length() / pageSize;
                    if (totalPages < nextAvailablePage) {
                        throw new CorruptVolumeException(String.format("Volume has been truncated: "
                                + "minimum required/actual lengths=%,d/%,d bytes", nextAvailablePage * pageSize, file
                                .length()));
                    }
                    long globalTimestamp = getGlobalTimestamp(bytes);
                    if (globalTimestamp > systemTimestamp) {
                        throw new CorruptVolumeException("Volume " + file + " has a global timestamp greater than "
                                + "system timestamp: " + globalTimestamp + " > " + systemTimestamp);
                    }
                    specification.setVersion(version);
                    specification.setPageSize(pageSize);
                    specification.setId(id);
                } else {
                    throw new CorruptVolumeException("Volume file " + file + " is a directory");
                }
                return true;
            } else {
                return false;
            }
        } catch (IOException ioe) {
            throw new PersistitIOException(ioe);
        }
    }

    /**
     * @return The version number of volumes created by this version of
     *         Persistit.
     */
    public static int getCurrentVersion() {
        return CURRENT_VERSION;
    }

}
