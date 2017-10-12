#!/bin/bash

javac lpmaker/TestRunAvg.java

for topo in 1 2 3
    do
    for failCount in 0 1 2 3 5 10
        do
        for trial in 1 2 3 4 5
            do
                java lpmaker.TestRunAvg "$topo" "$failCount" "$trial"

            for entry in ./*.lp
            do
                echo "$entry"
                gurobi_cl Threads=10 Method=2 Crossover=0 ResultFile=$entry'.sol' $entry
            done

            for entry in ./*.sol
            do
            echo "$entry"
            line=$(head -n 1 $entry)
            echo -e "$entry $line" >> "finalAvgFine.sol"
            done
            rm ./*.lp
            rm ./*.lp.sol
        done
    done
done