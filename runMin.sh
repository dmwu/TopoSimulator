#!/bin/bash

javac lpmaker/TestRunMin.java
rm ./*.lp

for trafficMode in 0 2 11 15
    do
    for topo in 0 1 2 3 4
        do
        java lpmaker.TestRunMin "$topo" "$trafficMode" 0 0 0
        for entry in ./*.lp
            do
            echo "$entry"
            gurobi_cl Threads=12 Method=2 Crossover=0 ResultFile=$entry'.sol' $entry
        done
        rm ./*.lp

        for failureMode in 0 1 2 3
            do
            for failCount in 1 3 5 10 20
                do
                for trial in 1 2 3 4 5 6
                    do
                    java lpmaker.TestRunMin "$topo" "$trafficMode" "$failureMode" "$failCount" "$trial" &
                done
                wait
                for entry in ./*.lp
                    do
                    echo "$entry"
                    gurobi_cl Threads=12 Method=2 Crossover=0 ResultFile=$entry'.sol' $entry
                done
                rm ./*.lp
            done
        done
    done

    for entry in ./*.lp.sol
        do
        echo "$entry"
        line=$(head -n 1 "$entry")
        echo -e "${entry} ${line}" >> "More${trafficMode}.txt"
    done
    rm ./*.lp.sol
done
#wait


