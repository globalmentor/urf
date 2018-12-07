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

import javax.annotation.*;

/**
 * Something that processes URF statements.
 * @param <R> The type of result returned by the processor.
 * @author Garret Wilson
 */
public interface UrfProcessor<R> {

	/** An URF processor that does nothing. */
	public static final UrfProcessor<Void> NOP = new AbstractUrfProcessor<Void>() {
		@Override
		public void processStatement(final UrfReference subject, final UrfReference property, final UrfReference object) {
		}

		@Override
		public Void getResult() {
			return null;
		}
	};

	/**
	 * Declares the existence of a resource with no type or for which no type is known.
	 * @apiNote The resource is not guaranteed to be referenced again during processing. It may not have a type or even a description.
	 * @implSpec The default implementation delegates to {@link #declareResource(URI, URI)}.
	 * @param declaredTag The identifying resource tag for declaration, which may be a blank tag if the resource has no tag.
	 * @throws IllegalArgumentException if a tag is given that is not an absolute IRI.
	 */
	public default void declareResource(@Nonnull final URI declaredTag) {
		declareResource(declaredTag, null);
	}

	/**
	 * Declares the existence of a resource.
	 * @apiNote The resource is not guaranteed to be referenced again during processing. It may not have a type or even a description.
	 * @param declaredTag The identifying resource tag for declaration, which may be a blank tag if the resource has no tag.
	 * @param typeTag The tag of the resource type, or <code>null</code> if not known.
	 * @throws IllegalArgumentException if a tag is given that is not an absolute IRI.
	 * @throws IllegalArgumentException if a type is given that doesn't match a previously declared type.
	 */
	public void declareResource(@Nonnull final URI declaredTag, @Nullable final URI typeTag);

	/**
	 * Reports a resource as a "root" in the URF data being processed.
	 * <p>
	 * This method provides a hint to processor, providing it with more information about the processing. This may help the processor better utilize the
	 * resources. For example, if the processor decides to serialize the information again later, knowing which resources were the root may help it arrange the
	 * information in a more pleasing manner.
	 * </p>
	 * <p>
	 * There is no guarantee that this method will ever be called during processing.
	 * </p>
	 * @apiNote Reporting a resource as a root imputes no additional semantics on the resource.
	 * @param root The reference being marked as a root resource, with a source-declared tag which may be a blank tag.
	 */
	public void reportRootResource(@Nonnull UrfReference root); //TODO should this automatically declare resources, or must they be declared separately? but this has to support value objects, too

	/**
	 * Processes an URF statement.
	 * <p>
	 * None of the parameters are guaranteed to have a tag, name, or ID. However if multiple properties pertain to the same anonymous subject, the same subject
	 * instance is guaranteed to be used each time this method is called.
	 * </p>
	 * @param subject The reference representing the subject of the statement, with a source-declared tag which may be a blank tag.
	 * @param property The reference representing the property of the statement, with a source-declared tag which may be a blank tag.
	 * @param object The reference representing the property value of the statement, with a source-declared tag which may be a blank tag.
	 * @throws IllegalArgumentException if a subject, property, and/or object was given without a tag.
	 */
	public void processStatement(@Nonnull UrfReference subject, @Nonnull UrfReference property, @Nonnull UrfReference object);

	/** @return The result of the processing. */
	public R getResult();

}
