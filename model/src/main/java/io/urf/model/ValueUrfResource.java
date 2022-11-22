/*
 * Copyright © 2018 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.urf.model;

/**
 * A resource that provides a reference to a value object indicated by the tag. For example the a resource with the tag {@code <https://urf.io/Boolean#true>}
 * might contain the value {@link Boolean#TRUE}.
 * <p>
 * An instances of this class is guaranteed to have a tag.
 * </p>
 * @param <T> The type of value the resource contains.
 * @author Garret Wilson
 */
public interface ValueUrfResource<T> extends ObjectUrfResource<T> {

	/**
	 * Returns the value object indicated by the resource tag.
	 * <p>
	 * The default implementation delegates to {@link #getObject()}.
	 * </p>
	 * @return The value object indicated by the resource tag.
	 */
	public default T getValue() {
		return getObject();
	}

}
