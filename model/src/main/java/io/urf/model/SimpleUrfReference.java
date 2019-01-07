/*
 * Copyright © 2018 GlobalMentor, Inc. <http://www.globalmentor.com/>
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
import java.util.*;

import javax.annotation.*;

import io.urf.URF;

/**
 * A simple reference to a resource based on its tag.
 * @apiNote References on their own do not support {@link #hashCode()} and {@link #equals(Object)}, although certain subtypes such as {@link UrfReference}
 *          provide such guarantees.
 * @author Garret Wilson
 */
public final class SimpleUrfReference implements UrfReference {

	private final URI tag;

	@Override
	public Optional<URI> getTag() {
		return Optional.ofNullable(tag);
	}

	/**
	 * Tag constructor.
	 * @param tag The identifying resource tag; must not be <code>null</code>.
	 * @throws NullPointerException if the given tag is <code>null</code>.
	 * @throws IllegalArgumentException if a tag is given that is not a valid tag, for example, not it is not an absolute IRI.
	 */
	public SimpleUrfReference(@Nonnull final URI tag) {
		this.tag = URF.Tag.checkArgumentValid(tag);
	}

}
