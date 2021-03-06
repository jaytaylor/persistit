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

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;

import com.persistit.StatisticsTask.Display;
import com.persistit.StatisticsTask.Stat;
import com.persistit.unit.PersistitUnitTestCase;

public class StatisticsTaskTest extends PersistitUnitTestCase {

    @Test
    public void testStatFormat() throws Exception {
        final Stat stat = new Stat("foo");
        stat.update(0, 1234);
        stat.update(10000000000L, 2345);
        assertEquals("foo=2345", stat.toString(Display.TOTAL));
        assertEquals("foo=1111", stat.toString(Display.CHANGE));
        assertEquals("foo=111.100", stat.toString(Display.RATE));
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        sw.getBuffer().setLength(0);
        stat.printHeader(pw);
        assertEquals("        foo", sw.toString());
        sw.getBuffer().setLength(0);
        stat.printValue(pw, Display.TOTAL);
        assertEquals("      2,345", sw.toString());
        sw.getBuffer().setLength(0);
        stat.printValue(pw, Display.CHANGE);
        assertEquals("      1,111", sw.toString());
        sw.getBuffer().setLength(0);
        stat.printValue(pw, Display.RATE);
        assertEquals("    111.100", sw.toString());

    }

    @Test
    public void testStatisticsTask() throws Exception {

        final File file = File.createTempFile("statistics", ".log");
        file.deleteOnExit();
        final StatisticsTask task = (StatisticsTask) CLI.parseTask(_persistit, "stat -a -r delay=1 count=5 file="
                + file.getAbsolutePath());
        task.setMessageWriter(new PrintWriter(System.out));
        task.setup(1, "stats", "cls", 0, 5);
        task.run();
        final BufferedReader reader = new BufferedReader(new FileReader(file));
        int lines = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            lines++;
            System.out.println(line);
        }
        assertEquals(5, lines);
    }

    @Override
    public void runAllTests() throws Exception {
        // TODO Auto-generated method stub

    }

}
