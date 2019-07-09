GPG_EXECUTABLE  ?= gpg
GPG_SECRET_KEYS ?=
GPG_OWNERTRUST  ?=
VERSION         ?=

# Functions
info := @printf "\033[32;01m%s\033[0m\n"

define help
Usage: make <command>
Commands:
  help:          Show this help information
  import-keys:   Importing PGP keys to sign the maven artifact
                 Environment variables:
                  - GPG_SECRET_KEYS (secret key to sign the maven artifact)
                  - GPG_OWNERTRUST (trust level for the secret key)
  set-version:   Update pom.xml for almaraz and almaraz example with new version
                 Environment variables:
                  - VERSION (new version of almaraz library)
  build:         Build almaraz
  build-example: Build almaraz example
  publish:       Publish the library in the maven central repository
                 Environment variables:
                  - SONATYPE_USER
				  - SONATYPE_PASSWORD
endef
export help

.PHONY: help import-keys set-version build

help:
	@echo "$$help"

check-%:
	@if [ -z '${${*}}' ]; then echo 'Environment variable $* not set' && exit 1; fi

import-keys: check-GPG_SECRET_KEYS check-GPG_OWNERTRUST
	$(info) "Importing private key"
	@echo $(GPG_SECRET_KEYS) | base64 --decode | $(GPG_EXECUTABLE) --import --no-tty --batch --yes
	$(info) "Importing trust level of the key"
	@echo $(GPG_OWNERTRUST) | base64 --decode | $(GPG_EXECUTABLE) --import-ownertrust

set-version: check-VERSION
	$(info) "Setting new version $(VERSION) for almaraz"
	mvn versions:set -DnewVersion=$(VERSION) -DgenerateBackupPoms=false
	$(info) "Setting new version $(VERSION) for almaraz example"
	cd example && \
		mvn versions:set -DnewVersion=$(VERSION) -DgenerateBackupPoms=false && \
		mvn versions:use-dep-version -DdepVersion=$(VERSION) -Dincludes=com.elevenpaths.almaraz:almaraz \
				-DforceVersion=true -DgenerateBackupPoms=false

build:
	$(info) "Building almaraz"
	mvn install --settings .circleci/settings.xml

build-example:
	$(info) "Building almaraz example"
	cd example && mvn install

publish: check-SONATYPE_USER check-SONATYPE_PASSWORD
	$(info) "Publishing almaraz"
	mvn deploy --settings .circleci/settings.xml
