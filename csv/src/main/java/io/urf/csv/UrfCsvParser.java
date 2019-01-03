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

import static com.globalmentor.io.ReaderParser.*;
import static com.globalmentor.java.Conditions.*;
import static io.urf.URF.*;
import static io.urf.csv.URFCSV.*;
import static java.util.Collections.*;
import static java.util.Objects.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Stream;

import javax.annotation.*;

import com.globalmentor.io.ParseIOException;
import com.globalmentor.io.function.IOFunction;
import com.univocity.parsers.common.*;
import com.univocity.parsers.csv.*;

import io.clogr.Clogged;
import io.urf.URF.Tag;
import io.urf.model.*;
import io.urf.turf.*;

/**
 * Parser for the URF CSV document format.
 * <p>
 * This parser is meant to be used once for parsing a single URF CSV document. It should not be used to parse multiple documents, as it maintains parsing state.
 * </p>
 * <p>
 * The parser should be released after use so as not to leak memory.
 * </p>
 * <p>
 * This implementation is not thread safe.
 * </p>
 * @param <R> The type of result returned by the parser, based upon the {@link UrfProcessor} used.
 * @author Garret Wilson
 * @see UrfProcessor
 */
public class UrfCsvParser<R> implements Clogged {

	private final UrfProcessor<R> processor;

	/** @return The strategy for processing the parsed URF graph. */
	protected UrfProcessor<R> getProcessor() {
		return processor;
	}

	/**
	 * Constructor.
	 * @param processor The strategy for processing the parsed URF graph.
	 */
	public UrfCsvParser(@Nonnull final UrfProcessor<R> processor) {
		this.processor = requireNonNull(processor);
	}

	/**
	 * Parses an URF CSV document from a string.
	 * <p>
	 * This is a convenience method that delegates to {@link #parseDocument(Reader, URI)}.
	 * </p>
	 * @param string The string containing SURF data.
	 * @param subjectTypeTag The tag indicating the type of the subject.
	 * @return The result of processing; the result from {@link UrfProcessor#getResult()}.
	 * @throws IOException If there was an error reading the data.
	 * @throws ParseIOException if the parsed data was invalid.
	 */
	public R parseDocument(@Nonnull final String string, @Nonnull final URI subjectTypeTag) throws IOException, ParseIOException {
		try (final Reader stringReader = new StringReader(string)) {
			return parseDocument(stringReader, subjectTypeTag);
		}
	}

	/**
	 * Parses an URF CSV document from an input stream.
	 * @param inputStream The input stream containing SURF data.
	 * @param subjectTypeTag The tag indicating the type of the subject.
	 * @return The result of processing; the result from {@link UrfProcessor#getResult()}.
	 * @throws IOException If there was an error reading the data.
	 * @throws ParseIOException if the parsed data was invalid.
	 * @see URFCSV#CHARSET
	 */
	public R parseDocument(@Nonnull final InputStream inputStream, @Nonnull final URI subjectTypeTag) throws IOException, ParseIOException {
		return parseDocument(new InputStreamReader(inputStream, CHARSET), subjectTypeTag); //the Univocity parser does its own buffering 
	}

	/**
	 * Parses an URF CSV document from a reader.
	 * @param reader The reader containing SURF data.
	 * @param subjectTypeTag The tag indicating the type of the subject.
	 * @return The result of processing; the result from {@link UrfProcessor#getResult()}.
	 * @throws IOException If there was an error reading the data.
	 * @throws ParseIOException if the parsed data was invalid.
	 */
	public R parseDocument(@Nonnull final Reader reader, @Nonnull final URI subjectTypeTag) throws IOException, ParseIOException {
		requireNonNull(subjectTypeTag);
		final CsvParserSettings parserSettings = new CsvParserSettings();
		parserSettings.setAutoConfigurationEnabled(false);
		parserSettings.setMaxCharsPerColumn(-1); //disable column width limits
		//TODO find a better way to do this; see https://github.com/uniVocity/univocity-parsers/issues/301 : parserSettings.setMaxColumns(Integer.MAX_VALUE); //disable column count limits
		parserSettings.setLineSeparatorDetectionEnabled(true);
		parserSettings.setHeaderExtractionEnabled(true);
		parserSettings.setProcessor(new RowProcessor(subjectTypeTag));
		parserSettings.getFormat().setComment('\0'); //turn off comment detection
		final CsvParser csvParser = new CsvParser(parserSettings);
		try {
			csvParser.parse(reader);
		} catch(final TextParsingException textParsingException) {
			throw new ParseIOException(textParsingException.getMessage(), textParsingException, textParsingException.getLineIndex(),
					textParsingException.getCharIndex());
		}
		return getProcessor().getResult();
	}

