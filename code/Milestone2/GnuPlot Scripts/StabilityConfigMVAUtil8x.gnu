reset
set title "Utilization of System Components using 8x faster DB" font ",12"

set key font ",10"
set xrange [0:1000]
set yrange [0:1]
set ytics 0.1 nomirror tc default
set xtics 100 nomirror tc default
set ylabel "Utilization" font ",12"
set xlabel "Number of Clients (30ms Think Time)" font ",12"
set key left top

plot "MVA_Clients1_1000_ThinkTime30Prediction8xDB.txt" using 1:4 with lines linetype 1 lw 2 title "Network Client-Middleware", \
"MVA_Clients1_1000_ThinkTime30Prediction8xDB.txt" using 1:5 with lines linetype 2 lw 2 title "Client Service Thread", \
"MVA_Clients1_1000_ThinkTime30Prediction8xDB.txt" using 1:6 with lines linetype 11 lw 2 title "DB Thread", \
"MVA_Clients1_1000_ThinkTime30Prediction8xDB.txt" using 1:7 with lines linetype 4 lw 2 title "Network Middleware-DB", \
"MVA_Clients1_1000_ThinkTime30Prediction8xDB.txt" using 1:8 with lines linetype 6 lw 2 title "Database"
