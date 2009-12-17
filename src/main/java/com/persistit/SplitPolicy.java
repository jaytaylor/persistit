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
 */
 
package com.persistit;

/**
 * @author pbeaman
 */
public abstract class SplitPolicy
{
    public final static SplitPolicy LEFT_BIAS = new Left();
    public final static SplitPolicy RIGHT_BIAS = new Right();
    public final static SplitPolicy EVEN_BIAS = new Even();
    public final static SplitPolicy NICE_BIAS = new Nice();
    
    /**
     * Determines the quality of fit for a specified candidate split location
     * within a page.  Returns 0 if no split is possible, or a positive integer
     * if a split is possible.  The caller will call splitFit to evaluate
     * several possible split locations, and will choose the one yielding the
     * largest value of this method.
     * 
     * @param buffer        the buffer being split.
     * @param kbOffset      offset of the proposed keyblock that will become
     *                      the first key of the right sibling page.
     * @param insertAt      offset of the keyblock where the record insertion
     *                      will occur.
     * @param replace       <i>true</i> if the record at kbOffset is being
     *                      replaced
     * @param leftSize      size of the left sibling page that would result if
     *                      the split were done at this candidate location.
     * @param rightSize     size of the right sibling page that would result if
     *                      the split were done at this candidate location.
     * @param currentSize   size of the page prior to insertion.
     * @param virtualSize   size of the page that would result where the
     *                      insertion to occur without splitting the page.
     *                      (This is usually larger than the actual capacity
     *                      of the buffer, which is why it needs to be split.)
     * @param capacity      Actual available bytes in a page.
     * @param splitBest     the previous best-fit result from this method, or 0
     *                      if there is no candidate split location yet.
     * @return              measure of goodness of fit.
     */
    abstract int splitFit(
        Buffer buffer,
        int kbOffset,
        int insertAt,
        boolean replace,
        int leftSize,
        int rightSize,
        int currentSize,
        int virtualSize,
        int capacity,
        int splitInfo);
    
    private static class Left
    extends SplitPolicy
    {
        
        int splitFit(
            Buffer buffer,
            int kbOffset,
            int insertAt,
            boolean replace,
            int leftSize,
            int rightSize,
            int currentSize,
            int virtualSize,
            int capacity,
            int splitInfo)
        {
            //
            // This implementation minimizes the difference -- i.e., attempts
            // to split the page into equally sized siblings.
            //
            if (leftSize > capacity || rightSize > capacity) return 0;
            return leftSize;
        }
        
        public String toString()
        {
            return "LEFT_BIAS";
        }
    }
    
    private static class Right
    extends SplitPolicy
    {
        int splitFit(
            Buffer buffer,
            int kbOffset,
            int insertAt,
            boolean replace,
            int leftSize,
            int rightSize,
            int currentSize,
            int virtualSize,
            int capacity,
            int splitInfo)
        {
            //
            // This implementation minimizes the difference -- i.e., attempts
            // to split the page into equally sized siblings.
            //
            if (leftSize > capacity || rightSize > capacity) return 0;
            return rightSize;
        }
        
        public String toString()
        {
            return "RIGHT_BIAS";
        }
    }

    private static class Even
    extends SplitPolicy
    {
        int splitFit(
            Buffer buffer,
            int kbOffset,
            int insertAt,
            boolean replace,
            int leftSize,
            int rightSize,
            int currentSize,
            int virtualSize,
            int capacity,
            int splitInfo)
        {
            //
            // This implementation minimizes the difference -- i.e., attempts
            // to split the page into equally sized siblings.
            //
            if (leftSize > capacity || rightSize > capacity) return 0;
            int difference = rightSize - leftSize;
            if (difference < 0) difference = -difference;
            return capacity - difference;
        }
        
        public String toString()
        {
            return "EVEN_BIAS";
        }
    }

    private static class Nice
    extends SplitPolicy
    {
        int splitFit(
            Buffer buffer,
            int kbOffset,
            int insertAt,
            boolean replace,
            int leftSize,
            int rightSize,
            int currentSize,
            int virtualSize,
            int capacity,
            int splitInfo)
        {
            //
            // This implementation optimizes toward a 66/34 split - i.e.,
            // biases toward splitting 66% of the records into the
            // left page and 33% into the right page.
            //
            if (leftSize > capacity || rightSize > capacity) return 0;
            int difference = 2 * rightSize - leftSize;
            if (difference < 0) difference = -difference;
            return capacity * 2 - difference;
        }
        
        public String toString()
        {
            return "NICE_BIAS";
        }
    }

}