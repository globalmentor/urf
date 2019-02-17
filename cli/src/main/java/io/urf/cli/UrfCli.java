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
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.*;

import com.globalmentor.application.*;
import com.globalmentor.io.Filenames;

import io.urf.csv.UrfCsvParser;
import io.urf.model.*;
import io.urf.turf.TURF;
import io.urf.turf.TurfParser;
import io.urf.turf.TurfSerializer;
import picocli.CommandLine.*;

/**
 * Command-line interface for working with URF data.
 * @author Garret Wilson
 */
@Command(name = "urf", description = "Command-line interface for working with URF data.", versionProvider = UrfCli.MetadataProvider.class, mixinStandardHelpOptions = true)
public class UrfCli extends BaseCliApplication {

	/**
	 * Constructor.
	 * @param args The command line arguments.
	 */
	public UrfCli(@Nonnull final String[] args) {
		super(args);
	}

	/**
	 * Main program entry method.
	 * @param args Program arguments.
	 */
	public static void main(@Nonnull final String[] args) {
		Application.start(new UrfCli(args));
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

			//TODO add a way to have feedback final AtomicLong rootCount = new AtomicLong(0);
			final SimpleGraphUrfProcessor urfProcessor = new SimpleGraphUrfProcessor(urfInferencer);
			//TODO add a way to have feedback
			//			{
			//				@Override
			//				public void reportRootResource(final UrfReference root) {
			//					super.reportRootResource(root);
			//					final long newRootCount = rootCount.incrementAndGet();
			//					if(newRootCount % 1000 == 0) {
			//						System.out.println(newRootCount);
			//					}
			//				};
			//			};

			//process files		
			for(final Path path : paths) { //parse each file using the same processor
				System.out.println(String.format("Converting file %s...", path.getFileName()));
				//TODO precondition check filename
				final String typeHandle = Filenames.getBaseFilename(path.getFileName().toString());
				final URI typeTag = Handle.toTag(typeHandle);

				try (final InputStream inputStream = new BufferedInputStream(newInputStream(path))) {

					if(Filenames.getExtension(path.getFileName().toString()).equals(TURF.FILENAME_EXTENSION)) {
						final TurfParser<List<Object>> turfParser = new TurfParser<>(urfProcessor);
						//TODO provide updates to the user during parsing and when writing
						turfParser.parseDocument(inputStream);
					} else { //assume URF CSV
						final UrfCsvParser<List<Object>> urfCsvParser = new UrfCsvParser<>(urfProcessor);
						//TODO provide updates to the user during parsing and when writing
						urfCsvParser.parseDocument(inputStream, typeTag);
					}

				}

				//only record root resources for the first file, to prevent forcing references to be serialized as roots from other aggregated files
				urfProcessor.setRootsRecorded(false);
			}

			//TODO add some sort of summary
			//System.out.println("number of declared resources: " + urfProcessor.getDeclaredResources().count());
			//System.out.println("number of cached URIs: " + URF.Handle.AD_HOC_TAGS_BY_HANDLE_CACHE.size());
			//URF.Handle.AD_HOC_TAGS_BY_HANDLE_CACHE.entrySet().stream().limit(100)
			//		.forEach(entry -> System.out.println(String.format("%s : %s", entry.getKey(), entry.getValue())));

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

	/** Strategy for providing version and other information from the configuration. */
	static class MetadataProvider extends AbstractMetadataProvider {
		public MetadataProvider() {
			super(UrfCli.class);
		}
	}
}
