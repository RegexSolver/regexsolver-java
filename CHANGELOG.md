# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed

- Jackson moves from 2.21.5 to 2.22.2, with `jackson-annotations` from 2.21 to 2.22, the version the 2.22.2 bill of materials pairs it with.
- `jakarta.annotation-api` moves from 2.1.1 to 3.0.0. The major bump is on the specification, not on anything the SDK relies on: 3.0.0 is still compiled for Java 11, and the only class it drops, `jakarta.annotation.ManagedBean`, is not one the generated code annotates with. The Java 11 baseline and the published API are unchanged.

## [1.1.0] - 2026-08-08

The static `RegexSolver` becomes an instantiable `RegexSolverClient`, with `AsyncRegexSolverClient` beside it, every operation moves from `Term` onto the client, and the SDK covers the whole API rather than the seven endpoints it knew about. See *Compatibility* below.

### Added

- `RegexSolverClient.builder()` and `AsyncRegexSolverClient.builder()`, taking `apiToken`, `baseUrl`, `autoBatch` and `maxTermsPerRequest`. Several clients, each with its own token, can exist in one process; the synchronous one blocks on the asynchronous one, whose operations return `CompletableFuture`.
- The operations the API gained since 1.0.2: `complement`, `concat`, `determinize`, `repeat`, `getCardinality`, `getDot`, `getLength`, `isEmpty`, `isEmptyString`, `isTotal`, `isDeterministic` and `getAccountLimits`.
- `OperationOptions` on every operation that returns a term, carrying `responseFormat` (`REGEX`, `FAIR` or `ANY`), `deterministic` and `executionTimeout`. Analyze operations and `determinize` honour `executionTimeout` alone.
- `generateStrings(term, limit, offset, options)`, which pages through the language instead of returning a fixed count from the start, with `pathOrder`, `characterOrder`, `seed`, `minLength`, `maxLength` and `charset` in `GenerateStringsOptions`. Paging is only consistent over a deterministic FAIR, hence `determinize()` and `isDeterministic()`.
- `Term.matches(str)`, evaluated locally with `java.util.regex` — matched against the whole input, compiled with `Pattern.DOTALL` — rather than by the API. It throws on a FAIR whose pattern is not known yet, and returns `false` for the empty language, which the engine writes as `[]`.
- An unchecked exception hierarchy under `RegexSolverException`, so a caller can catch the case it handles instead of matching on a message. `ApiException` carries `statusCode`, `errorCode` and `body`, and splits per status down to `RegexSyntaxException`, `TimeoutExceededException`, `QuotaExceededException` and the rest.
- A rate limiter, shared by every client holding the same token: a 429 sets a deadline from `Retry-After`, requests wait for it, and the operation is retried within a five-minute budget.
- Automatic batching: `concat`, `intersection` and `union` split a call carrying more terms than the account allows per request and fold the results back into one, each constituent request counting against the monthly quota. Disable with `autoBatch(false)`, or lower the split with `maxTermsPerRequest`.
- A per-term cache of what the API has already returned — cardinality, length, pattern, dot, and the empty, empty-string, total and deterministic flags — so asking twice costs one request.
- `Term.serialize()` / `Term.deserialize()`, round-tripping the `regex=<pattern>` / `fair=<payload>` form, plus `equals()`, `hashCode()`, `getValue()` and `toDto()` / `fromDto()`.
- `AccountLimits`, `Cardinality` (`Integer`, `BigInteger`, `Infinite`), `Length`, `ResponseFormat`, `PathOrder` and `CharacterOrder` as exported models.
- A `module-info.java`, so the SDK is a named module, `com.regexsolver.api`, on the module path.
- `generate-api.sh`, which regenerates `src/main/java/com/regexsolver/api/generated/` from the specification the API publishes at `https://api.regexsolver.com/openapi.json`; `.openapi-generator-ignore` protects the hand-written files.
- CI running the build and the tests on Java 11, 17 and 21, and a workflow publishing to Maven Central on a `v*` tag.
- A `CHANGELOG.md`, this file, a pull request template, and Dependabot updates.

### Changed

- The HTTP layer is code generated from the OpenAPI specification on `java.net.http` and Jackson, instead of hand-written Retrofit and OkHttp calls, with `com.regexsolver.api` the hand-written surface over `com.regexsolver.api.generated`.
- `Term` is an abstract class with `RegexTerm` and `FairTerm` subclasses, `regex()` and `fair()` unchanged. `getPattern()` and `getFair()` return `Optional<String>`, empty when the other format has not been resolved yet; resolving a pattern is `client.getPattern(term)`.
- The tests are JUnit 5 with Mockito and AssertJ, driving the clients over the generated API, instead of JUnit 4 matching requests with `mockwebserver` and loading fixtures from `src/test/resources/`.

### Removed

- `RegexSolver`, with its static `initialize()`, and `RegexSolverApiWrapper`.
- The operation methods on `Term`: `intersection()`, `union()`, `subtraction()`, `isEquivalentTo()`, `isSubsetOf()`, `generateStrings()` and `getDetails()`.
- `Details`, with the `com.regexsolver.api.dto` package, and the `com.regexsolver.api.exception` package, with `ApiError` and `MissingAPITokenException`.

### Compatibility

- `RegexSolver.initialize(token)` becomes `RegexSolverClient.builder().apiToken(token).build()`, and a term method becomes a client method taking the terms as arguments: `term1.union(term2)` is `client.union(term1, term2)`, `isEquivalentTo` is `equivalent`, `isSubsetOf` is `subset`, `subtraction` is `difference`, and `getDetails` is `getCardinality()`, `getLength()`, `isEmpty()` and `isTotal()`.
- `ApiError` is gone; catch `ApiException`, or the subclass for the case at hand, and branch on `getStatusCode()`. Since nothing is checked any more, `catch (IOException | ApiError e)` around a call no longer compiles.
- The endpoints moved from `https://api.regexsolver.com/api/*` to `/v1/*`, and `/api/analyze/details` is split into one `/v1/analyze/*` endpoint per property. 1.1.0 is the lowest version that works against the API.
- The serialized `regex=` / `fair=` form is unchanged, so a term persisted by 1.0.x deserializes.

## [1.0.2] - 2024-08-08

### Fixed

- The `User-Agent` header reports the version being released rather than the next one.

## [1.0.1] - 2024-07-27

### Added

- `RegexSolver`, with `initialize(token)` and `initialize(token, baseUrl)`. `RegexSolverApiWrapper` is package-private, so the SDK could not be initialized from outside its own package.

## [1.0.0] - 2024-07-27

Initial release.

[1.1.0]: https://github.com/RegexSolver/regexsolver-java/compare/v1.0.2...v1.1.0
[1.0.2]: https://github.com/RegexSolver/regexsolver-java/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/RegexSolver/regexsolver-java/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/RegexSolver/regexsolver-java/releases/tag/v1.0.0
