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

import java.util.AbstractSequentialList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.persistit.Value;
import com.persistit.exception.ConversionException;

/**
 * <p>
 * Implements ValueCoder, ValueRenderer and ValueDisplayer for selected classes
 * in the Java Collections API. By default an instance of this coder is
 * registered for each of the following concrete classes:
 * <ul>
 * <li>java.util.ArrayList</li>
 * <li>java.util.LinkedList</li>
 * <li>java.util.Stack</li>
 * <li>java.util.Vector</li>
 * <li>java.util.Properties</li>
 * <li>java.util.HashMap</li>
 * <li>java.util.HashSet</li>
 * <li>java.util.Hashtable</li>
 * <li>java.util.TreeMap</li>
 * <li>java.util.TreeSet</li>
 * <li>java.util.LinkedHashMap (Java 1.4+)</li>
 * <li>java.util.LinkedHashSet (Java 1.4+)</li>
 * </ul>
 * </p>
 * <p>
 * The stored data consist only of items accessible through the
 * <code>Collection</code> or <code>Map</code> interfaces. For example,
 * implementation details such as the current loadFactor for a
 * <code>HashMap</code> are not stored. For any <code>Collection</code>
 * (including <code>List</code> and <code>Set</code>), the stored format is
 * simply a list of all the values in the order returned by an
 * <code>Iterator</code>. For a <code>Map</code> the stored format is simply a
 * key/value pair for each entry in the order returned by the
 * <code>Iterator</code> returned by the <code>entrySet()</code> method of the
 * <code>Map</code>.
 * </p>
 * <p>
 * Because customer-written <code>Collection</code> and <code>Map</code>
 * implementations may contain additional internal state that may also need to
 * be stored when they are serialized, this coder is registered only for
 * specific implementations provided by the JRE. You may register and use this
 * ValueCoder for any custom <code>Collection</code> or <code>Map</code> that
 * does not need to serialize additional data.
 * </p>
 * <p>
 * Note that for <code>ArrayList</code>, <code>Vector</code>, <code>Stack</code>
 * , and other <code>List</code> implementations that do not extend
 * <code>AbstractSequentialList</code>, the serialization logic uses the
 * <code>get(index)</code> method of <code>List</code> to acquire each member
 * rather than constructing an <code>Iterator</code>. Subclasses of
 * <code>AbstractSequentialList</code> are serialized by using an
 * <code>Iterator</code> because access by index may be inefficient.
 * </p>
 * 
 * @since 1.1
 * @version 1.1
 */
public class CollectionValueCoder implements ValueRenderer, ValueDisplayer {

