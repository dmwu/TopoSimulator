#!/bin/bash

for entry in ./*.lp
do
  echo "$entry"
  gurobi_cl Threads=10 Crossover=0 ResultFile=$entry'.sol' $entry
done

for entry in ./*.sol
do
  echo "$entry"
  line=$(head -n 1 $entry)
  echo -e "$entry $line" >> "final.sol"
done
