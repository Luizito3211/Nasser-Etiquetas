#!/usr/bin/env bash
DIR="$( cd "$( dirname "$0" )" && pwd )"
if [ -x "$DIR/.maven/apache-maven-3.9.9/bin/mvn" ]; then
	exec "$DIR/.maven/apache-maven-3.9.9/bin/mvn" "$@"
fi

if command -v mvn >/dev/null 2>&1; then
	exec mvn "$@"
fi

echo "ERRO: Maven nao foi encontrado. Execute setup.bat ou instale o Maven." >&2
exit 1
