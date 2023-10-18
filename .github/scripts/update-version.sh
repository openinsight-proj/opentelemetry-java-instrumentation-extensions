#!/bin/bash -e

version=$1

if [[ $version == *-SNAPSHOT ]]; then
  alpha_version=${version//-SNAPSHOT/-alpha-SNAPSHOT}
else
  alpha_version=${version}-alpha
fi

sed -Ei "s/(opentelemetryJavaagent *: )\"[^\"]*\"/\1\"$version\"/" build.gradle
sed -Ei "s/(opentelemetryJavaagentAlpha *: )\"[^\"]*\"/\1\"$alpha_version\"/" build.gradle

sed -Ei "s/(io.opentelemetry.instrumentation.muzzle-generation\" version )\"[^\"]*\"/\1\"$alpha_version\"/" build.gradle
sed -Ei "s/(io.opentelemetry.instrumentation.muzzle-check\" version )\"[^\"]*\"/\1\"$alpha_version\"/" build.gradle
