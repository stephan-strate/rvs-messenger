package com.messenger.console;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Method annotation to identify console methods
 * for {@link Console}. Like this, java methods, the
 * annotation is declared with, can be accessed directly
 * from unix/windows console.
 * Easy way to link complex java methods with different
 * methods, that can be used directly from unix/windows
 * console.
 *
 * Example:
 * "@Method
 * protected void update (String[] args) {
 *     if (args[0] != null) {
 *         switch (args[0]) {
 *             case "database": {
 *                 ...
 *                 break;
 *             }
 *         }
 *     } else {
 *         ...
 *     }
 * }"</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Method {

}