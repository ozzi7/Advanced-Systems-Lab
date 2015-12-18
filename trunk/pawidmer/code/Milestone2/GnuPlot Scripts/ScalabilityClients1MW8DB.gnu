reset
set title "Response Time Vs. Arrival Rate using different Models" font ",12"

set key font ",11"
set xrange [0:1500]
set yrange [0:50]
set ytics 5 font ",11"
set xtics 100 font ",11"

set ylabel "Avg. Response Time [ms]" font ",12"
set xlabel "Arrival Rate [queries/s]" font ",12"
set key left top

set object circle at 445,4.21 size 10.0 fc rgb "red" 
set object circle at 896,7.54 size 10.0 fc rgb "red"
set object circle at 1097,7.52 size 10.0 fc rgb "red"

set object circle at 498,3.9 size 10.0 fc rgb "blue" 
set object circle at 935,6.86 size 10.0 fc rgb "blue"
set object circle at 1099,2.8 size 10.0 fc rgb "blue"


fac(x) = gamma(x+1)
mu =180.0
servicetime = 0.005565
p(x) = x/(8*mu)
p08(x) = 1/(1+((8*p(x))**8)/(fac(8)*(1-p(x)))+(8*p(x))+((8*p(x))**2)/(fac(2))+((8*p(x))**3)/(fac(3))+((8*p(x))**4)/(fac(4))+((8*p(x))**5)/(fac(5))+((8*p(x))**6)/(fac(6))+((8*p(x))**7)/(fac(7)))
gr(x) = ((8*p(x))**8)/(fac(8)*(1-p(x)))*p08(x)

f1(x) = (1/(180-x/8))*1000.0
f2(x) = ((1/mu)*1000)*(1+gr(x)/(8.0*(1-p(x))))

plot f1(x) title "8 Parallel M/M/1 Queues" with lines linetype 1 lw 2, \
f2(x) title "Single M/M/8 Queue" with lines linetype 2 lw 2 

