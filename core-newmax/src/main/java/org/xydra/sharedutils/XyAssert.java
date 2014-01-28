/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.xydra.sharedutils;

import javax.annotation.Nullable;

import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;


/**
 * Provides functionality similar to the Java 'assert' keyword, which also runs
 * on Google AppEngine.
 * 
 * Ensuring whether {@code assert} is enabled in a given environment can be
 * difficult.
 * 
 * @author ohler@google.com (Christian Ohler) [code copied from him]
 * @author xamde
 */
public class XyAssert {
    
    private static final Logger log = LoggerFactory.getLogger(XyAssert.class);
    
    /**
     * Runtime-config setting to enable or disable assertion on this JVM
     */
    private static boolean enabled = true;
    
    /**
     * Turn on assertions that run also in AppEngine production mode and in GET
     */
    public static void enable() {
        if(log.isDebugEnabled()) log.debug("XyAssert is on");
        enabled = true;
    }
    
    public static boolean isEnabled() {
        return enabled;
    }
    
    public static void setEnabled(boolean enabled_) {
        log.info("XyAssert is " + enabled_);
        enabled = enabled_;
    }
    
    private XyAssert() {
    }
    
    /**
     * Ensures the truth of an expression.
     * 
     * @param expression a boolean expression
     * @throws AssertionError if {@code expression} is false
     */
    public static void xyAssert(boolean expression) {
        if(!enabled) {
            return;
        }
        if(!expression) {
            throw new AssertionError("xyAssert failed");
        }
    }
    
    /**
     * Validations are always active.
     * 
     * @param o
     * @throws IllegalArgumentException if o is null
     */
    public static void validateNotNull(Object o) throws IllegalArgumentException {
        if(o == null) {
            throw new IllegalArgumentException("Parameter may not be null");
        }
    }
    
    /**
     * Validations are always active.
     * 
     * @param o
     * @param parameterName that is checked
     * @throws IllegalArgumentException if o is null
     */
    public static void validateNotNull(Object o, String parameterName)
            throws IllegalArgumentException {
        if(o == null) {
            throw new IllegalArgumentException("Parameter '" + parameterName + "' may not be null");
        }
    }
    
    /**
     * Validations are always active.
     * 
     * @param condition
     * @param explanation what to tell users when the condition fails
     * @throws IllegalArgumentException if condition is false
     */
    public static void validateCondition(boolean condition, final String explanation)
            throws IllegalArgumentException {
        if(!condition) {
            throw new IllegalArgumentException("Parameter is not valid. Reason: " + explanation);
        }
    }
    
    /**
     * Ensures the truth of an expression.
     * 
     * @param expression a boolean expression
     * @param errorMessage the exception message to use if the check fails; will
     *            be converted to a string using {@link String#valueOf(Object)}
     * @throws AssertionError if {@code expression} is false
     */
    public static void xyAssert(boolean expression, @Nullable Object errorMessage) {
        if(!enabled) {
            return;
        }
        if(!expression) {
            throw new AssertionError(String.valueOf(errorMessage));
        }
    }
    
    /**
     * Ensures the truth of an expression.
     * 
     * @param expression a boolean expression
     * @param errorMessageTemplate a template for the exception message should
     *            the check fail. The message is formed by replacing each
     *            {@code %s} placeholder in the template with an argument. These
     *            are matched by position - the first {@code %s} gets
     *            {@code errorMessageArgs[0]}, etc. Unmatched arguments will be
     *            appended to the formatted message in square braces. Unmatched
     *            placeholders will be left as-is.
     * @param errorMessageArgs the arguments to be substituted into the message
     *            template. Arguments are converted to strings using
     *            {@link String#valueOf(Object)}.
     * @throws IllegalArgumentException if {@code expression} is false
     * @throws NullPointerException if the check fails and either
     *             {@code errorMessageTemplate} or {@code errorMessageArgs} is
     *             null (don't let this happen)
     */
    public static void xyAssert(boolean expression, @Nullable String errorMessageTemplate,
            @Nullable Object ... errorMessageArgs) {
        if(!enabled) {
            return;
        }
        if(!expression) {
            throw new AssertionError(format(errorMessageTemplate, errorMessageArgs));
        }
    }
    
    /**
     * Substitutes each {@code %s} in {@code template} with an argument. These
     * are matched by position - the first {@code %s} gets {@code args[0]}, etc.
     * If there are more arguments than placeholders, the unmatched arguments
     * will be appended to the end of the formatted message in square braces.
     * 
     * @param templateStr a non-null string containing 0 or more {@code %s}
     *            placeholders.
     * @param args the arguments to be substituted into the message template.
     *            Arguments are converted to strings using
     *            {@link String#valueOf(Object)}. Arguments can be null.
     * @return A string with all '%s' replaced (if there are enough objects)
     */
    public static String format(String templateStr, @Nullable Object ... args) {
        String template = String.valueOf(templateStr); // null -> "null"
        
        // start substituting the arguments into the '%s' placeholders
        StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);
        int templateStart = 0;
        int i = 0;
        while(i < args.length) {
            int placeholderStart = template.indexOf("%s", templateStart);
            if(placeholderStart == -1) {
                break;
            }
            builder.append(template.substring(templateStart, placeholderStart));
            builder.append(args[i++]);
            templateStart = placeholderStart + 2;
        }
        builder.append(template.substring(templateStart));
        
        // if we run out of placeholders, append the extra args in square braces
        if(i < args.length) {
            builder.append(" [");
            builder.append(args[i++]);
            while(i < args.length) {
                builder.append(", ");
                builder.append(args[i++]);
            }
            builder.append(']');
        }
        
        return builder.toString();
    }
    
}
