reset
set title "Throughput over Time (30ms think time)" font ",12"

set key font ",10"
set xrange [0:1800]
set yrange [1060:1120]
set ytics 5 nomirror tc default
set xtics 100 nomirror tc default
set ylabel "Throughput [req/s]" font ",12"
set xlabel "Time [s]" font ",12"
set key right top

plot "StabilityThroughput32C.txt" using 1:2 with lines linetype 1 lw 1 title "Stability Trace"
