# RegexSolver Java API Client

[Homepage](https://regexsolver.com) | [Online Demo](https://regexsolver.com/demo) | [Documentation](https://docs.regexsolver.com) | [Developer Console](https://console.regexsolver.com)

**RegexSolver** is a powerful toolkit for building, combining, and analyzing regular expressions. It is designed for constraint solvers, test generators, and other systems that need advanced regex operations.

## Installation

### Requirements

- Java >=11

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

## Usage

1. Create an API token in the [Developer Console](https://console.regexsolver.com/).
2. Initialize the client and start working with terms:

```java
import com.regexsolver.api.RegexSolver;
import com.regexsolver.api.Term;
import com.regexsolver.api.exception.ApiError;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, ApiError {
        // Set REGEXSOLVER_API_TOKEN in your env and call initialize(),
        // or pass the token directly:
        RegexSolver.initialize(); // or RegexSolver.initialize("YOUR_API_TOKEN");

        // Create terms
        Term term1 = Term.regex("(abc|de|fg){2,}");
        Term term2 = Term.regex("de.*");
        Term term3 = Term.regex(".*abc");

        // Compute intersection and difference
        Term result = term1.intersection(term2, term3)
                .difference(Term.regex(".+(abc|de).+"));
        System.out.println(result.getPattern()); // de(fg)*abc
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

By default, the engine returns whatever the operation produces, with no extra convertion. Override with `responseFormat`:

```java
Term term = Term.regex("abcde");

OperationOptions operationOptions = OperationOptions.newDefault()
        .responseFormat(ResponseFormat.REGEX);
Term result1 = term.union(operationOptions, Term.regex("de"));

System.out.println(result1); // regex=(abc)?de

operationOptions = OperationOptions.newDefault()
        .responseFormat(ResponseFormat.FAIR);
Term result2 = term.union(operationOptions, Term.regex("de"));

System.out.println(result2); // fair=...
```

If the format does not matter, omit `responseFormat` or set it to `ResponseFormat.ANY`.

Regardless of the format, you can always call `getPattern()` to obtain the regex pattern of a term.

## Bounding execution time

Set a server-side compute timeout in milliseconds with `executionTimeout`:

```java
// Limit the server-side compute time to 5 ms
try {
    Term term1 = Term.regex(".*ab.*c(de|fg).*dab.*c(de|fg).*ab.*c(de|fg).*dab.*c");
    Term term2 = Term.regex(".*abc.*");

    OperationOptions operationOptions = OperationOptions.newDefault()
        .executionTimeout(5);
    Term out = term1.difference(operationOptions, term2);
} catch (ApiError e) {
    System.out.println(e.getMessage()); // The operation took too much time.
}
```

Timeout is best effort. The exact time is not guaranteed.

## API Overview

`Term` exposes the following methods.

### Build
| Method | Return | Description |
| -------- | ------- | ------- |
| `Term.fair(String fair)` | `Term` | Creates a term from a FAIR. |
| `Term.regex(String regex)` | `Term` | Creates a term from a regex pattern. |

### Analyze

| Method | Return | Description |
| -------- | ------- | ------- |
| `t.equivalent(Term term)` | `boolean` | `true` if `t` and `term` accept exactly the same language. Supports `executionTimeout`. |
| `t.getCardinality()` | `Cardinality` | Returns the cardinality of the term (i.e., the number of possible matched strings). |
| `t.getDot()` | `String` | Returns a Graphviz DOT representation of the automaton for the term. |
| `t.getFair()` | `String` | Returns the FAIR of the term if defined. |
| `t.getLength()` | `Length` | Returns the minimum and maximum length of matched strings. |
| `t.getPattern()` | `String` | Returns a regular expression pattern for the term. |
| `t.isEmpty()` | `boolean` | `true` if the term matches no string. |
| `t.isEmptyString()` | `boolean` | `true` if the term matches only the empty string. |
| `t.isTotal()` | `boolean` | `true` if the term matches all possible strings. |
| `t.subset(Term term)` | `boolean` | `true` if every string matched by `t` is also matched by `term`. Supports `executionTimeout`. |

### Compute

| Method | Return | Description |
| -------- | ------- | ------- |
| `t.concat(Term... terms)` | `Term` | Concatenates `t` with the given terms. Supports `responseFormat` and `executionTimeout`. |
| `t.difference(Term term)` | `Term` | Computes the difference `t - term`. Supports `responseFormat` and `executionTimeout`. |
| `t.intersection(Term... terms)` | `Term` | Computes the intersection of `t` with the given terms. Supports `responseFormat` and `executionTimeout`. |
| `t.repeat(int min, Integer max)` | `Term` | Computes the repetition of the term between `min` and `max` times; if `max` is `null`, the repetition is unbounded. Supports `responseFormat` and `executionTimeout`. |
| `t.union(Term... terms)` | `Term` | Computes the union of `t` with the given terms. Supports `responseFormat` and `executionTimeout`. |

### Generate

| Method | Return | Description |
| -------- | ------- | ------- |
| `t.generateStrings(int count)` | `String[]` | Generates up to `count` unique example strings matched by `t`. Supports `executionTimeout`. |

### Other
| Method | Return | Description |
| -------- | ------- | ------- |
| `t.serialize()` | `String` | Returns a serialized form of `t`. |
| `Term.deserialize(String string)` | `Term` | Returns a deserialized term from the given `string`. |

## Cross-Language Support

If you want to use this library with other programming languages, we provide:
- [regexsolver-js](https://github.com/RegexSolver/regexsolver-js)
- [regexsolver-python](https://github.com/RegexSolver/regexsolver-python)

For more information about how to use the wrappers, you can refer to our [guide](https://docs.regexsolver.com/getting-started.html).

You can also take a look at [regexsolver](https://github.com/RegexSolver/regexsolver) which contains the source code of the engine.

## License

This project is licensed under the MIT License.
