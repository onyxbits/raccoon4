/*
 * Copyright 2015 Patrick Ahlbrecht
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.onyxbits.weave;

/**
 * Support for populating the {@link Globals} registry lazily.
 * 
 * @author patrick
 * 
 */
public interface GlobalsFactory {

	/**
	 * Called by {@link Globals#get(Class)} when it doesn't have an instance of
	 * the given class.
	 * 
	 * @param globals
	 *          the calling registry
	 * @param requested
	 *          the class of the requested object.
	 * @return a new instance of the requested class or null if none can be
	 *         created.
	 */
	public Object onCreate(Globals globals, Class<?> requested);

}
