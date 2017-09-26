#!/bin/bash

for entry in ./*.lp
do
  echo "$entry"
  gurobi_cl Threads=8 ResultFile=$entry'.sol' $entry | tee $entry".out"
done

for entry in ./*.sol
do
  echo "$entry"
  line=$(head -n 1 filename)
  echo -e "$entry $line" >> "final.sol"
done
