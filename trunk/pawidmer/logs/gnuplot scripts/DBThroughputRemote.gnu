set title "Queries per second in relation to number of database connections"

set xrange [0:18]
set yrange [0:13000]
set ylabel "Throughput [queries/second]"
set xlabel "Number of DB connections"


plot "ThroughputRemote.txt" every ::0::7 using 1:2 title "200 char send" with lines linetype 1 lw 2, \
"" every ::0::7 using 1:2:3:4 with errorbars linetype 1 notitle, \
"" every ::8::15 using 1:2 title "2000 char send" with lines linetype 2 lw 2, \
"" every ::8::15 using 1:2:3:4 with errorbars linetype 2 notitle, \
"" every ::16::23 using 1:2 title "peek" with lines linetype 3 lw 2, \
"" every ::16::23 using 1:2:3:4 with errorbars linetype 3 notitle, \
"" every ::24::31 using 1:2 title "40% send, 40% pop, 20% peek " with lines linetype 4 lw 2, \
"" every ::24::31 using 1:2:3:4 with errorbars linetype 4 notitle, \

