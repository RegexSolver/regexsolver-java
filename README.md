# RegexSolver Java API Client
[Homepage](https://regexsolver.com) | [Online Demo](https://regexsolver.com/demo) | [Documentation](https://docs.regexsolver.com) | [Developer Console](https://console.regexsolver.com)

**RegexSolver** is a powerful toolkit for building, combining, and analyzing regular expressions. It is designed for constraint solvers, test generators, and other systems that need advanced regex operations.

## Installation

### Maven

```xml
<dependency>
    <groupId>com.regexsolver.api</groupId>
    <artifactId>RegexSolver</artifactId>
    <version>1.1.0</version>
</dependency>
```

### Gradle

```groovy
implementation "com.regexsolver.api:RegexSolver:1.1.0"
```

Requirements: **Java >= 11**

## Quick Start

1. Create an API token in the [Developer Console](https://console.regexsolver.com/).
2. Initialize the client and start working with terms.

### Synchronous Usage

The synchronous client provides a simple, blocking API.

```java
import com.regexsolver.api.RegexSolverClient;
import com.regexsolver.api.Term;

public class Main {
    public static void main(String[] args) {
        RegexSolverClient client = RegexSolverClient.builder()
                .apiToken("REGEXSOLVER_API_TOKEN")
                .build();

        Term term1 = Term.regex("(abc|de|fg){2,}");
        Term term2 = Term.regex("de.*");

        Term intersection = client.intersection(term1, term2);
        String pattern = client.getPattern(intersection);
        System.out.println(pattern); // de(abc|de|fg)+
    }
}
```

### Asynchronous Usage

For non-blocking applications, use the asynchronous client.

```java
import com.regexsolver.api.AsyncRegexSolverClient;
import com.regexsolver.api.Term;
import java.util.concurrent.CompletableFuture;

public class Main {
    public static void main(String[] args) {
        AsyncRegexSolverClient client = AsyncRegexSolverClient.builder()
                .apiToken("REGEXSOLVER_API_TOKEN")
                .build();

        Term term1 = Term.regex("(abc|de|fg){2,}");
        Term term2 = Term.regex("de.*");

        client.intersection(term1, term2)
                .thenCompose(client::getPattern)
                .thenAccept(System.out::println); // de(abc|de|fg)+
    }
}
```

## Key Concepts & Limitations

RegexSolver supports a subset of regular expressions that adhere to the principles of regular languages. Here are the key characteristics and limitations of the regular expressions supported by RegexSolver:
- **Anchored Expressions:** All regular expressions in RegexSolver are anchored. This means that the expressions are treated as if they start and end at the boundaries of the input text. For example, the expression `abc` will match the string "abc" but not "xabc" or "abcx".
- **Lookahead/Lookbehind:** RegexSolver does not support lookahead (`(?=...)`) or lookbehind (`(?<=...)`) assertions. Using them returns an error.
- **Pure Regular Expressions:** RegexSolver focuses on pure regular expressions as defined in regular language theory. This means features that extend beyond regular languages, such as backreferences (`\1`, `\2`, etc.), are not supported. Any use of backreference would return an error.
- **Greedy/Ungreedy Quantifiers:** The concept of ungreedy (`*?`, `+?`, `??`) quantifiers is not supported. All quantifiers are treated as greedy. For example, `a*` or `a*?` will match the longest possible sequence of "a"s.
- **Line Feed and Dot:** RegexSolver handles all characters the same way. The dot `.` matches any Unicode character including line feed (`\n`).
- **Empty Regular Expressions:** The empty language (matches no string) is represented by constructs like `[]` (empty character class). This is distinct from the empty string.

## Response Formats

The API can handle terms in two formats:
- `regex`: a regular expression pattern
- `fair`: FAIR (Fast Automaton Internal Representation), a stable, signed format used internally by the engine

By default, the engine returns whatever the operation produces, with no extra conversion. Override with `OperationOptions`, accepted by the operations that return a term:

```java
import com.regexsolver.api.ResponseFormat;
import com.regexsolver.api.OperationOptions;

Term term1 = Term.regex("abcde");
Term term2 = Term.regex("de");

Term result1 = client.union(term1, term2, new OperationOptions().responseFormat(ResponseFormat.REGEX));
System.out.println(result1); // regex=(abc)?de

Term result2 = client.union(term1, term2, new OperationOptions().responseFormat(ResponseFormat.FAIR));
System.out.println(result2); // fair=...
```

If the format does not matter, omit `responseFormat` or set it to `ResponseFormat.ANY`.

Regardless of the format, you can always call `getPattern()` to obtain the regex pattern of a term.

## Bounding execution time

Set a server-side compute timeout in milliseconds with `executionTimeout` in `OperationOptions`:

```java
import com.regexsolver.api.exceptions.TimeoutExceededException;
import com.regexsolver.api.OperationOptions;

// Limit the server-side compute time to 100 ms
try {
    Term term1 = Term.regex(".*ab.*c(de|fg).*dab.*c(de|fg).*ab.*c(de|fg).*dab.*c");
    Term term2 = Term.regex(".*abc.*");
    
    Term res = client.difference(term1, term2, new OperationOptions().executionTimeout(100));
} catch (TimeoutExceededException error) {
    System.out.println(error.getMessage()); // The operation took too much time.
}
```

Timeout is best effort. The exact time is not guaranteed.

## API Overview

`RegexSolverClient` and `AsyncRegexSolverClient` expose the following methods. Every method accepts an optional `OperationOptions` as its last parameter (`responseFormat`, `deterministic`, `executionTimeout`). An option that does not apply to an operation is ignored: analyze operations and `determinize()` only honour `executionTimeout` — the response format is not theirs to choose.

### Analyze

| Method | Return | Description |
| -------- | ------- | ------- |
| `client.equivalent(term1, term2, options?)` | `boolean` | `true` if `term1` and `term2` accept exactly the same language. |
| `client.getCardinality(term, options?)` | `Cardinality` | Returns the number of possible matched strings. |
| `client.getDot(term, options?)` | `String` | Returns a Graphviz DOT representation of the automaton. |
| `client.getLength(term, options?)` | `Length` | Returns the minimum and maximum length of matched strings. |
| `client.getPattern(term, options?)` | `String` | Returns a regular expression pattern for the term. |
| `client.isEmpty(term, options?)` | `boolean` | `true` if the term matches no string. |
| `client.isEmptyString(term, options?)` | `boolean` | `true` if the term matches only the empty string. |
| `client.isTotal(term, options?)` | `boolean` | `true` if the term matches all possible strings. |
| `client.isDeterministic(term, options?)` | `boolean` | `true` if the term's automaton is deterministic. Only a deterministic FAIR guarantees consistent string ordering across paginated `generateStrings()` calls; call `determinize()` first if this is `false`. |
| `client.subset(term1, term2, options?)` | `boolean` | `true` if every string matched by `term1` is also matched by `term2`. |

*Note: For `AsyncRegexSolverClient`, these methods return `CompletableFuture`.*

### Compute

| Method | Return | Description |
| -------- | ------- | ------- |
| `client.complement(term, options?)` | `Term` | Computes the complement of the given term. |
| `client.concat(term1, term2, ..., options?)` | `Term` | Concatenates multiple terms in order. |
| `client.determinize(term, options?)` | `Term` | Computes a deterministic FAIR for the given term, suitable for consistent pagination with `generateStrings()`. |
| `client.difference(term1, term2, options?)` | `Term` | Computes the difference `term1 - term2`. |
| `client.intersection(term1, term2, ..., options?)` | `Term` | Computes the intersection of the given terms. |
| `client.repeat(term, min, max, options?)` | `Term` | Computes the repetition of the term between `min` and `max` times. |
| `client.union(term1, term2, ..., options?)` | `Term` | Computes the union of the given terms. |

*Note: For `AsyncRegexSolverClient`, these methods return `CompletableFuture<Term>`.*

### Generate

| Method | Return | Description |
| -------- | ------- | ------- |
| `client.generateStrings(term, limit, offset, options?)` | `List<String>` | Generates up to `limit` unique strings matched by `term`, skipping the first `offset` strings. |

*Note: For `AsyncRegexSolverClient`, this method returns `CompletableFuture<List<String>>`.*

## Cross-Language Support

If you want to use this library with other programming languages, we provide:
- [regexsolver-js](https://github.com/RegexSolver/regexsolver-js)
- [regexsolver-python](https://github.com/RegexSolver/regexsolver-python)

For more information about how to use the wrappers, you can refer to our [guide](https://docs.regexsolver.com/getting-started.html).

You can also take a look at [regexsolver](https://github.com/RegexSolver/regexsolver) which contains the source code of the engine.

## License

This project is licensed under the MIT License.
