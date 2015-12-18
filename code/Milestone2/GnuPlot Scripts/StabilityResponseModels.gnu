reset
set title "Response Time Vs. Arrival Rate using Different Models" font ",12"

set key font ",10"
set xrange [0:2000]
set yrange [0:50]
set ytics 5 nomirror tc default
set ylabel "Avg. Response Time [ms]" font ",12"
set xlabel "Arrival Rate [requests/s]" font ",12"
set key left top

set object circle at 1088,4.979 size 10.0 fc rgb "red" 

fac(x) = gamma(x+1)
mu =209.0
servicetime = 0.00478

p(x) = x/(8*mu)
p08(x) = 1/(1+((8*p(x))**8)/(fac(8)*(1-p(x)))+(8*p(x))+((8*p(x))**2)/(fac(2))+((8*p(x))**3)/(fac(3))+((8*p(x))**4)/(fac(4))+((8*p(x))**5)/(fac(5))+((8*p(x))**6)/(fac(6))+((8*p(x))**7)/(fac(7)))
gr(x) = ((8*p(x))**8)/(fac(8)*(1-p(x)))*p08(x)

f1(x) = (1/(mu-x/8))*1000
f2(x) = ((1/mu)*1000)*(1+gr(x)/(8.0*(1-p(x))))
f3(x) = 1000*(1/(mu*8))/(1-x/(mu*8))

plot f1(x) title "8 Parallel M/M/1 Queues" with lines linetype 1 lw 2, f2(x) title "M/M/8 Queue" with lines linetype 2 lw 2,f3(x) title "Single M/M/1 Queue" with lines linetype 3 lw 2