	/**
	 * An URF CSV row processor.
	 * <p>
	 * As is expected for Univocity parsers, this class throws {@link TextParsingException} if there is a parsing error.
	 * </p>
	 * @author Garret Wilson
	 * @see TextParsingException
	 */
	protected class RowProcessor implements com.univocity.parsers.common.processor.RowProcessor {

		private final URI subjectTypeTag;

		/** @return subjectTypeTag The tag indicating the type of the subject. */
		public URI getSubjectTypeTag() {
			return subjectTypeTag;
		}

		private Column[] columns = null;

		/**
		 * Returns a column definition. The column definitions are only available after parsing has started.
		 * @param columnIndex The zero-based index of the column.
		 * @return The requested column defined from the headers.
		 * @throws IllegalStateException if the columns are not yet available because processing has not yet started.
		 * @throws IndexOutOfBoundsException if the given column index is not within the range of available columns.
		 */
		protected Column getColumn(final int columnIndex) {
			checkState(columns != null, "Columns not yet initialized; processing not yet started.");
			return columns[columnIndex];
		}

		protected Optional<Column> idColumn = null;

		/**
		 * @return The column designated as representing the ID of each resource, if a column was designated as an ID.
		 * @throws IllegalStateException if the ID column has not yet been determined because processing has not yet started.
		 */
		protected Optional<Column> getIdColumn() {
			checkState(idColumn != null, "ID column not yet determined; processing not yet started.");
			return idColumn;
		}

		/**
		 * Constructor.
		 * @param subjectTypeTag The tag indicating the type of the subject.
		 */
		public RowProcessor(@Nonnull final URI subjectTypeTag) {
			this.subjectTypeTag = requireNonNull(subjectTypeTag);
		}

		@Override
		public void processStarted(final ParsingContext context) {
			final String[] headers = context.headers();

			final StringJoiner rowJoiner = new StringJoiner("|", "|", "|");
			Stream.of(headers).forEach(rowJoiner::add);
			getLogger().trace(rowJoiner.toString()); //TODO delete

			final Column[] columns = new Column[headers.length];
			for(int headerIndex = 0; headerIndex < headers.length; headerIndex++) {
				final String header = headers[headerIndex];
				try {
					final Column column;
					final boolean isIdColumn;
					try (final Reader headerReader = new StringReader(header)) {
						//preprocess the header to determine if it indicates an ID column
						final int firstSignificantChar = checkReaderNotEnd(headerReader, skip(headerReader, TURF.WHITESPACE_CHARACTERS)); //skip all whitespace
						isIdColumn = (firstSignificantChar == Name.ID_DELIMITER);
						column = parseHeader(headerReader, headerIndex, header);
					}
					columns[headerIndex] = column;
					if(isIdColumn) {
						if(idColumn != null && idColumn.isPresent()) {
							throw new ParseIOException(
									String.format("Column %d header `%s` already defined as an ID.", idColumn.get().getIndex(), idColumn.get().getHeader()));
						}
						idColumn = Optional.of(column);
					}
				} catch(final IOException ioException) {
					throw new TextParsingException(context, String.format("Error in column %d header `%s`: %s", headerIndex + 1, header, ioException.getMessage()),
							ioException);
				}
			}
			this.columns = columns; //store the columns only if we are successful in parsing all the headers
			if(idColumn == null) { //if no ID column was found, initialize it to empty
				idColumn = Optional.empty();
			}
		}

		/**
		 * Parses a single header representing a column.
		 * <p>
		 * This method ignores any column
		 * @param reader The reader containing the header.
		 * @param headerIndex The index of the column.
		 * @param header The original literal header.
		 * @return The column the header represents.
		 * @throws NullPointerException if the given header reader is <code>null</code>.
		 * @throws IllegalArgumentException if the given index is negative.
		 * @throws IOException if there was an error parsing the header.
		 * @throws ParseIOException if the format of the header is invalid, or if its content is invalid (e.g. if a handle refers to an unregistered namespace
		 *           alias).
		 */
		private Column parseHeader(@Nonnull final Reader reader, @Nonnegative final int headerIndex, @Nonnull final String header)
				throws IOException, ParseIOException {
			int c = checkReaderNotEnd(reader, skip(reader, TURF.WHITESPACE_CHARACTERS)); //skip all whitespace
			if(c == TURF.LINE_COMMENT_BEGIN) { //!
				return new IgnoredColumn(headerIndex, header); //ignore the entire column
			}
			final boolean isIdColumn = (c == Name.ID_DELIMITER); //#
			if(isIdColumn) {
				check(reader, Name.ID_DELIMITER);
				c = skip(reader, TURF.WHITESPACE_CHARACTERS);
			}
			//TODO detect/allow a missing property if this is an ID column
			final URI propertyTag = TurfParser.parseTagReference(reader, emptyMap()); //e.g. propertyHandle or |<propertyUri>|
			c = skip(reader, TURF.WHITESPACE_CHARACTERS);
			final URI typeTag; //the type representing the type of the column (the _range_ of the property), not the type of the property
			if(c == TURF.MAP_KEY_DELIMITER) { //:
				check(reader, TURF.MAP_KEY_DELIMITER);
				typeTag = TurfParser.parseTagReference(reader, emptyMap()); //e.g. typeHandle or |<typeUri>|
				c = skip(reader, TURF.WHITESPACE_CHARACTERS);
			} else {
				typeTag = null; //no type specified
			}
			//TODO detect unexpected trailing characters, allowing for a comment
			final IOFunction<String, UrfReference> parseStrategy; //determine how to parse the column based on the type
			if(typeTag == null) { //if no type is specified, use the field as-is
				parseStrategy = field -> new DefaultValueUrfResource<>(STRING_TYPE_TAG, field);
				//				return new DefaultColumn(headerIndex, header, propertyTag, typeTag, IOFunction.identity());
			} else {
				throw new ParseIOException("Unsupported type: " + typeTag); //TODO implement the other types
			}
			return new DefaultColumn(headerIndex, header, propertyTag, typeTag, parseStrategy);
		}

