/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.sisob.components.framework.util;

/**

 * This utility provides a generic way to get an instance of

 * {@link java.lang.ClassLoader}. <br/>

 * Is introduced for replacing the system classloader in the

 * FreeStyler context to allow the FreeStyler to used with

 * web start.

 * 

 * @author goehnert

 *

 */

public class ClassLoaderUtility {

        

        /**

         * This flag allows enables or disables the use of the

         * system classloader.

         */

        private static boolean useSystemClassLoader = false;

        

        /**

         * This instance of the ClassLoaderUtility allows to

         * get a classloader bound to an object instantiated

         * in the current VM.

         */

        private static ClassLoaderUtility classLoaderUtility;

        

        /**

         * Returns a classloader to be used in the FreeStyler context.

         * If {@link #useSystemClassLoader} is true, it is the

         * system classloader (obtained by {@link ClassLoader#getSystemClassLoader()}),

         * otherwise it is the classloader associated with an internal

         * singleton instance of this class. <br/>

         * ATTENTION: The exact way the returned classloader is obtained

         * may change in the future if necessary.

         * 

         * @return an instance of ClassLoader

         */

        public static ClassLoader getClassLoader() {

                if (useSystemClassLoader) {

                        return ClassLoader.getSystemClassLoader();

                } else {

                        if (classLoaderUtility == null) {

                                classLoaderUtility = new ClassLoaderUtility();

                        }

                        return classLoaderUtility.getClass().getClassLoader();

                }

        }

}

