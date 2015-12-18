reset
set title "Waiting Time Vs. Arrival Rate using Different Models" font ",12"

set key font ",10"
set xrange [0:2000]
set yrange [0:50]
set ytics 5 nomirror tc default
set ylabel "Avg. Waiting Time [ms]" font ",12"
set xlabel "Arrival Rate [requests/s]" font ",12"
set key left top

set object circle at 1088,0.2 size 10.0 fc rgb "red" front

f1(x) = 1000*((x/8.0)/209.0)*(1/209.0)/(1-(x/8.0)/209.0)
f2(x) = 1000*(x/1672.0)*(1/1672.0)/(1-(x/1672.0))
plot f1(x) title "8 Parallel M/M/1 Queues" with lines linetype 1 lw 2, f2(x) title "Single M/M/1 Queue" with lines linetype 2 lw 2
