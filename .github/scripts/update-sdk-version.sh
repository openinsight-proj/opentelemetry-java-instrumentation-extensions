#!/bin/bash -e

version=$1

sed -Ei "s/(opentelemetrySdk *: )\"[^\"]*\"/\1\"$version\"/" build.gradle