#!/bin/bash

# The API serves its own specification, which is the source the SDK is generated
# from. Pass a path or another URL as the first argument to generate against it.
SPEC="${1:-https://api.regexsolver.com/openapi.json}"
OUT_DIR="./"

echo "Running openapi-generator-cli..."
openapi-generator-cli generate \
  -i "$SPEC" \
  -g java \
  -o "$OUT_DIR" \
  --model-name-suffix Dto \
  --additional-properties=library=native \
  --additional-properties=invokerPackage=com.regexsolver.api.generated \
  --additional-properties=apiPackage=com.regexsolver.api.generated.api \
  --additional-properties=modelPackage=com.regexsolver.api.generated.model \
  --additional-properties=asyncNative=true \
  --additional-properties=useRuntimeException=true \
  --additional-properties=openApiNullable=false \
  --additional-properties=useJakartaEe=true

echo "API Generation Complete."
