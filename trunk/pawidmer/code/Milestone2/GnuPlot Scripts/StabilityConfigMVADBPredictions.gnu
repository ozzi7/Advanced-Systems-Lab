reset
set title "Throughput Predictions" font ",12"

set key font ",10"
set xrange [0:1000]
set yrange [0:16000]
set ytics 1000 nomirror tc default
set xtics 50 nomirror tc default
set ylabel "Throughput[req/s]" font ",12"
set xlabel "Number of Clients (30ms Think Time)" font ",12"
set key left top

plot "MVA_Clients1_1000_ThinkTime30.txt" using 1:3 with lines linetype 1 lw 2 title "Initial Database", \
"MVA_Clients1_1000_ThinkTime30Prediction4xDB.txt" using 1:3 with lines linetype 2 lw 2 title  "4x Faster Database", \
"MVA_Clients1_1000_ThinkTime30Prediction8xDB.txt" using 1:3 with lines linetype 3 lw 2 title  "8x Faster Database"

