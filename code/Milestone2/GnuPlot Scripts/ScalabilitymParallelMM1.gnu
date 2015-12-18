set title "Response Time Vs. Arrival Rate using m Parallel M/M/1 Queues" font ",12"

set key font ",10"
set xrange [0:1500]
set yrange [0:100]
set ytics 5
set ylabel "Avg. Response Time [ms]" font ",12"
set xlabel "Arrival Rate [queries/s]" font ",12"
set key left top

f1(x) = (1/(288-x/4))*1000
f2(x) = (1/(180-x/8))*1000
f3(x) = (1/(182-x/8))*1000
f4(x) = (1/(94-x/16))*1000
plot f1(x) title "1 Middleware/4 DB Connections" with lines linetype 1 lw 2,\
f2(x) title "1 Middleware/8 DB Connections" with lines linetype 2 lw 2,\
f3(x) title "2 Middleware/4 DB Connections each" with lines linetype 3 lw 2, \
f4(x) title "2 Middleware/8 DB Connections each" with lines linetype 4 lw 2


