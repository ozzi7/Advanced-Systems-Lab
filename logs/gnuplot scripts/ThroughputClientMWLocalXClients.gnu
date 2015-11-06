set title "Throughput client middleware on local machine"

set xrange [0:56]
set yrange [7000:10000]
set ylabel "Throughput [queries/second]"
set xlabel "Time [s]"

set key right top

plot "ThroughputClientMWLocalXClients.txt" every ::0::55 using 1:2 title "5 clients" with lines linetype 1 lw 2, \
"" every ::56::111 using 1:2 title "15 clients" with lines linetype 2 lw 2, \
"" every ::112::167 using 1:2 title "30 clients" with lines linetype 3 lw 2
