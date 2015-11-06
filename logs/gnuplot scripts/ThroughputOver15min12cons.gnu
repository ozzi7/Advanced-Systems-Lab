set title "Throughput over time using 12 DB connections"

set xrange [0:800]
set yrange [0:8000]
set ylabel "Throughput over time [queries/s]"
set xlabel "Time [s]"


plot "Throughput_Type1_Cons12.txt" title "200 char send" with lines linetype 1 lw 0.8 linecolor 6
