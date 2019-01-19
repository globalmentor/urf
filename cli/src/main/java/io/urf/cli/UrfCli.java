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
import static java.nio.file.Files.*;

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.*;

import org.fusesource.jansi.AnsiConsole;
import org.slf4j.event.Level;

import com.globalmentor.io.Filenames;

import io.clogr.*;
import io.confound.config.file.ResourcesConfigurationManager;
import io.urf.csv.UrfCsvParser;
import io.urf.model.*;
import io.urf.turf.TurfParser;
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
	public void convert(
			@Option(names = {"--schema", "-s"}, description = "Indicates a schema file for validation and making inferences.") @Nonnull final Path schemaPath,
			@Option(names = {"--out", "-o"}) final Path outputPath, @Parameters(paramLabel = "<file>", arity = "1..*") @Nonnull final Path[] paths) {
		//TODO precondition paths length check
		//TODO detect a glob as the first path, for shells that don't enumerate the file automatically

		try {

			//load schema if requested
			final UrfInferencer urfInferencer;
			if(schemaPath != null) {
				try (final InputStream inputStream = new BufferedInputStream(newInputStream(schemaPath))) {
					final SimpleGraphUrfProcessor schemaProcessor = new SimpleGraphUrfProcessor();
					new TurfParser<>(schemaProcessor).parseDocument(inputStream);
					urfInferencer = new ExperimentalUrfInferencer(() -> schemaProcessor.getDeclaredObjects().iterator());
				}
			} else {
				urfInferencer = UrfInferencer.NOP;
			}

			final SimpleGraphUrfProcessor urfProcessor = new SimpleGraphUrfProcessor(urfInferencer);

			//process files		
			for(final Path path : paths) { //parse each file using the same processor
				System.out.println(String.format("Converting file %s...", path.getFileName()));
				//TODO precondition check filename
				final String typeHandle = Filenames.getBaseFilename(path.getFileName().toString());
				final URI typeTag = Handle.toTag(typeHandle);

				final UrfCsvParser<List<Object>> urfCsvParser = new UrfCsvParser<>(urfProcessor);
				try (final InputStream inputStream = new BufferedInputStream(newInputStream(path))) {
					//TODO provide updates to the user during parsing and when writing
					urfCsvParser.parseDocument(inputStream, typeTag);
				}
			}
			final TurfSerializer turfSerializer = new TurfSerializer();
			turfSerializer.setFormatted(true);
			if(outputPath != null) {
				System.out.println(String.format("Writing output file %s...", outputPath.getFileName()));
				try (final OutputStream outputStream = new BufferedOutputStream(newOutputStream(outputPath))) {
					turfSerializer.serializeDocument(outputStream, urfProcessor);
				}
			} else {
				turfSerializer.serializeDocument((Appendable)System.out, urfProcessor); //TODO improve varargs in signature to prevent need for cast
			}
			System.out.println("Conversion finished.");
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