		@Override
		public void rowProcessed(final String[] row, final ParsingContext context) {
			//TODO provide some callback so the CLI can inform the user System.out.print(String.format("Processing line %d...\r", context.currentLine()-1));	//subtract one to compensate for the header

			final UrfProcessor<?> urfProcessor = getProcessor();

			final StringJoiner rowJoiner = new StringJoiner("|", "|", "|");
			Stream.of(row).forEach(rowJoiner::add);
			getLogger().trace(rowJoiner.toString()); //TODO delete

			//declare the resource, with tag (from ID) if possible
			final URI subjectTypeTag = getSubjectTypeTag();
			final URI subjectTag = getIdColumn().map(idColumn -> {
				final int idFieldIndex = idColumn.getIndex();
				if(idFieldIndex >= row.length) {
					throw new TextParsingException(context, String.format("Row missing ID field %d.", idFieldIndex));
				}
				return Tag.forTypeId(subjectTypeTag, row[idFieldIndex]);
			}).orElse(Tag.generateBlank()); //use a blank tag if no ID was indicated
			urfProcessor.declareResource(subjectTag, subjectTypeTag);

			final UrfResource subject = new SimpleUrfResource(subjectTag, subjectTypeTag);

			//process the properties
			for(int columnIndex = 0; columnIndex < row.length; columnIndex++) {
				//TODO make sure column index is not greater than column count
				final Column column = getColumn(columnIndex);
				final String field = row[columnIndex];
				if(field != null) { //if a value is present in the field TODO allow some configuration or determination of how "null" is indicated
					//if this column represents a property (e.g. it isn't ignored, and it isn't solely an ID column
					column.getProperty().ifPresent(property -> {
						final UrfReference object;
						try {
							object = column.parseField(field);
						} catch(final IOException ioException) {
							throw new TextParsingException(context,
									String.format("Error in column %d field `%s`: %s", column.getIndex() + 1, field, ioException.getMessage()), ioException);
						}
						urfProcessor.processStatement(subject, property, object);
					});
				}
			}
		}

		@Override
		public void processEnded(final ParsingContext context) {
			//TODO call the processor's end method when available
		}
	}

	protected interface Column {

		/** @return The index of the column. */
		public int getIndex();

		/** @return The original literal header of the CSV document. */
		public String getHeader();

		/** @return The property reference, which will be empty if the column does not represent a property. */
		public Optional<UrfReference> getProperty();

		/** @return The tag of the column type (the range of the property), which will be empty if default CSV strings should be assumed. */
		public Optional<URI> getTypeTag();

		/**
		 * Parses a field in the column.
		 * @implNote This method should never throw an {@link IOException} that is not an instance of {@link ParseIOException}.
		 * @param field
		 * @return A reference to the object, which will be a {@link ObjectUrfResource} for any value objects.
		 * @throws IOException If there was an error reading the data.
		 * @throws ParseIOException if the parsed data was invalid.
		 */
		public UrfReference parseField(@Nonnull final String field) throws IOException, ParseIOException;

	}

	protected static abstract class AbstractColumn implements Column {

		private final int index;

		@Override
		public int getIndex() {
			return index;
		}

		private final String header;

		@Override
		public String getHeader() {
			return header;
		}

		/**
		 * Constructor.
		 * @param index The index of the column.
		 * @param header The original literal header of the CSV document.
		 * @throws NullPointerException if the header is <code>null</code>.
		 * @throws IllegalArgumentException if the given index is negative.
		 */
		public AbstractColumn(@Nonnegative final int index, @Nonnull final String header) {
			this.index = checkArgumentNotNegative(index);
			this.header = requireNonNull(header);
		}

	}

