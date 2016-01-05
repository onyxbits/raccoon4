/**
 * <h2>Weave - A framework for building user interfaces.</h2>
 * 
 * Weave provides the following:
 * <ul>
 * <li> A managed window system that takes care of creating/opening/hiding/destroying windows.
 * <li> A message passing mechanism for communicating between subsystems (even non UI ones) without tight coupling.
 * <li> A lot of reusable components that address recurring problems.
 * </ul> 
 * <p>
 * A weave application looks like this:
 * 
 * <code>
 * public static void main(String[] args) {<br>
 * &nbsp;&nbsp;Lifecycle myCycle = new MyCycle();<br>
 * &nbsp;&nbsp;SwingUtilities.invokeLater(new LifecycleManager(myCycle));<br>
 * }
 * </code>
 * 
 * @author patrick
 *
 */
package de.onyxbits.weave;