set title "Response time between 15 clients and middleware using 4 char EchoRequest messages"

set xrange [0:300]
set yrange [-1.5:2]
set ylabel "Response time [ms]"
set xlabel "Time [s]"
set key inside

plot "ResponseClientMW15Cons5Min4CharEcho.txt" using 1:2 title "4 char EchoRequests" with lines linetype 1 linecolor 18 lw 1, \
"" using 1:2:3:4 with errorbars linetype 1 linecolor 18 notitle
