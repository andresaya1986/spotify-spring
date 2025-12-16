#!/usr/bin/env bash
set -euo pipefail

# Script de ayuda para migrar el proyecto a Java 21 (ejecútalo desde la raíz del repo)
# Genera: stash (si procede), crea branch, instala JDK21 en /Users/$USER/.jdk,
# configura JAVA_HOME en la sesión y sugiere cambios en pom.xml.

WORKDIR="$(pwd)"
JDK_DIR="$HOME/.jdk"
BRANCH_NAME="appmod/java-migration-java21"
MAVEN_BIN="/opt/homebrew/Cellar/maven/3.9.11/bin/mvn"

echo "Iniciando script de migración a Java 21 en: $WORKDIR"

# 1) Comprobar git
if [ ! -d ".git" ]; then
  echo "ERROR: No se detectó un repositorio git en $WORKDIR. Ejecuta este script desde la raíz del repo." >&2
  exit 1
fi

# 2) Stash si hay cambios (política: Always Stash)
if [ -n "$(git status --porcelain)" ]; then
  echo "Cambios sin commitear detectados. Aplicando stash con mensaje automático..."
  git stash push -m "Auto-stash: Save uncommitted changes before migration"
else
  echo "No hay cambios sin commitear."
fi

# 3) Crear y cambiar a la rama de trabajo
if git show-ref --verify --quiet refs/heads/$BRANCH_NAME; then
  echo "La rama $BRANCH_NAME ya existe. Cambiando a ella..."
  git switch $BRANCH_NAME
else
  echo "Creando la rama $BRANCH_NAME desde la rama actual..."
  git switch -c $BRANCH_NAME
fi

# 4) Instalar JDK 21 en $JDK_DIR si no existe
if [ -x "$JDK_DIR/bin/java" ]; then
  echo "JDK ya instalado en $JDK_DIR"
else
  echo "Instalando JDK 21 en $JDK_DIR (descarga de Adoptium, ajuste manual si lo deseas)..."
  mkdir -p "$JDK_DIR"
  TMPDIR=$(mktemp -d)
  echo "Descargando Temurin 21 (tar.gz) a $TMPDIR..."
  URL="https://github.com/adoptium/temurin21-binaries/releases/latest/download/OpenJDK21U-jdk_x64_mac_hotspot_latest.tar.gz"
  curl -L -o "$TMPDIR/temurin21.tar.gz" "$URL"
  tar -xzf "$TMPDIR/temurin21.tar.gz" -C "$TMPDIR"
  # Mover contenido extraído dentro de $JDK_DIR
  EXTRACTED_DIR=$(find "$TMPDIR" -maxdepth 1 -type d -name 'jdk-*' -print -quit || true)
  if [ -z "$EXTRACTED_DIR" ]; then
    echo "No se pudo localizar el JDK extraído en $TMPDIR. Revisa manualmente." >&2
    rm -rf "$TMPDIR"
    exit 1
  fi
  # Copiar contenidos
  cp -R "$EXTRACTED_DIR/"* "$JDK_DIR/"
  rm -rf "$TMPDIR"
  echo "JDK 21 instalado en $JDK_DIR"
fi

# 5) Configurar JAVA_HOME para la sesión actual
export JAVA_HOME="$JDK_DIR"
export PATH="$JAVA_HOME/bin:$PATH"
echo "JAVA_HOME apuntando a: $JAVA_HOME"
java -version || true

# 6) Backup del pom.xml original
if [ -f pom.xml ]; then
  cp pom.xml pom.xml.migration.bak
  echo "Backup de pom.xml creado: pom.xml.migration.bak"
else
  echo "No se encontró pom.xml en $WORKDIR" >&2
fi

# 7) Sugerencia de modificación de pom.xml
cat <<'EOF'

--- Siguientes pasos: editar pom.xml ---
Abre pom.xml y añade/ajusta las propiedades y plugin del compilador, por ejemplo:

<properties>
  <java.version>21</java.version>
  <maven.compiler.source>21</maven.compiler.source>
  <maven.compiler.target>21</maven.compiler.target>
</properties>

Y/o config del plugin maven-compiler-plugin:

<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <version>3.11.0</version>
  <configuration>
    <release>21</release>
  </configuration>
</plugin>

Si prefieres que el script intente modificar el pom automáticamente, instale 'xmlstarlet' y re-ejecuta.

EOF

# 8) Compilar sin tests para validar (usa el mvn detectado)
if [ -x "$MAVEN_BIN" ]; then
  echo "Ejecutando build (sin tests) con: $MAVEN_BIN"
  "$MAVEN_BIN" -DskipTests clean package
else
  echo "No se encontró Maven en $MAVEN_BIN. Ejecuta 'mvn -v' o ajusta la variable MAVEN_BIN en este script." >&2
fi

# 9) Ejecutar tests (opcional)
if [ -x "$MAVEN_BIN" ]; then
  echo "Ejecutando tests unitarios..."
  "$MAVEN_BIN" test || echo "Algunos tests fallaron. Revisa logs para diagnosticar." 
fi

# 10) Comandos útiles impresos para next steps
cat <<EOF

Comandos útiles:
- Ver árbol de dependencias:
  $MAVEN_BIN dependency:tree
- Escaneo de vulnerabilidades (OWASP dependency-check):
  $MAVEN_BIN org.owasp:dependency-check-maven:check
- Para revertir el stash aplicado (si corresponde):
  git stash list
  git stash pop
- Para commitear los cambios de migración:
  git add .
  git commit -m "Code migration to Java 21: update pom and build settings"
  git push -u origin $BRANCH_NAME

EOF

echo "Script finalizado. Revisa la salida para errores y edita pom.xml según se indicó."
exit 0
