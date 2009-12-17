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
 * Created on Feb 24, 2004
 */
package com.persistit.stress;

import java.util.Arrays;

import com.persistit.ArgParser;
import com.persistit.Exchange;
import com.persistit.Key;
import com.persistit.Value;
import com.persistit.exception.PersistitException;

/**
 * A basic test sequence:
 * 
 * 1. Delete all � to clean 2. Sequential Fixed-length set/get/traverse/reverse
 * traverse/remove 3. Child key insert / traverse / reverse /remove 4.
 * Sequential Replacement / Elongation / Shortening 5. Random insertion /
 * replacement / elongation / shortening 6. Long records insert / grow / shrink /
 * remove 7. Spotty removes 8. Key range remove
 * 
 */
public abstract class Stress4Base extends StressBase {
    protected final static String[] ARGS_TEMPLATE =
        { "repeat|int:1:0:1000000000|Repetitions",
            "count|int:10000:0:1000000000|Number of nodes to populate",
            "seed|int:1:1:20000|Random seed", "splay|int:1:1:1000|Splay", };

    private int[] _checksum;

    protected int _splay;
    protected int _seed;

    private Exception _mostRecentException;

    @Override
    public void setupTest(final String[] args) {
        _ap = new ArgParser("com.persistit.Stress4", args, ARGS_TEMPLATE);
        _total = _ap.getIntValue("count");
        _repeatTotal = _ap.getIntValue("repeat");
        _total = _ap.getIntValue("count");
        _seed = _ap.getIntValue("seed");
        _splay = _ap.getIntValue("splay");
        _dotGranularity = 10000;

        try {
            // Exchange with Thread-private Tree
            _ex = getPersistit().getExchange("persistit", _rootName + _threadIndex, true);
            _exs = getPersistit().getExchange("persistit", "shared", true);
        } catch (final Exception ex) {
            handleThrowable(ex);
        }
    }

    public abstract void repeatedTasks() throws PersistitException;

    @Override
    public void runTest() {
        try {
            _checksum = new int[_total];

            setPhase("@");
            try {
                _ex.clear().remove(Key.GTEQ);
            } catch (final Exception e) {
                handleThrowable(e);
            }
            println();

            for (_repeat = 0; (_repeat < _repeatTotal) && !isStopped(); _repeat++) {
                println();
                println("Starting cycle " + (_repeat + 1) + " of "
                    + _repeatTotal);

                repeatedTasks();
                println();
            }
        } catch (final Throwable t) {
            handleThrowable(t);
        }
    }

    public void clean() throws PersistitException {
        _ex.clear().remove();
        Arrays.fill(_checksum, 0);
    }

    public boolean testReads(final int to) throws PersistitException {
        for (_count = 0; (_count < _total) && (_count < to) && !isStopped(); _count++) {
            _ex.clear().append(_count).fetch();
            final int cksum = checksum(_ex.getValue());
            if (cksum != _checksum[_count]) {
                return false;
            }
        }
        return true;
    }

    public boolean testForward() throws PersistitException {
        _ex.clear().append(Key.BEFORE);
        int index1 = 0;
        int index2;
        while (_ex.next() && !isStopped()) {
            index2 = (int) (_ex.getKey().decodeLong());
            for (int i = index1; i < index2; i++) {
                if (_checksum[i] != -1) {
                    return false;
                }
            }
            index1 = index2 + 1;
            final int cksum = checksum(_ex.getValue());
            if ((index2 < 0) || (index2 >= _total)) {
                return false;
            }
            if (cksum != _checksum[index2]) {
                return false;
            }
        }
        for (int i = index1; i < _total; i++) {
            if (_checksum[i] != -1) {
                return false;
            }
        }
        return true;
    }

    public boolean testReverse() throws PersistitException {
        _ex.clear().append(Key.AFTER);
        int index1 = _total - 1;
        int index2;
        while (_ex.previous() && !isStopped()) {
            index2 = (int) (_ex.getKey().decodeLong());
            for (int i = index1; i > index2; i--) {
                if (_checksum[i] != -1) {
                    return false;
                }
            }
            index1 = index2 - 1;
            final int cksum = checksum(_ex.getValue());
            if ((index2 < 0) || (index2 >= _total)) {
                return false;
            }
            if (cksum != _checksum[index2]) {
                return false;
            }
        }
        for (int i = index1; i >= 0; i--) {
            if (_checksum[i] != -1) {
                return false;
            }
        }

        return true;
    }

    public void writeRecords(final int to, final boolean random,
        final int minsize, final int maxsize) throws PersistitException {
        for (_count = 0; (_count < to) && !isStopped(); _count++) {
            dot();
            int keyInteger;
            if (random) {
                keyInteger = keyInteger(_count);
            } else {
                keyInteger = _count;
            }
            _ex.clear().append(keyInteger);
            int size = minsize;
            if (minsize != maxsize) {
                size = random(minsize, maxsize);
            }
            setupTestValue(_ex, _count, size);
            _ex.store();
            _checksum[keyInteger] = checksum(_ex.getValue());
        }
    }

    public void readRecords(final int to, final boolean random,
        final int minsize, final int maxsize) throws PersistitException {
        final Value value1 = _ex.getValue();
        final Value value2 = new Value(getPersistit());

        for (_count = 0; (_count < to) && !isStopped(); _count++) {
            dot();
            int keyInteger;
            if (random) {
                keyInteger = keyInteger(_count);
            } else {
                keyInteger = _count;
            }
            _ex.clear().append(keyInteger);
            int size = minsize;
            if (minsize != maxsize) {
                size = random(minsize, maxsize);
            }
            setupTestValue(_ex, _count, size);
            // fetch to a different Value object so we can compare
            // with the original.
            _ex.fetch(value2);
            compareValues(value1, value2);
        }
    }

    public void removeRecords(final int to, final boolean random)
        throws PersistitException {
        for (_count = 0; (_count < _total) && (_count < to) && !isStopped(); _count++) {
            dot();
            int keyInteger;
            if (random) {
                keyInteger = keyInteger(_count);
            } else {
                keyInteger = _count;
            }

            _ex.clear().append(keyInteger);
            _ex.remove();
            _checksum[keyInteger] = -1;
        }
    }

    public void writeLongKey(final int keyInteger, final int length,
        final int valueSize) throws PersistitException {

    }

    public void readLongKey(final int keyInteger, final int length,
        final int valueSize) throws PersistitException {
    }

    public void removeLongKey(final int keyInteger, final int length)
        throws PersistitException {
    }

    public void sleep() {
        try {
            Thread.sleep(1000);
        } catch (final Exception e) {
        }
    }

    int keyInteger(final int counter) {
        int keyInteger = (counter * _splay) % _total;
        if (keyInteger < 0) {
            keyInteger += _total;
        }
        return keyInteger;
    }

}