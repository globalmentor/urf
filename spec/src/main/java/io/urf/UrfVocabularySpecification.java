/*
 * Copyright © 2020 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

package io.urf;

import static com.globalmentor.java.Conditions.checkArgumentNotNegative;
import static com.globalmentor.net.URIs.isCollectionURI;

import java.net.URI;
import java.util.Optional;

import com.globalmentor.net.URIs;
import com.globalmentor.vocab.VocabularySpecification;

/**
 * Specification for URF vocabularies for namespace alias registration.
 * @author Garret Wilson
 */
public class UrfVocabularySpecification implements VocabularySpecification {

	/** The shared, singleton instance of this specification. */
	public static UrfVocabularySpecification INSTANCE = new UrfVocabularySpecification();

	/** The beginning string to use when generating new prefixes. */
	public static final String PREFIX_PREFIX = "ns";

	/** This specification cannot be instantiated publicly. */
	private UrfVocabularySpecification() {
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation determines whether the string is valid URF token.
	 * @see URF.Handle#isValidAlias(String)
	 */
	@Override
	public boolean isValidPrefix(final String prefix) {
		return URF.Handle.isValidAlias(prefix);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation returns {@value #PREFIX_PREFIX} with the uniqueness guarantee appended.
	 */
	@Override
	public String generatePrefix(final long uniquenessGuarantee) {
		checkArgumentNotNegative(uniquenessGuarantee);
		return PREFIX_PREFIX + uniquenessGuarantee;
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation returns whether the supposed namespace is a collection URI, ending with a slash character.
	 * @see URIs#isCollectionURI(URI)
	 */
	@Override
	public boolean isNamespaceRegular(final URI namespace) {
		return isCollectionURI(namespace);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation delegates to {@link URF.Tag#findNamespace(URI)}.
	 */
	@Override
	public Optional<URI> findTermNamespace(final URI term) {
		return URF.Tag.findNamespace(term);
	}
}
