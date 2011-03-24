/**
 * Copyright (C) 2011 Akiban Technologies Inc.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses.
 */

package com.persistit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

public class TimestampAllocator {

    /**
     * Default interval in nanoseconds between checkpoints - one minute.
     */
    private final static long DEFAULT_CHECKPOINT_INTERVAL = 60000000000L;

    private final AtomicLong _timestamp = new AtomicLong();

    private Checkpoint _checkpoint = new Checkpoint(0, 0);

    private long _lastCheckpointNanos;

    private volatile long _checkpointInterval = DEFAULT_CHECKPOINT_INTERVAL;

    /**
     * A component that maintains state outside of Persistit B-Trees can
     * register a <code>CheckpointListener</code> to receive calls when
     * checkpoint events are occurring. The <code>save</code> method should save
     * any updates created before the checkpoint timestamp to Persistit. If it
     * is able to successfully do so, then <code>save</code> should return
     * <code>true</code> to indicate that the checkpoint can proceed.
     * <p>
     * For example, an in-memory component that keeps aggregated statistics on
     * various records, and which needs to have its aggregation saved in a state
     * consistent with a Checkpoint, should register a
     * <code>CheckpointListener</code>.
     */
    public interface CheckpointListener {

        /**
         * Verify that all data updated before the specified checkpoint has been
         * written. This method may write data to B-Trees. The return value
         * indicates whether all updates for timestamps before the given
         * checkpoint have been written.
         * 
         * @param checkpoint
         * @return <code>true</code> iff all pending updates have been written.
         */
        boolean save(final Checkpoint checkpoint);

        /**
         * Indicates that the checkpoint has been made final. This affords an
         * opportunity to move obsolete memory structures.
         * 
         * @param checkpoint
         */
        void done(final Checkpoint checkpoint);
    }

    public static class Checkpoint {

        private final static SimpleDateFormat SDF = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");

        private final long _timestamp;

        private final long _systemTime;

        public Checkpoint(final long timestamp, final long systemTime) {
            _timestamp = timestamp;
            _systemTime = systemTime;
        }

        public long getTimestamp() {
            return _timestamp;
        }

        public long getSystemTimeMillis() {
            return _systemTime;
        }

        public String toString() {
            return String.format("Checkpoint %,d @ %s", _timestamp,
                    SDF.format(new Date(_systemTime)));
        }

    }

    public long updateTimestamp() {
        return _timestamp.incrementAndGet();
    }

    public long updateTimestamp(final long timestamp) {
        _timestamp.incrementAndGet();
        while (true) {
            final long expected = _timestamp.get();
            if (expected < timestamp) {
                if (_timestamp.compareAndSet(expected, timestamp)) {
                    return timestamp;
                }
            } else {
                return expected;
            }
        }
    }

    public long getCurrentTimestamp() {
        return _timestamp.get();
    }

    public synchronized Checkpoint updatedCheckpoint() {
        final long now = System.nanoTime();
        if (_lastCheckpointNanos + _checkpointInterval < now) {
            _lastCheckpointNanos = now;
            return forceCheckpoint();
        } else {
            return _checkpoint;
        }
    }

    public synchronized Checkpoint forceCheckpoint() {
        final long checkpointTimestamp = _timestamp.addAndGet(10000);
        _checkpoint = new Checkpoint(checkpointTimestamp,
                System.currentTimeMillis());
        return _checkpoint;
    }

    public synchronized Checkpoint getCurrentCheckpoint() {
        return _checkpoint;
    }

    public long getCheckpointInterval() {
        return _checkpointInterval;
    }

    public void setCheckpointInterval(long checkpointInterval) {
        _checkpointInterval = checkpointInterval;
    }

}