	/**
	 * Represents a column that is ignored, that is, excluded from parsing.
	 * <p>
	 * Calling {@link #parseField(String)} will result in an exception.
	 * </p>
	 * @author Garret Wilson
	 */
	protected static class IgnoredColumn extends AbstractColumn {

		/**
		 * Constructor.
		 * @param index The index of the column.
		 * @param header The original literal header of the CSV document.
		 * @throws NullPointerException if the header is <code>null</code>.
		 * @throws IllegalArgumentException if the given index is negative.
		 */
		public IgnoredColumn(@Nonnegative final int index, @Nonnull final String header) {
			super(index, header);
		}

		@Override
		public Optional<UrfReference> getProperty() {
			return Optional.empty();
		}

		@Override
		public Optional<URI> getTypeTag() {
			return Optional.empty();
		}

		/** @implSpec This implementation throws an {@link UnsupportedOperationException}. */
		@Override
		public UrfReference parseField(String field) throws IOException, ParseIOException {
			throw new UnsupportedOperationException("Ignored columns should not be parsed");
		}

	}

	protected static abstract class BaseColumn extends AbstractColumn {

		private final Optional<UrfReference> property;

		@Override
		public Optional<UrfReference> getProperty() {
			return property;
		}

		private final Optional<URI> typeTag;

		@Override
		public Optional<URI> getTypeTag() {
			return typeTag;
		}

		/**
		 * Constructor.
		 * @param index The index of the column.
		 * @param header The original literal header of the CSV document.
		 * @param property The tag of the property, or <code>null</code> if this column doesn't represent a property.
		 * @param typeTag The tag of the column type, or <code>null</code> if no type was specified.
		 * @throws NullPointerException if the header is <code>null</code>.
		 * @throws IllegalArgumentException if the given index is negative.
		 */
		public BaseColumn(@Nonnegative final int index, @Nonnull final String header, @Nullable final URI propertyTag, @Nullable final URI typeTag) {
			super(index, header);
			this.property = propertyTag != null ? Optional.of(new SimpleUrfReference(propertyTag)) : Optional.empty();
			this.typeTag = Optional.ofNullable(typeTag);
		}

	}

	protected static class DefaultColumn extends BaseColumn {

		private final IOFunction<String, UrfReference> parseStrategy;

		/**
		 * Constructor.
		 * @param index The index of the column.
		 * @param header The original literal header of the CSV document.
		 * @param propertyTag The tag of the property, or <code>null</code> if this column doesn't represent a property.
		 * @param typeTag The tag of the column type, or <code>null</code> if no type was specified.
		 * @param parseStrategy The function for parsing a field.
		 * @throws NullPointerException if the header and/or parse strategy is <code>null</code>.
		 * @throws IllegalArgumentException if the given index is negative.
		 */
		public DefaultColumn(@Nonnegative final int index, @Nonnull final String header, @Nullable final URI propertyTag, @Nullable final URI typeTag,
				@Nonnull final IOFunction<String, UrfReference> parseStrategy) {
			super(index, header, propertyTag, typeTag);
			this.parseStrategy = requireNonNull(parseStrategy);
		}

		@Override
		public UrfReference parseField(final String field) throws IOException, ParseIOException {
			return parseStrategy.apply(requireNonNull(field));
		}

	}

	/**
	 * A column that constructs a value resource tag by using the field value as an ID of the column type. For example if the column type is <code>Example</code>
	 * and the field is <code>fooBar</code>, the resulting reference will be <code>Example#fooBar</code>.
	 * @author Garret Wilson
	 */
	protected static class IdReferenceColumn extends BaseColumn {

		private final URI typeTag;

		/**
		 * Constructor.
		 * @param index The index of the column.
		 * @param header The original literal header of the CSV document.
		 * @param propertyTag The tag of the property, or <code>null</code> if this column doesn't represent a property.
		 * @param typeTag The tag of the column type.
		 * @throws NullPointerException if the header and/or type tag is <code>null</code>.
		 * @throws IllegalArgumentException if the given index is negative.
		 */
		public IdReferenceColumn(@Nonnegative final int index, @Nonnull final String header, @Nullable final URI propertyTag, @Nullable final URI typeTag,
				@Nonnull final IOFunction<String, UrfReference> parseStrategy) {
			super(index, header, propertyTag, typeTag);
			this.typeTag = requireNonNull(typeTag); //store the type tag locally for efficient lookup without requiring Optional overhead, as we know there must be a type
		}

		/**
		 * @implSpec This implementation returns a reference with a tag formed by the type tag and the given field.
		 * @see #getTypeTag()
		 */
		@Override
		public UrfReference parseField(final String field) throws IOException, ParseIOException {
			return new SimpleUrfReference(Tag.forTypeId(typeTag, field));
		}

	}

}
