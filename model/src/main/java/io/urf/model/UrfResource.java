/*
 * Copyright © 2017 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

import io.urf.URF;

/**
 * Represents a resource with an optional identifying tag.
 * <p>
 * URF resources are considered equal if and only if they both of type {@link UrfResource}, they both have a tag, and those tags are equal.
 * </p>
 * @author Garret Wilson
 */
public interface UrfResource extends UrfReference {

	/**
	 * Determines the name of the resource, based on its tag.
	 * <p>
	 * The default implementation determines the name from the tag, if any.
	 * </p>
	 * @return The name, if any, of the resource.
	 * @see #getTag()
	 * @see URF.Tag#getName(URI)
	 */
	public default Optional<String> getName() {
		return getTag().flatMap(URF.Tag::getName);
	}

	/**
	 * Determines the ID of the resource, based on its tag.
	 * <p>
	 * The default implementation determines the ID from the tag, if any.
	 * </p>
	 * @return The ID, if any, of the resource.
	 * @see #getTag()
	 * @see URF.Tag#getId(URI)
	 */
	public default Optional<String> getId() {
		return getTag().flatMap(URF.Tag::getId);
	}

	/** @return The tag of the resource type, if known. */
	public Optional<URI> getTypeTag();

}
