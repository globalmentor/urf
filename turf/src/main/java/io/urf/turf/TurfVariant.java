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

package io.urf.turf;

import static io.urf.turf.TURF.*;
import static java.util.Objects.*;

import java.util.Optional;

import javax.annotation.*;

import com.globalmentor.io.Filenames;
import com.globalmentor.net.MediaType;

/**
 * Useful enumeration of possible variants of a TURF file.
 * @author Garret Wilson
 */
public enum TurfVariant {

	/** A classic Turf file. */
	TURF(MEDIA_TYPE, FILENAME_EXTENSION),
	/** A Turf Properties file. */
	TURF_PROPERTIES(PROPERTIES_MEDIA_TYPE, PROPERTIES_FILENAME_EXTENSION);

	private final MediaType mediaType;

	/** @return The base Internet media type of the variant. */
	public MediaType getMediaType() {
		return mediaType;
	}

	private final String filenameExtension;

	/** @return The default extension to be used for filenames. */
	public String getFilenameExtension() {
		return filenameExtension;
	}

	/**
	 * Constructor.
	 * @param mediaType The base Internet media type of the variant.
	 * @param filenameExention The default extension to be used for filenames.
	 */
	private TurfVariant(@Nonnull final MediaType mediaType, @Nonnull final String filenameExention) {
		this.mediaType = requireNonNull(mediaType);
		this.filenameExtension = requireNonNull(filenameExention);
	}

	/**
	 * Determines the variant from a media type if possible.
	 * @param mediaType The Internet media type from which to determine the TURF variant.
	 * @return The TURF variant, if one could be determined.
	 */
	public static Optional<TurfVariant> findFromMediaType(@Nonnull final MediaType mediaType) {
		if(mediaType.hasBaseType(MEDIA_TYPE)) {
			return Optional.of(TURF);
		} else if(mediaType.hasBaseType(PROPERTIES_MEDIA_TYPE)) {
			return Optional.of(TURF_PROPERTIES);
		} else {
			return Optional.empty();
		}
	}

	/**
	 * Determines the variant from a filename if possible.
	 * @implSpec The variant is determined from the filaneme's extension, if any, using {@link #findFromFilenameExtension(String)}
	 * @param filename The filename from which to determine the TURF variant.
	 * @return The TURF variant, if one could be determined.
	 */
	public static Optional<TurfVariant> findFromFilename(@Nonnull final String filename) {
		return Filenames.findExtension(filename).flatMap(TurfVariant::findFromFilenameExtension);
	}

	/**
	 * Determines the variant from a filename extension if possible.
	 * @param filenameExtension The filename extension from which to determine the TURF variant.
	 * @return The TURF variant, if one could be determined.
	 */
	public static Optional<TurfVariant> findFromFilenameExtension(@Nonnull final String filenameExtension) {
		switch(filenameExtension) {
			case FILENAME_EXTENSION:
				return Optional.of(TURF);
			case PROPERTIES_FILENAME_EXTENSION:
				return Optional.of(TURF_PROPERTIES);
			default:
				return Optional.empty();
		}
	}

}
