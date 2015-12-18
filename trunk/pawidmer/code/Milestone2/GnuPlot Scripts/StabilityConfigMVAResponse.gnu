reset
set title "Response Time vs. Number of Clients" font ",12"

set key font ",10"
set xrange [0:300]
set yrange [0:150]
set ytics 5 nomirror tc default
set xtics 10 nomirror tc default
set ylabel "Avg. Response Time [ms]" font ",12"
set xlabel "Number of Clients (30ms Think Time)" font ",12"
set key left top

plot "MVA_Clients1_1000_ThinkTime30.txt" using 1:($2*1000) with lines linetype 1 lw 2 title "MVA Analysis", \
"ResponseMVA.txt" using 1:2:3:4 with errorbars linetype 2 lw 2 title "Experiment" 
