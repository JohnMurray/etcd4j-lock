#!/bin/bash

if [ ! "$ROOT_CHECK" = "0" ]; then
   if [ "$(id -u)" != "0" ]; then
       echo "${red}${bold}ERROR: This must be run as root (sudo)!${reset}"
       exit 1
   else
       true "INFO: Script running as root."
   fi
fi

USERNAME="$(logname)"

WHONIX_LOCAL_SIGNING_KEY_FOLDER="/home/$USERNAME"

sudo -E -u "$USERNAME" mkdir --parents "$WHONIX_LOCAL_SIGNING_KEY_FOLDER"

## http://www.gnupg.org/documentation/manuals/gnupg-devel/Unattended-GPG-key-generation.html

echo "
   Key-Type: RSA
   Key-Length: 4096
   Subkey-Type: RSA
   Subkey-Length: 4096
   Name-Real: TravisCI Test Key - etcd4j-lock
   Name-Email: etcd4j-lock@travisci.org
   Expire-Date: 0
   Preferences: SHA512 SHA384 SHA256 SHA224 AES256 AES192 AES CAST5 ZLIB BZIP2 ZIP Uncompressed
" | sudo -E -u "$USERNAME" gpg \
--batch \
--gen-key
