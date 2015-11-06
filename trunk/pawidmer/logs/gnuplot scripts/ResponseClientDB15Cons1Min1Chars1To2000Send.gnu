set title "Response time between 15 clients and the database using different message sizes"

set xrange [0:60]
set yrange [0.1:100]
set ylabel "Response Time [ms]"
set xlabel "Time [s]"
set logscale y
set key right top
set key opaque

plot "ResponseClientDB15Cons1Min1Chars1To2000Send.txt" every ::0::80 using 1:2 title "1 char send" with lines linetype 1 lw 2, \
"" every ::81::161 using 1:2 title "10 char send" with lines linetype 2 lw 2, \
"" every ::162::242 using 1:2 title "100 char send" with lines linetype 3 lw 2, \
"" every ::243::323 using 1:2 title "500 char send" with lines linetype 4 lw 2, \
"" every ::324::404 using 1:2 title "1200 char send" with lines linetype 5 lw 2, \
"" every ::405::485 using 1:2 title "2000 char send" with lines linetype 6 lw 2

unset logscale y


