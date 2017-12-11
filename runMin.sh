#!/bin/bash

javac lpmaker/TestRunMin.java
rm ./*.lp

for trafficMode in 0 11 13 15
    do
    for topo in 1 2 3
        do
        java lpmaker.TestRunMin "$topo" "$trafficMode" 0 0 0
        for entry in ./*.lp
            do
            echo "$entry"
            gurobi_cl Threads=12 Method=2 Crossover=0 ResultFile=$entry'.sol' $entry
        done
        rm ./*.lp

        for failurePos in 1 2
            do
            for failCount in 1 3 5 10 20
                do
                for trial in 1 2 3
                    do
                    java lpmaker.TestRunMin "$topo" "$trafficMode" "$failurePos" "$failCount" "$trial" &
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
        echo -e "${entry} ${line}" >> "finalMin${trafficMode}.txt"
    done
    rm ./*.lp.sol
done
#wait


