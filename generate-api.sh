#!/bin/bash

SPEC_FILE="../m-lab/shared/openapi.yaml"
OUT_DIR="./"

echo "Running openapi-generator-cli..."
openapi-generator-cli generate \
  -i "$SPEC_FILE" \
  -g java \
  -o "$OUT_DIR" \
  --model-name-suffix Dto \
  --additional-properties=library=native \
  --additional-properties=invokerPackage=com.regexsolver.api.generated \
  --additional-properties=apiPackage=com.regexsolver.api.generated.api \
  --additional-properties=modelPackage=com.regexsolver.api.generated.model \
  --additional-properties=asyncNative=true \
  --additional-properties=useRuntimeException=true \
  --additional-properties=openApiNullable=false

echo "API Generation Complete."
