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
		public void process(final UrfResource subject, final UrfResource property, final UrfResource propertyValue) {
		}

		@Override
		public Void getResult() {
			return null;
		}
	};

	/**
	 * Creates a general resource.
	 * <p>
	 * A parser is not required to call this method to create a value resource; it may explicitly create a value resource using some other implementation which
	 * may be more efficient.
	 * </p>
	 * @param tag The identifying resource tag, or <code>null</code> if not known.
	 * @param typeTag The tag of the resource type, or <code>null</code> if not known.
	 * @return The resource instance representing that being processed.
	 * @throws IllegalArgumentException if a tag is given that is not an absolute IRI.
	 */
	public UrfResource createResource(@Nullable final URI tag, @Nullable final URI typeTag);

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
	 * @param root The resource being marked as a root resource.
	 */
	public void reportRootResource(@Nonnull final UrfResource root);

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

	/** @return The result of the processing. */
	public R getResult();

}
