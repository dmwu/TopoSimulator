#!/bin/bash

javac lpmaker/TestRunMin.java
java lpmaker.TestRunMin

for entry in ./*.lp
do
  echo "$entry"
  gurobi_cl Threads=10 Method=2 Crossover=0 ResultFile=$entry'.sol' $entry
done

for entry in ./*.sol
do
  echo "$entry"
  line=$(head -n 1 $entry)
  echo -e "$entry $line" >> "finalMin.sol"
done
rm ./*.lp