    /**
     * <p>
     * Encodes the supplied <code>Object</code> into the supplied
     * <code>Value</code>. This method will be called only if this
     * <code>ValueCoder</code> has been registered with the current
     * {@link CoderManager} to encode objects having the class of the supplied
     * object.
     * </p>
     * <p>
     * Upon completion of this method, the backing byte array of the
     * <code>Value</code> and its size should be updated to reflect the
     * serialized collection. Use the methods {@link Value#getEncodedBytes},
     * {@link Value#getEncodedSize} and {@link Value#setEncodedSize} to
     * manipulate the byte array directly. More commonly, the implementation of
     * this method will simply call the appropriate <code>put</code> methods to
     * write the interior field values into the <code>Value</code> object.
     * </p>
     * 
     * @param value
     *            The <code>Value</code> to which the interior data of the
     *            supplied <code>Object</code> should be encoded
     * @param object
     *            The object value to encode. This parameter will never be
     *            <code>null</code> because Persistit encodes nulls with a
     *            built-in encoding.
     * @param context
     *            An arbitrary object that can optionally be supplied by the
     *            application to convey an application-specific context for the
     *            operation. (See {@link CoderContext}.) The default value is
     *            <code>null</code>.
     */
    @Override
    public void put(Value value, Object object, CoderContext context) throws ConversionException {
        if (object instanceof Map<?, ?>) {
            for (Iterator<Map.Entry<?, ?>> iter = ((Map) object).entrySet().iterator(); iter.hasNext();) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) iter.next();
                value.put(entry.getKey(), context);
                value.put(entry.getValue(), context);
            }
        } else if (object instanceof List<?> && !(object instanceof AbstractSequentialList)) {
            List<?> list = (List<?>) object;
            int size = list.size();
            for (int index = 0; index < size; index++) {
                value.put(list.get(index), context);
            }
        } else if (object instanceof Collection<?>) {
            for (Iterator<?> iter = ((Collection<?>) object).iterator(); iter.hasNext();) {
                value.put(iter.next(), context);
            }
        } else {
            throw new ConversionException("CollectionValueCoder cannot encode an object of type "
                    + object.getClass().getName());
        }
    }

    /**
     * <p>
     * Creates an instance of the supplied class, populates its state by
     * decoding the supplied <code>Value</code>, and returns it. This method
     * will be called only if this <code>ValueCoder</code> has been registered
     * with the current {@link CoderManager} to encode objects having supplied
     * <code>Class</code> value. Persistit will never call this method to decode
     * a value that was <code>null</code> when written because null values are
     * handled by built-in encoding logic.
     * </p>
     * 
     * @param value
     *            The <code>Value</code> from which interior fields of the
     *            object are to be retrieved
     * @param clazz
     *            The class of the object to be returned.
     * @param context
     *            An arbitrary object that can optionally be supplied by the
     *            application to convey an application-specific context for the
     *            operation. (See {@link CoderContext}.) The default value is
     *            <code>null</code>.
     * @return An <code>Object</code> having the same class as the suppled
     *         <code>clazz</code> parameter.
     * @throws ConversionException
     */
    @Override
    public Object get(Value value, Class<?> clazz, CoderContext context) throws ConversionException {
        try {
            Object target = clazz.newInstance();
            value.registerEncodedObject(target);
            render(value, target, clazz, context);
            return target;
        } catch (InstantiationException ce) {
            throw new ConversionException(ce + " while decoding a Collection value");
        } catch (IllegalAccessException iae) {
            throw new ConversionException(iae + " while decoding a Collection value");
        }
    }

    /**
     * <p>
     * Populates the state of the supplied (mutable) target <code>Object</code>
     * by decoding the supplied <code>Value</code>. This method will be called
     * only if this <code>ValueRenderer</code> has been registered with the
     * current {@link CoderManager} to encode objects having the supplied
     * <code>Class</code> value. Persistit will never call this method to decode
     * a value that was <code>null</code> when written because null values are
     * handled by built-in encoding logic.
     * </p>
     * 
     * @param value
     *            The <code>Value</code> from which interior fields of the
     *            object are to be retrieved
     * 
     * @param target
     *            The object into which the decoded value is to be written
     * 
     * @param clazz
     *            The class of the object that was originally encoded into
     *            Value.
     * 
     * @param context
     *            An arbitrary object that can optionally be supplied by the
     *            application to convey an application-specific context for the
     *            operation. (See {@link CoderContext}.) The default value is
     *            <code>null</code>.
     * 
     * @throws ConversionException
     */
    @Override
    public void render(Value value, Object target, Class clazz, CoderContext context) throws ConversionException {
        if (target instanceof Map) {
            if (Map.class.isAssignableFrom(clazz)) {
                Map map = (Map) target;
                for (;;) {
                    if (!value.hasMoreItems()) {
                        break;
                    }
                    Object itemKey = value.get(null, context);
                    if (!value.hasMoreItems()) {
                        throw new ConversionException("Encoded Map entry has missing value");
                    }
                    Object itemValue = value.get(null, context);
                    map.put(itemKey, itemValue);
                }
            } else
                throw new ConversionException("Cannot convert a " + clazz.getName() + " to a Map");
        } else if (target instanceof Collection) {
            if (Collection.class.isAssignableFrom(clazz)) {
                Collection collection = (Collection) target;
                for (;;) {
                    if (!value.hasMoreItems()) {
                        break;
                    }
                    Object itemValue = value.get(null, context);
                    collection.add(itemValue);
                }
            } else
                throw new ConversionException("Cannot convert " + clazz.getName() + " to a Collection");

        } else
            throw new ConversionException("CollectionValueCoder cannot render to an object of class "
                    + target.getClass().getName());
    }

    /**
     * <p>
     * Writes a String representation of the value into a supplied
     * <code>StringBuilder</code>. This is used in utility programs to display
     * stored content without actually deserialized Objects represented by the
     * value.
     * </p>
     * <p>
     * This method will be called only if this <code>ValueDisplayer</code> has
     * been registered with the current {@link CoderManager} to encode objects
     * having the supplied <code>Class</code> value. Persistit will never call
     * this method to decode a value that was <code>null</code> when written
     * because null values are handled by built-in encoding logic.
     * </p>
     * 
     * @param value
     *            The <code>Value</code> from which interior fields of the
     *            object are to be retrieved
     * 
     * @param target
     *            The <code>Appendable</code> into which the decoded value is
     *            to be written
     * 
     * @param clazz
     *            The class of the object that was originally encoded into
     *            Value.
     * 
     * @param context
     *            An arbitrary object that can optionally be supplied by the
     *            application to convey an application-specific context for the
     *            operation. (See {@link CoderContext}.) The default value is
     *            <code>null</code>.
     * 
     * @throws ConversionException
     */
    @Override
    public void display(Value value, StringBuilder target, Class<?> clazz, CoderContext context)
            throws ConversionException {
        if (Map.class.isAssignableFrom(clazz)) {
            target.append('[');
            boolean first = true;
            for (;;) {
                if (!value.hasMoreItems()) {
                    break;
                }

                if (first)
                    first = false;
                else
                    target.append(',');

                value.decodeDisplayable(true, target);
                target.append("->");

                if (!value.hasMoreItems()) {
                    throw new ConversionException("Encoded Map entry has missing value");
                }

                value.decodeDisplayable(true, target);
            }
            target.append(']');
        } else if (Collection.class.isAssignableFrom(clazz)) {
            target.append('[');
            boolean first = true;

            for (;;) {
                if (!value.hasMoreItems()) {
                    break;
                }

                if (first)
                    first = false;
                else
                    target.append(',');

                value.decodeDisplayable(true, target);
            }
            target.append(']');
        } else
            throw new ConversionException("Cannot display value that is neither a Map nor a Collection");
    }

}
