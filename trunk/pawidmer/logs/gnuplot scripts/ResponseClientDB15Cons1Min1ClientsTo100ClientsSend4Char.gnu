set title "Response time between 15 clients and the database using different number of clients"

set xrange [0:68]
set yrange [0.1:10]
set ylabel "Response Time [ms]"
set xlabel "Time [s]"
set key right top
set logscale y
set key outside

plot "ResponseClientDB15Cons1Min1ClientsTo100ClientsSend4Char.txt" every ::0::68 using ($1-10):2 title "1 client" with lines linetype 1 lw 2, \
"" every ::69::137 using ($1-10):2  title "2 clients" with lines linetype 2 lw 2, \
"" every ::138::206 using ($1-10):2  title "5 clients" with lines linetype 3 lw 2, \
"" every ::207::275 using ($1-10):2  title "10 clients" with lines linetype 4 lw 2, \
"" every ::276::344 using ($1-10):2  title "20 clients" with lines linetype 5 lw 2, \
"" every ::345::413 using ($1-10):2  title "50 clients" with lines linetype 6 lw 2, \
"" every ::414::482 using ($1-10):2  title "100 clients" with lines linetype 7 lw 2

unset logscale y





