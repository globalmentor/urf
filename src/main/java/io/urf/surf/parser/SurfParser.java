/*
 * Copyright © 2016–2017 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.urf.surf.parser;

import static com.globalmentor.io.ReaderParser.*;
import static com.globalmentor.java.Characters.*;
import static io.urf.SURF.*;

import java.io.*;
import java.util.*;

import javax.annotation.*;

import com.globalmentor.io.ParseIOException;
import com.globalmentor.java.Characters;

import io.urf.SURF;

/**
 * Simple parser for the Simple URF (SURF) document format.
 * @author Garret Wilson
 */
public class SurfParser {

	/** The map of resources that have been labeled. */
	private final Map<String, SurfResource> labeledResources = new HashMap<>();

	/**
	 * Parses an URF resource from an input stream.
	 * @param inputStream The input stream containing SURF data.
	 * @return The root URF resource, which may be empty if the SURF document was empty.
	 * @throws IOException If there was an error reading the SURF data.
	 */
	public Optional<Object> parse(@Nonnull final InputStream inputStream) throws IOException {
		return parse(new LineNumberReader(new InputStreamReader(inputStream, CHARSET)));
	}

	/**
	 * Parses an URF resource from a reader.
	 * @param reader The reader containing SURF data.
	 * @return The root URF resource, which may be empty if the SURF document was empty.
	 * @throws IOException If there was an error reading the SURF data.
	 */
	public Optional<Object> parse(@Nonnull final Reader reader) throws IOException {
		if(skipFiller(reader) < 0) { //skip filler; if we reached the end of the stream
			return Optional.empty(); //the SURF document is empty
		}
		return Optional.of(parseResource(reader));
	}

	/**
	 * Parses a resource; either a label or a resource representation. The next character read must be the start of the resource.
	 * @param document The document being parsed.
	 * @param reader The reader containing SURF data.
	 * @throws IOException If there was an error reading the SURF data.
	 */
	public Object parseResource(@Nonnull final Reader reader) throws IOException {
		String label = null;
		int c = peekEnd(reader); //peek the next character TODO change to peek()?
		if(c == LABEL_BEGIN) {
			//TODO parse label
			skipFiller(reader);
			c = peekEnd(reader); //peek the next character after the label
		}
		final Object resource;
		switch(c) {
			case BOOLEAN_FALSE_BEGIN:
			case BOOLEAN_TRUE_BEGIN:
				resource = parseBoolean(reader);
				break;
			case OBJECT_BEGIN:
				resource = parseObject(reader);
				break;
			default:
				throw new ParseIOException(reader, "Expected resource; found character: " + Characters.getLabel(c));
		}
		return resource;
	}

	/**
	 * Parses a Boolean value.
	 * @param document The document being parsed. The next character read is expected to be the start of {@link SURF#BOOLEAN_FALSE} or {@link SURF#BOOLEAN_TRUE}.
	 * @param reader The reader containing SURF data.
	 * @throws IOException If there was an error reading the SURF data.
	 */
	public Object parseBoolean(@Nonnull final Reader reader) throws IOException {
		int c = peek(reader); //peek the next character
		switch(c) { //see what the next character is
			case BOOLEAN_FALSE_BEGIN: //false
				check(reader, BOOLEAN_FALSE_LEXICAL_FORM); //make sure this is really false
				return Boolean.FALSE;
			case BOOLEAN_TRUE_BEGIN: //true
				check(reader, BOOLEAN_TRUE_LEXICAL_FORM); //make sure this is really true
				return Boolean.TRUE;
			default: //if we don't recognize the start of the boolean lexical form
				checkReaderNotEnd(reader, c); //make sure we're not at the end of the reader
				throw new ParseIOException(reader, "Unrecognized start of boolean: " + (char)c);
		}
	}

	/**
	 * Parses an object; that is, an anonymous resource instance indicated by <code>*</code>.
	 * @param document The document being parsed. The next character read is expected to be {@value SURF#OBJECT_BEGIN}.
	 * @param reader The reader containing SURF data.
	 * @throws IOException If there was an error reading the SURF data.
	 */
	public SurfResource parseObject(@Nonnull final Reader reader) throws IOException {
		check(reader, OBJECT_BEGIN); //*
		//TODO finish
		final String typeName = null; //TODOO fix
		final SurfResource resource = new SurfResource(typeName);
		return resource;
	}

	/**
	 * Skips over SURF fillers in a reader. This method skips all filler characters {@link SURF#FILLER_CHARACTERS as well as any comments. The new position will
	 * either be the that of the first non-filler character or the end of the input stream.
	 * @param reader The reader the contents of which to be parsed.
	 * @return The next character that will be returned the reader's {@link Reader#read()} operation, or <code>-1</code> if the end of the reader has been
	 *         reached.
	 * @throws NullPointerException if the given reader is <code>null</code>.
	 * @throws IOException if there is an error reading from the reader.
	 */
	protected static int skipFiller(final Reader reader) throws IOException {
		int c; //we'll store the next non-filler character here so that it can be returned
		while((c = skip(reader, FILLER_CHARACTERS)) == COMMENT_BEGIN) { //skip all fillers; if the start of a comment was encountered
			check(reader, COMMENT_BEGIN); //read the beginning comment delimiter
			reachEnd(reader, EOL_CHARACTERS); //skip to the end of the line; we'll then skip all filler characters and see if another comment starts
		}
		return c; //return the last character read
	}

}
