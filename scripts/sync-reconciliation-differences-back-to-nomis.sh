#!/usr/bin/env bash
# ------------------------------------------------------------------
# Sync physical attribute reconciliation differences back to NOMIS
#
#         This script accepts a file containing a list of prisoner 
#         numbers that were flagged in a reconciliation job as 
#         having differing physical attributes (e.g. height and
#         weight) in NOMIS to what is calculated and stored by the 
#         hmpps-prison-person-api.
#
#         The reason this difference exists is because there are
#         some prisoner records where the latest booking has no
#         physical attributes record - it wasn't copied across from
#         the previous record.
#
#         This script should be run manually to correct the data in
#         NOMIS, creating a physical attributes record on the latest
#         booking for each prisoner in the list, copying the latest
#         data that hmpps-prison-person-api holds.
#
#         The script requires, in addition to the list of prisoner 
#         numbers, client credentials supplied via environment
#         variables to request a token from HMPPS Auth with the 
#         `ROLE_NOMIS_PRISON_PERSON__RECONCILIATION` role.
#
#         There is an example file for the prisoner numbers:
#         prisoner-numbers-example.sh.  It is recommended that a
#         copy is made to prisoner-numbers.sh and is NOT checked 
#         into source code.
#         
# ------------------------------------------------------------------

# Set via env vars for auth
#ENV=(dev/preprod/prod)
#CLIENTID=(client id with ROLE_NOMIS_PRISON_PERSON__RECONCILIATION)
#CLIENTSECRET=

VERSION=0.1.0
SUBJECT=sync-reconciliation-differences-back-to-nomis
TOKEN_REFRESH_RATE=100 # Retrieve a new token after this number of requests
DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)
. "$DIR"/token-functions.sh

# --- BASH version check -------------------------------------------
set -e
if ! echo "$BASH_VERSION" | grep -E "^[45]" &>/dev/null; then
  echo "Found bash version: $BASH_VERSION"
  echo "Ensure you are using bash version 4 or 5"
  exit 1
fi

# --- Options processing -------------------------------------------
usage() {
  echo "$0 usage: "
  echo "Ensure ENV, CLIENTID and CLIENTSECRET are set via environment variables."
  echo "Then supply options:" && grep " .)\ \#" "$0"
  exit 1
}

enforce_var_set() {
  if [[ ! -v $1 ]]; then
    echo "$1 environment variable not set."
    exit 1
  fi
}

enforce_var_set ENV
enforce_var_set CLIENTID
enforce_var_set CLIENTSECRET

if [ $# == 0 ] ; then
  usage
  exit 1;
fi

while getopts ":f:vh" optname
  do
    case "$optname" in
      f) # file name containing prisoner numbers to sync back to NOMIS
        echo "-f prisoner numbers file: $OPTARG"
        PRISONER_NUMBERS_FILE=$OPTARG 
        ;;
      v) # print script version 
        echo "Version $VERSION"
        exit 0;
        ;;
      h) # print usage
        usage
        exit 0;
        ;;
      ?)
        echo "Unknown option $OPTARG"
        exit 0;
        ;;
      :)
        echo "No argument value for option $OPTARG"
        exit 0;
        ;;
      *)
        echo "Unknown error while processing options"
        exit 0;
        ;;
    esac
  done

shift $(($OPTIND - 1))

# --- Body --------------------------------------------------------
echo "---------------"

authenticated_request() {
  http --check-status "$@" "$AUTH_TOKEN_HEADER"
}

prisonerToNomisUpdateHostname() {
  local ENV=$1
  # Set the environment-specific hostname for the oauth2 service
  if [[ "$ENV" == "dev" ]]; then
    echo "https://prisoner-to-nomis-update-dev.hmpps.service.justice.gov.uk"
  elif [[ "$ENV" == "preprod" ]]; then
    echo "https://prisoner-to-nomis-update-preprod.hmpps.service.justice.gov.uk"
  elif [[ "$ENV" == "prod" ]]; then
    echo "https://prisoner-to-nomis-update.hmpps.service.justice.gov.uk"
  elif [[ "$ENV" =~ localhost* ]]; then
    echo "http://$ENV"
  fi
}

echo "Checking prisoner numbers file exists..."
checkFile $PRISONER_NUMBERS_FILE

echo "Getting token from HMPPS Auth..."
PRISONER_TO_NOMIS_UPDATE_HOST=$(prisonerToNomisUpdateHostname "$ENV")
echo "Determined host: $PRISONER_TO_NOMIS_UPDATE_HOST"

echo "Loading prisoner numbers from file..."
. "$PRISONER_NUMBERS_FILE" 

numberOfSyncs=${#prisonerNumbers[@]}
echo "Syncing $numberOfSyncs prisoners..."
for (( i=0; i<numberOfSyncs; i++ ));
do
  if [[ $((i % TOKEN_REFRESH_RATE)) -eq 0 ]]; then
    echo "Refreshing auth token..."
    CLIENT="$CLIENTID:$CLIENTSECRET"
    HMPPS_AUTH_HOST=$(calculateHmppsAuthHostname "$ENV")
    AUTH_TOKEN_HEADER=$(authenticate "$CLIENT")
  fi

  echo "Syncing prisoner $((i + 1)) of $numberOfSyncs: ${prisonerNumbers[$i]}..."
  authenticated_request PUT "$PRISONER_TO_NOMIS_UPDATE_HOST/prisonperson/${prisonerNumbers[$i]}/physical-attributes"
done

echo "Sync request sent for all prisoner numbers."
#-----------------------------------------------------------------
