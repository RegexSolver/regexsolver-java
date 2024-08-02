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
    <version>1.0.1</version>
</dependency>
```

### Gradle

```groovy
implementation "com.regexsolver.api:RegexSolver:1.0.1"
```

## Usage

In order to use the library you need to generate an API Token on our [Developer Console](https://console.regexsolver.com/).

```java
import com.regexsolver.api.RegexSolver;
import com.regexsolver.api.Term;
import com.regexsolver.api.exception.ApiError;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, ApiError {
        RegexSolver.initialize(/* Your API token here -> */"");

        Term term1 = Term.Regex.of("(abc|de|fg){2,}");
        Term term2 = Term.Regex.of("de.*");
        Term term3 = Term.Regex.of(".*abc");

        Term term4 = Term.Regex.of(".+(abc|de).+");

        Term result = term1.intersection(term2, term3)
                .subtraction(term4);

        System.out.println(result);
    }
}
```
