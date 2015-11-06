set title "Queries/second vs. number of database connections using a local database"

set yrange [2000:7000]
set xrange [0:300]
plot "DBThroughput5minCons12.txt" using 1 title "200 char send" with lines linetype 1 lw 1