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

package io.urf.cli;

import static io.urf.URF.*;
import static java.nio.charset.StandardCharsets.*;
import static java.nio.file.Files.*;

import java.io.*;
import java.net.URI;
import java.nio.file.Path;

import java.util.StringJoiner;
import java.util.stream.Stream;

import javax.annotation.*;

import org.fusesource.jansi.AnsiConsole;
import org.slf4j.event.Level;

import com.globalmentor.io.Filenames;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.processor.AbstractRowProcessor;
import com.univocity.parsers.csv.*;

import io.clogr.*;
import io.confound.config.file.ResourcesConfigurationManager;
import io.urf.model.*;
import io.urf.turf.TurfSerializer;
import picocli.CommandLine;
import picocli.CommandLine.*;

/**
 * Command-line interface for working with URF data.
 * @author Garret Wilson
 */
@Command(name = "urf", description = "Command-line interface for working with URF data.", versionProvider = UrfCli.VersionProvider.class, mixinStandardHelpOptions = true)
public class UrfCli implements Runnable, Clogged {

	private boolean debug;

	/**
	 * Enables or disables debug mode, which is disabled by default.
	 * @param debug The new state of debug mode.
	 */
	@Option(names = {"--debug", "-d"}, description = "Turns on debug level logging.")
	protected void setDebug(final boolean debug) {
		this.debug = debug;
		updateLogLevel();
	}

	/**
	 * Returns whether debug mode is enabled.
	 * <p>
	 * Debug mode enables debug level logging and may also enable other debug functionality.
	 * </p>
	 * @return The state of debug mode.
	 */
	protected boolean isDebug() {
		return debug;
	}

	/** Updates the log level based upon the current debug setting. The current debug setting remains unchanged. */
	protected void updateLogLevel() {
		final Level logLevel = debug ? Level.DEBUG : Level.WARN; //TODO default to INFO level when we provide a log output (e.g. to file) option
		Clogr.getLoggingConcern().setLogLevel(logLevel);
	}

	/**
	 * Main program entry method.
	 * @param args Program arguments.
	 */
	public static void main(@Nonnull final String[] args) {
		AnsiConsole.systemInstall();
		CommandLine.run(new UrfCli(), args);
		AnsiConsole.systemUninstall();
	}

	/** Constructor. */
	public UrfCli() {
		updateLogLevel(); //update the log level based upon the debug setting
	}

	@Override
	public void run() {
		CommandLine.usage(this, System.out);
	}

	@Command(description = "Converts one or more files to some URF file format.")
	public void convert(@Option(names = {"--out", "-o"}) final Path output, @Parameters(paramLabel = "<file>", arity = "1..*") @Nonnull final Path[] paths) {
		//TODO precondition paths length check
		final Path path = paths[0];
		//TODO precondition check filename
		//TODO convert to PascalCase; remove any "s"; make configurable
		final String typeHandle = Filenames.removeExtension(path.getFileName().toString());
		final URI typeTag = Handle.toTag(typeHandle);

		final SimpleGraphUrfProcessor urfProcessor = new SimpleGraphUrfProcessor();

		final CsvParserSettings parserSettings = new CsvParserSettings();
		//TODO parserSettings.setLineSeparatorDetectionEnabled(true);
		parserSettings.getFormat().setQuote('\0'); //TODO analyze to guess; make configurable
		parserSettings.setHeaderExtractionEnabled(true); //TODO make configurable
		parserSettings.setProcessor(new AbstractRowProcessor() {
			@Override
			public void rowProcessed(final String[] row, final ParsingContext context) {
				super.rowProcessed(row, context);

				System.out.print(String.format("Processing line %d...\r", context.currentLine()));

				final StringJoiner rowJoiner = new StringJoiner("|", "|", "|");
				Stream.of(row).forEach(rowJoiner::add);
				getLogger().trace(rowJoiner.toString()); //TODO delete

				final URI subjectTag = Tag.forTypeId(typeTag, row[0]); //TODO allow configuration of "ID" column
				final UrfReference subject = UrfReference.ofTag(subjectTag);
				urfProcessor.reportRootResource(subject);

				final String[] headers = context.headers();
				for(int columnIndex = 0, columnCount = row.length; columnIndex < columnCount; columnIndex++) {

					//property TODO calculate these beforehand
					if(columnIndex >= headers.length) {
						//TODO getLogger().warn("No property for {}, line {}, column index {}.", row[columnIndex], context.currentLine(), columnIndex);
						continue; //TODO provide formal warning/error feedback to importer
					}
					final String header = headers[columnIndex];
					final String propertyHandle = header; //TODO convert to camelCase; allow configuration
					final URI propertyTag = Handle.toTag(propertyHandle);
					final UrfReference property = UrfReference.ofTag(propertyTag);

					//property value
					final String propertyValueString = row[columnIndex];
					if(propertyValueString == null) { //skip null values TODO how did the parser know this should be null?
						continue;
					}

					final ValueUrfResource<String> object = new DefaultValueUrfResource<>(STRING_TYPE_TAG, propertyValueString);

					urfProcessor.processStatement(subject, property, object);
				}
			}

			@Override
			public void processEnded(final ParsingContext context) {
				super.processEnded(context);
				System.out.println("\nConversion finished.");
			}
		});

		final CsvParser csvParser = new CsvParser(parserSettings);
		//TODO detect encoding
		try {
			try (final Reader reader = new InputStreamReader(new BufferedInputStream(newInputStream(path)), UTF_8)) {
				csvParser.parse(reader);
			}
			final TurfSerializer turfSerializer = new TurfSerializer();
			turfSerializer.setFormatted(true);
			if(output != null) {
				try (final OutputStream outputStream = new BufferedOutputStream(newOutputStream(output))) {
					turfSerializer.serializeDocument(outputStream, urfProcessor);
				}
			} else {
				turfSerializer.serializeDocument((Appendable)System.out, urfProcessor); //TODO improve varargs in signature to prevent need for cast
			}
		} catch(final IOException ioException) {
			getLogger().error("Error converting file.", ioException);
			System.err.println(ioException.getMessage());
		}

	}

	/**
	 * Strategy for retrieving a version from the configuration.
	 * @author Garret Wilson
	 */
	static class VersionProvider implements IVersionProvider {

		/** The configuration key containing the version of the program. */
		public static final String CONFIG_KEY_VERSION = "version";

		@Override
		public String[] getVersion() throws Exception {
			return new String[] {ResourcesConfigurationManager.loadConfigurationForClass(UrfCli.class)
					.orElseThrow(ResourcesConfigurationManager::createConfigurationNotFoundException).getString(CONFIG_KEY_VERSION)};
		}

	}
}
