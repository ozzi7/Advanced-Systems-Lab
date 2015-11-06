set title "Throughput between 15 clients and middleware over time"

set yrange [0:8000]
set xrange [0:300]
set ylabel "Throughput over time [queries/s]"
set xlabel "Time [s]"

plot "ThroughputClientMW5Min4CharEcho.txt" using 1:2 title "4 char EchoRequests" with lines linetype 1 lw 1 linecolor rgb "blue"
