/*
 * Copyright © 2019 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

import javax.annotation.*;

/**
 * Something that infers new URF statements from existing ones.
 * @apiNote This interface is experimental.
 * @author Garret Wilson
 */
public interface UrfInferencer {

	/** An URF inferencer that does nothing. */
	public static final UrfInferencer NOP = (processor, subject, property, object) -> {
	};

	/**
	 * Infers zero or more statements during processing of an URF statement.
	 * @param processor The processor to process any inferences made.
	 * @param subject The reference representing the subject of the statement, with a source-declared tag which may be a blank tag.
	 * @param property The reference representing the property of the statement, with a source-declared tag which may be a blank tag.
	 * @param object The reference representing the property value of the statement, with a source-declared tag which may be a blank tag.
	 * @throws IllegalArgumentException if a subject, property, and/or object was given without a tag.
	 * @see UrfProcessor#processStatement(UrfReference, UrfReference, UrfReference)
	 */
	public void processStatement(@Nonnull final UrfProcessor<?> processor, @Nonnull UrfReference subject, @Nonnull UrfReference property,
			@Nonnull UrfReference object);

}
