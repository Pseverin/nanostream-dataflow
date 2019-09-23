#!/bin/bash
set -e

# set environment variables
export NAME="bwa-resistance-mm2"
export REGION="asia-northeast1"
export ZONE="${REGION}-c"
export MACHINE_TYPE="n1-highmem-4"
export MIN_REPLICAS=1
export MAX_REPLICAS=3
export TARGET_CPU_UTILIZATION=0.5

export DOCKER_IMAGE='dockersubtest/nano-gcp-http'
export BWA_FILES='gs://nano-stream1/Databases/resFinder/*'

# REQUESTER_PROJECT - project billed for downloading BWA_FILES
# this line set it value to the active project ID
export REQUESTER_PROJECT=$(gcloud config get-value project)

source provision.sh

[[ $1 = '-c' ]] && cleanup || setup
