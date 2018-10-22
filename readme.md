# SURF

Reference implementation of the Simple URF (SURF) document format.

## Download

SURF is available in the Maven Central Repository as [io.urf:surf](https://search.maven.org/search?q=g:io.urf%20AND%20a:surf).

SURF test documents are available in the Maven Central Repository as [io.urf:surf:jar:tests](https://search.maven.org/search?q=g:io.urf%20AND%20a:surf%20AND%20l:tests).

## Issues

Issues tracked by [JIRA](https://globalmentor.atlassian.net/projects/URF).

## Changelog

- v0.2.2: (2018-10-22) Included test JAR in release.
- v0.2.1: (2018-10-22) Added constants and refactored tests for use with TURF.
- v0.2.0: (2017-12-15)
	* [URF-49](https://globalmentor.atlassian.net/browse/JAVA-49): Move SURF test resources into consolidated SURF project as test-jar.
	* [URF-48](https://globalmentor.atlassian.net/browse/JAVA-48): Add support for objects as map keys.
	* [URF-47](https://globalmentor.atlassian.net/browse/JAVA-47): Use term "label" to refer to SURF construct containing tags, IDs, and aliases.
	* [URF-44](https://globalmentor.atlassian.net/browse/JAVA-44): Combine SURF implementation into single project, repository, and package.
	* [URF-41](https://globalmentor.atlassian.net/browse/JAVA-41): Add parser support for SURF IDs.
	* [URF-40](https://globalmentor.atlassian.net/browse/JAVA-40): Update API to use SURF "handles".
	* [URF-38](https://globalmentor.atlassian.net/browse/JAVA-38): Serializer not writing data to a ByteArrayOutputStream
	* [URF-31](https://globalmentor.atlassian.net/browse/JAVA-31): Change SURF binary literal encoding to use base64url.
	* [URF-29](https://globalmentor.atlassian.net/browse/JAVA-29): Rename SURF "labels" to "tags".
	* [URF-28](https://globalmentor.atlassian.net/browse/JAVA-28): Add label serialization support.
	* [URF-27](https://globalmentor.atlassian.net/browse/JAVA-27): Add parsing support for nested ident references.
	* [URF-26](https://globalmentor.atlassian.net/browse/JAVA-26): Add support for SURF IRI short forms.
	* [URF-25](https://globalmentor.atlassian.net/browse/JAVA-25): Move SurfObject to separate model package.
	* [JAVA-43](https://globalmentor.atlassian.net/browse/JAVA-43): Updated SURF parser methods to reflect improved parser naming conventions.
- v0.1.0: (2017-05-15) First public release.
