package com.aperto.magkit.utils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

/**
 * Provides an wrapper for methods with paramters wich allows accessing it with jsp expression language syntax.
 * E.g. ${obj.foo.bar["2.0"]} --> obj.foo("bar", "2.0")
 *
 * @author Norman Wiechmann (Aperto AG)
 */
public class ElUtil {

    /**
     * Returns a {@link Map} it keys map to parameters of the specified method using reflection.
     * Methods with more than one parameter are supported. Each parameter represents a key of a map. All parameters
     * will return another map until the last parameter is provided as key. Then the method will be invoked.
     * Note, that the returned maps are not completely implemented. Only the get(Object) method is supported to use
     * as intended within jsp expression language.
     */
    public static Map methodWrapper(final Object object, final String methodName, final Class... parameterTypes) {
        if (parameterTypes == null || parameterTypes.length == 0) {
            throw new IllegalArgumentException("Method must have at least one parameter.");
        }
        try {
            Method method = object.getClass().getMethod(methodName, parameterTypes);
            return new MethodInvocatingMap(object, method);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Not wrappable method.", e);
        }
    }

    /**
     * Method invocating map implementation.
     * Note that only {@link #get(Object)} method is implemented.
     */
    protected static class MethodInvocatingMap implements Map {
        private static final Logger LOGGER = Logger.getLogger(MethodInvocatingMap.class);

        private final Object _object;
        private final Method _method;
        private final int _nrOfMethodParameters;
        private final Object[] _parameters;

        public MethodInvocatingMap(final Object object, final Method method) {
            this(object, method, new Object[0]);
        }

        public MethodInvocatingMap(final Object object, final Method method, final Object[] parameters) {
            _object = object;
            _method = method;
            _nrOfMethodParameters = method.getParameterTypes().length;
            _parameters = parameters;
        }

        public int size() {
            throw new UnsupportedOperationException("Method size() is not implemented.");
        }

        public boolean isEmpty() {
            return false;
        }

        public boolean containsKey(final Object key) {
            throw new UnsupportedOperationException("Method size() is not implemented.");
        }

        public boolean containsValue(final Object value) {
            throw new UnsupportedOperationException("Method size() is not implemented.");
        }

        /**
         * Returns the method invocation result or another map for the remaining parameter key.
         */
        public Object get(final Object key) {
            Object result = null;
            Object[] parameters = ArrayUtils.add(_parameters, key);
            if (parameters.length == _nrOfMethodParameters) {
                try {
                    result = _method.invoke(_object, parameters);
                } catch (Exception e) {
                    logError(e, parameters);
                }
            } else {
                result = new MethodInvocatingMap(_object, _method, parameters);
            }
            return result;
        }

        private void logError(final Exception e, final Object[] parameters) {
            StringBuilder message = new StringBuilder(256);
            message.append(e.getMessage());
            for (Object parameter : parameters) {
                message.append(", ").append(parameter)
                    .append("[").append(parameter.getClass().getSimpleName()).append("]");
            }
            message.append(" invoke ").append(_method);
            LOGGER.error(message, e);
        }

        public Object put(final Object key, final Object value) {
            throw new UnsupportedOperationException("Method size() is not implemented.");
        }

        public Object remove(final Object key) {
            throw new UnsupportedOperationException("Method size() is not implemented.");
        }

        public void putAll(final Map t) {
            throw new UnsupportedOperationException("Method size() is not implemented.");
        }

        public void clear() {
            throw new UnsupportedOperationException("Method size() is not implemented.");
        }

        public Set keySet() {
            throw new UnsupportedOperationException("Method size() is not implemented.");
        }

        public Collection values() {
            throw new UnsupportedOperationException("Method size() is not implemented.");
        }

        public Set entrySet() {
            throw new UnsupportedOperationException("Method size() is not implemented.");
        }
    }

    protected ElUtil() {
        // hidden default constructor
    }
}