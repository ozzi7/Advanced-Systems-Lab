set title "Queries/second vs. number of database connections using a local database"

set xrange [0:21]
set yrange [2000:20000]
set ylabel "Throughput [queries/second]"
set xlabel "Number of DB connections"

set key right top

plot "ThroughputLocal.txt" every ::0::8 using 1:2 title "200 char send" with lines linetype 1 lw 2, \
"" every ::0::8 using 1:2:3:4 with errorbars linetype 1 notitle, \
"" every ::9::17 using 1:2 title "2000 char send" with lines linetype 2 lw 2, \
"" every ::9::17 using 1:2:3:4 with errorbars linetype 2 notitle, \
"" every ::18::26 using 1:2 title "peek" with lines linetype 3 lw 2, \
"" every ::18::26 using 1:2:3:4 with errorbars linetype 3 notitle, \
"" every ::27::35 using 1:2 title "40% send, 40% pop, 20% peek " with lines linetype 4 lw 2, \
"" every ::27::35 using 1:2:3:4 with errorbars linetype 4 notitle, \


