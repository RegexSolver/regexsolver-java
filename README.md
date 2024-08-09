# RegexSolver Java API Client

[Homepage](https://regexsolver.com) | [Documentation](https://docs.regexsolver.com) | [Developer Console](https://console.regexsolver.com)

This repository contains the source code of the Java library for [RegexSolver](https://regexsolver.com) API.

RegexSolver is a powerful regular expression manipulation toolkit, that gives you the power to manipulate regex as if
they were sets.

## Installation

### Requirements

- Java >=11

### Maven

```xml
<dependency>
    <groupId>com.regexsolver.api</groupId>
    <artifactId>RegexSolver</artifactId>
    <version>1.0.2</version>
</dependency>
```

### Gradle

```groovy
implementation "com.regexsolver.api:RegexSolver:1.0.2"
```

## Usage

In order to use the library you need to generate an API Token on
our [Developer Console](https://console.regexsolver.com/).

```java
import com.regexsolver.api.RegexSolver;
import com.regexsolver.api.Term;
import com.regexsolver.api.exception.ApiError;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, ApiError {
        RegexSolver.initialize("YOUR TOKEN HERE");

        Term term1 = Term.regex("(abc|de|fg){2,}");
        Term term2 = Term.regex("de.*");
        Term term3 = Term.regex(".*abc");

        Term term4 = Term.regex(".+(abc|de).+");

        Term result = term1.intersection(term2, term3)
                .subtraction(term4);

        System.out.println(result);
    }
}
```

## Features

- [Intersection](#intersection)
- [Union](#union)
- [Subtraction / Difference](#subtraction--difference)
- [Equivalence](#equivalence)
- [Subset](#subset)
- [Details](#details)
- [Generate Strings](#generate-strings)

### Intersection

#### Request

Compute the intersection of the provided terms and return the resulting term.

The maximum number of terms is currently limited to 10.

```java
Term.Regex term1 = Term.regex("(abc|de){2}");
Term.Regex term2 = Term.regex("de.*");
Term.Regex term3 = Term.regex(".*abc");

Term result = term1.intersection(term2, term3);
System.out.println(result);
```

#### Response

```
regex=deabc
```

### Union

Compute the union of the provided terms and return the resulting term.

The maximum number of terms is currently limited to 10.

#### Request

```java
Term.Regex term1 = Term.regex("abc");
Term.Regex term2 = Term.regex("de");
Term.Regex term3 = Term.regex("fghi");

Term result = term1.union(term2, term3);
System.out.println(result);
```

#### Response

```
regex=(abc|de|fghi)
```

### Subtraction / Difference

Compute the first term minus the second and return the resulting term.

#### Request

```java
Term.Regex term1 = Term.regex("(abc|de)");
Term.Regex term2 = Term.regex("de");

Term result = term1.subtraction(term2);
System.out.println(result);
```

#### Response

```
regex=abc
```

### Equivalence

Analyze if the two provided terms are equivalent.

#### Request

```java
Term.Regex term1 = Term.regex("(abc|de)");
Term.Fair term2 = Term.regex("(abc|de)*");

boolean result = term1.isEquivalentTo(term2);
System.out.println(result);
```

#### Response

```
false
```

### Subset

Analyze if the second term is a subset of the first.

#### Request

```java
Term.Regex term1 = Term.regex("de");
Term.Regex term2 = Term.regex("(abc|de)");

boolean result = term1.isSubsetOf(term2);
System.out.println(result);
```

#### Response

```
true
```

### Details

Compute the details of the provided term.

The computed details are:

- **Cardinality:** the number of possible values.
- **Length:** the minimum and maximum length of possible values.
- **Empty:** true if is an empty set (does not contain any value), false otherwise.
- **Total:** true if is a total set (contains all values), false otherwise.

#### Request

```java
Term.Regex term = Term.regex("(abc|de)");

Details details = term.getDetails();
System.out.println(details);
```

#### Response

```
Details[cardinality=Integer(2), length=Length[minimum=2, maximum=3], empty=false, total=false]
```

### Generate Strings

Generate the given number of strings that can be matched by the provided term.

The maximum number of strings to generate is currently limited to 200.

#### Request

```java
Term.Regex term = Term.regex("(abc|de){2}");

List<String> strings = term.generateStrings(3);
System.out.println(strings);
```

#### Response

```
[abcde, dede, deabc]
```
