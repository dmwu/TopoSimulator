#!/bin/bash

for entry in ./*.lp
do
  echo "$entry"
  gurobi_cl Threads=8 ResultFile=$entry'.sol' $entry | tee $entry".out"
done
