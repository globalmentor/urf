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

package io.urf.csv;

import static com.globalmentor.net.ContentType.*;
import static java.nio.charset.StandardCharsets.*;

import java.nio.charset.Charset;

import com.globalmentor.net.ContentType;

/**
 * Definitions for the URF CSV document format.
 * <p>
 * This format does not define a new file extension but instead defines a sub-extension to the CSV file extension, that is, <code>.urf.csv</code>. That is
 * because this format is intended to be primarily an import format, not a general storage and interchange format.
 * </p>
 * @author Garret Wilson
 */
public class URFCSV {

	/** The content type for URF CSV: <code>text/urf+csv</code>. */
	public static final ContentType CONTENT_TYPE = ContentType.of(TEXT_PRIMARY_TYPE, "urf+csv");

	/** An extension for URF CSV filenames, including the sub-extension: <code>urf.csv</code>. */
	public static final String FILENAME_EXTENSION = "urf.csv";

	/** The URF CSV charset. */
	public static final Charset CHARSET = UTF_8;

}
