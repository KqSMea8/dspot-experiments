#!/usr/bin/env bash

project=$1

python src/main/python/octobre-2017/mutations_analysis.py ${project} > src/main/bash/run-mutant-original.sh
python src/main/python/octobre-2017/amplification.py ${project} > src/main/bash/amplification.sh
python src/main/python/octobre-2017/mutations_analysis.py ${project} amplified > src/main/bash/run-mutant-original.sh