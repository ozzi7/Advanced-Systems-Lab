set title "Throughput between 30 clients and 2 middleware over 30 minutes"

set yrange [0:7000]
set xrange [0:1800]
set ylabel "Throughput [queries/s]"
set xlabel "Time [s]"

plot "ThroughputClientMW30MinMixedLoad.txt" using 1:2 title "50% send, 45% peek, 2% queryQueues, 3% popQueue" with lines linetype 1 lw 1 linecolor rgb "blue"