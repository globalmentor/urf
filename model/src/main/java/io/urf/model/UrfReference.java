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

import java.net.URI;
import java.util.Optional;

import javax.annotation.*;

/**
 * Represents a reference to a resource or the resource itself; an encapsulation of a possible resource tag.
 * @author Garret Wilson
 */
public interface UrfReference {

	/** @return The resource identifier tag, if any. */
	public Optional<URI> getTag();

	/**
	 * Creates a reference from a tag.
	 * @param tag The identifying resource tag; must not be <code>null</code>.
	 * @return A reference with the given tag.
	 * @throws NullPointerException if the given tag is <code>null</code>.
	 * @throws IllegalArgumentException if a tag is given that is not a valid tag, for example, not it is not an absolute IRI.
	 */
	public static UrfReference ofTag(@Nonnull final URI tag) {
		return new SimpleUrfReference(tag);
	}

}
