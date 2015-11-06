set title "Response time using 30 clients and 2 middleware over 30 minutes"

set xrange [0:1800]
set yrange [0.1:7]
set ylabel "Response Time [ms]"
set xlabel "Time [s]"
set key right top
set key inside

plot "ResponseClientDB12Cons30Clients30MinStability.txt" using 1:2 title "50% send, 45% peek, 2% queryQueues, 3% popQueue" with lines linetype 1 lw 1




