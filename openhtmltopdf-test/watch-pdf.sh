#!/usr/bin/env bash

PDF_FILE=$1

# Ensure file exists before watching
if [[ ! -f "$PDF_FILE" ]]; then
  echo "File not found: $PDF_FILE"
  exit 1
fi

# Watch the file with entr and open with default PDF viewer
echo "$PDF_FILE" | entr -r xdg-open "$PDF_FILE"

