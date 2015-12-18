reset
set title "Throughput vs. Number of Clients" font ",12"

set key font ",10"
set xrange [0:300]
set yrange [0:2200]
set ytics 100 nomirror tc default
set xtics 10 nomirror tc default
set ylabel "Throughput[req/s]" font ",12"
set xlabel "Number of Clients (30ms Think Time)" font ",12"
set key left top

plot "MVA_Clients1_1000_ThinkTime30.txt" using 1:3 with lines linetype 1 lw 2 title "MVA Analysis", \
"ThroughputMVA.txt" using 1:2:3:4 with errorbars linetype 2 lw 2 title "Experiment" 
