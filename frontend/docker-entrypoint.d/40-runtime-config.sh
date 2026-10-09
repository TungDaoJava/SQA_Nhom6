#!/bin/sh
set -eu

config_file=/usr/share/nginx/html/runtime-config.js
api_base_url="${VITE_API_BASE_URL:-/api/v1}"
escaped_api_base_url=$(printf '%s' "$api_base_url" | sed 's/[\\&|]/\\&/g')

sed "s|__VITE_API_BASE_URL__|${escaped_api_base_url}|g" "$config_file" > "${config_file}.tmp"
mv "${config_file}.tmp" "$config_file"
