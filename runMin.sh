#!/bin/bash

javac lpmaker/TestRunMin.java

for topo in 1 2 3
    do
    for failurePos in 0 1 2
        do
        for failCount in 0 1 3 5 10 20
            do
            for trial in 1 2 3 4
                do
                java lpmaker.TestRunMin "$topo" "$failurePos" "$failCount" "$trial" &
            done
        done
    done
done
wait

for entry in ./*.lp
    do
    echo "$entry"
    gurobi_cl Threads=10 Method=2 Crossover=0 ResultFile=$entry'.sol' $entry &
done
wait
rm ./*.lp

for entry in ./*.lp.sol
    do
    echo "$entry"
    line=$(head -n 1 "$entry")
    echo -e "$entry $line" >> "finalMin.txt"
done
rm ./*.lp.sol
