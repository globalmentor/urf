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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Something that processes URF statements.
 * @author Garret Wilson
 */
@FunctionalInterface
public interface UrfProcessor {

	/** An URF processor that does nothing. */
	public static final UrfProcessor NOP = (subject, property, propertyValue) -> {
	};

	/**
	 * Creates a general, non-container resource.
	 * <p>
	 * A parser is not required to call this method to create a resource; it may explicitly create a resource using some other implementation which may be more
	 * efficient.
	 * </p>
	 * <p>
	 * The default implementation returns an instance of a {@link SimpleUrfResource}.
	 * </p>
	 * @param tag The identifying resource tag, or <code>null</code> if not known.
	 * @param typeTag The tag of the resource type, or <code>null</code> if not known.
	 * @throws IllegalArgumentException if a tag is given that is not an absolute IRI.
	 */
	public default UrfResource createResource(@Nullable final URI tag, @Nullable final URI typeTag) {
		return new SimpleUrfResource(tag, typeTag);
	}

	//TODO document

	public default UrfResource createListResource(@Nullable final URI tag, @Nullable final URI typeTag) {
		return createResource(tag, typeTag);
	}

	public default UrfResource createMapResource(@Nullable final URI tag, @Nullable final URI typeTag) {
		return createResource(tag, typeTag);
	}

	public default UrfResource createSetResource(@Nullable final URI tag, @Nullable final URI typeTag) {
		return createResource(tag, typeTag);
	}

	/**
	 * Processes an URF statement.
	 * <p>
	 * None of the parameters are guaranteed to have a tag, name, or ID. However if multiple properties pertain to the same anonymous subject, the same subject
	 * instance is guaranteed to be used each time this method is called.
	 * </p>
	 * @param subject The URF resource representing the subject of the statement.
	 * @param property The URF resource representing the property of the statement.
	 * @param propertyValue The URF resource representing the property value of the statement.
	 */
	public void process(@Nonnull UrfResource subject, @Nonnull UrfResource property, @Nonnull UrfResource propertyValue);

}
