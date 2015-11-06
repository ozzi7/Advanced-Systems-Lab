set title "Response time over time using 12 DB connections"

set xrange [0:120]
set yrange [-1.5:2]
set ylabel "Response time [ms]"
set xlabel "Time [s]"


plot "ResponseTime2Min.txt" every ::2::118 using 1:2 title "200 char send" with lines linetype 1 linecolor 18  lw 2, \
"" every ::2::118 using 1:2:3:4 with errorbars linetype 1 linecolor 18 notitle