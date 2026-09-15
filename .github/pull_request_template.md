## What this changes

<!-- And why. Link the issue it closes, if there is one. -->

## Checklist

- [ ] `mvn -B package` passes
- [ ] New public items carry a Javadoc comment, and anything user-facing is in the README
- [ ] A test covers the change
- [ ] `CHANGELOG.md` is updated under `## [Unreleased]`
- [ ] The change runs on Java 11, the version the compiler plugin's `<release>` declares, or the bump is intentional and noted
- [ ] `src/main/java/com/regexsolver/api/generated/` is only touched by `./generate-api.sh`, once the change is live in the API's published specification
