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
 * Created on Mar 19, 2004
 */
package com.persistit.stress;

import com.persistit.ArgParser;
import com.persistit.Exchange;
import com.persistit.Key;
import com.persistit.Value;
import com.persistit.exception.PersistitException;
import com.persistit.test.TestRunner.Result;

public class Stress5 extends StressBase {
    private final static String SHORT_DESCRIPTION =
        "Extreme variations in key and record length";

    private final static String LONG_DESCRIPTION =
        "   Inserts, reads and deletes key/value pairs with very long keys, \r\n"
            + "   and keys with large variation in elision count";

    @Override
    public String shortDescription() {
        return SHORT_DESCRIPTION;
    }

    @Override
    public String longDescription() {
        return LONG_DESCRIPTION;
    }

    private final static String[] ARGS_TEMPLATE =
        { "repeat|int:1:0:1000000000|Repetitions",
            "count|int:10000:0:1000000000|Number of nodes to populate",
            "size|int:4029:10:10000000|Data record size",
            "splay0|int:12:1:1000|Splay 0", "splay1|int:3:1:1000|Splay 1",
            "splay2|int:7:1:1000|Splay 2", };

    int _splay0;
    int _splay1;
    int _splay2;
    int _size;

    @Override
    public void setupTest(final String[] args) {
        _ap = new ArgParser("com.persistit.Stress5", args, ARGS_TEMPLATE);
        _total = _ap.getIntValue("count");
        _repeatTotal = _ap.getIntValue("repeat");
        _total = _ap.getIntValue("count");

        _splay0 = _ap.getIntValue("splay0");
        _splay1 = _ap.getIntValue("splay1");
        _splay2 = _ap.getIntValue("splay2");
        _size = _ap.getIntValue("size");
        _dotGranularity = 10000;

        try {
            // Exchange with Thread-private Tree
            _ex = getPersistit().getExchange("persistit", _rootName + _threadIndex, true);
            _exs = getPersistit().getExchange("persistit", "shared", true);
        } catch (final Exception ex) {
            handleThrowable(ex);
        }
    }

    /**
     * Implements tests with long keys and values of borderline length
     */
    @Override
    public void runTest() {
        final Value value = new Value(getPersistit());

        final int baselineCount = 500;
        final int keyLength = 2041;
        final int maxDepth = keyLength - 10;
        final int minDepth = 20;

        for (_repeat = 0; (_repeat < _repeatTotal) && !isStopped(); _repeat++) {
            try {
                println();
                println();
                println("Starting test cycle " + _repeat + " at " + tsString());
                describeTest("Deleting all records");
                setPhase("@");
                _ex.clear().remove(Key.GTEQ);
                println();

                describeTest("Creating baseline records");
                setPhase("a");
                for (_count = 0; (_count < baselineCount) && !isStopped(); _count++) {
                    setupKey(_ex, keyLength, keyLength - 5, _count, _count, '5');
                    setupTestValue(_ex, _count, _size);
                    _ex.store();
                    dot();
                }
                println();
                if (isStopped()) {
                    break;
                }

                describeTest("Creating eliding keys");
                setPhase("b");
                int depth;
                for (_count = 0, depth = maxDepth; (depth > minDepth)
                    && !isStopped(); depth -= _splay1, _count++) {
                    setupKey(_ex, keyLength, depth, minDepth
                        + (depth % _splay0), 55555 + depth, '5');
                    setupTestValue(_ex, 55555 + depth, _size);
                    _ex.store();
                    dot();
                }
                println();
                if (isStopped()) {
                    break;
                }

                setPhase("c");
                for (_count = 0, depth = maxDepth; (depth > minDepth)
                    && !isStopped(); depth -= _splay2, _count++) {
                    setupKey(_ex, keyLength, depth, minDepth
                        + (depth % _splay0), 55555 - depth, '5');
                    setupTestValue(_ex, 55555 - depth, _size);
                    _ex.store();
                    dot();
                }
                println();
                if (isStopped()) {
                    break;
                }

                describeTest("Verifying and removing eliding keys");
                setPhase("d");
                for (_count = 0, depth = maxDepth; (depth > minDepth)
                    && !isStopped(); depth -= _splay1, _count++) {
                    setupKey(_ex, keyLength, depth, minDepth
                        + (depth % _splay0), 55555 + depth, '5');
                    setupTestValue(_ex, 55555 + depth, _size);
                    _ex.fetch(value);
                    compareValues(_ex.getValue(), value);
                    if (isStopped()) {
                        break;
                    }
                    _ex.remove();
                    dot();
                }
                println();
                if (isStopped()) {
                    break;
                }

                setPhase("e");
                for (_count = 0, depth = maxDepth; (depth > minDepth)
                    && !isStopped(); depth -= _splay2, _count++) {
                    setupKey(_ex, keyLength, depth, minDepth
                        + (depth % _splay0), 55555 - depth, '5');
                    setupTestValue(_ex, 55555 - depth, _size);
                    _ex.fetch(value);
                    compareValues(_ex.getValue(), value);
                    if (isStopped()) {
                        break;
                    }
                    if (!_ex.remove()) {
                        _result =
                            new Result(false, "Failed to remove depth=" + depth);
                        forceStop();
                        break;
                    }
                    dot();
                }
                println();
                if (isStopped()) {
                    break;
                }

                describeTest("Deleting baseline records");
                setPhase("f");
                for (_count = 0; _count < baselineCount; _count++) {
                    setupKey(_ex, keyLength, keyLength - 5, _count, _count, '5');
                    if (!_ex.remove()) {
                        println("Failed to remove counter=" + _count);
                    }
                    dot();
                }
                println();

                describeTest("Verifying empty tree");
                setPhase("g");
                _ex.clear();
                if (_ex.traverse(Key.GT, true)) {
                    println("Tree is not empty");
                }
                println();

            } catch (final PersistitException de) {
                handleThrowable(de);
            }
            println("Done at " + tsString());
        }
    }

    private void setupKey(final Exchange ex, final int length, final int depth,
        final int a, final int b, final char fill) {
        _sb1.setLength(0);
        for (int i = 0; i < length; i++) {
            _sb1.append(fill);
        }
        fillLong(b, depth, 5, true);
        ex.clear().append(a).append(_sb1);
    }

    private void describeTest(final String m) {
        print(m);
        print(": ");
        for (int i = m.length(); i < 52; i++) {
            print(" ");
        }
    }

    public static void main(final String[] args) throws Exception {
        new Stress5().runStandalone(args);
    }
}