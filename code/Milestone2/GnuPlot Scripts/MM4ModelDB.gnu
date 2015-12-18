reset
set title "Response Time Vs. Arrival Rate using a M/M/4 Model for the Database" font ",12"

set key font ",10"
set xrange [0:1800]
set yrange [0:10]
set y2range [0:1]

set ytics 1 nomirror tc default 
set y2tics 0.1 nomirror tc default
set xtics 100 nomirror tc default

set ylabel "Avg. Response/Waiting Time [ms]" font ",12" tc default
set y2label "Traffic Intensity/Probability" font ",12" tc default

set xlabel "Arrival Rate [queries/s]" font ",12"
set key left top

set object circle at 364,2.37 size 10.0 fc rgb "red" 
set object circle at 655,2.37 size 10.0 fc rgb "red" 
set object circle at 1165,2.96 size 10.0 fc rgb "red" 

fac(x) = gamma(x+1)
mu =446.0

p(x) = x/(4.0*mu)
p0(x) = 1/(1+((4*p(x))**4)/(fac(4)*(1-p(x)))+(4*p(x))+((4*p(x))**2)/(fac(2))+((4*p(x))**3)/(fac(3)))
gr(x) = ((4*p(x))**4)/(fac(4)*(1-p(x)))*p0(x)
r2(x) = ((1/mu)*1000.0)*(1+gr(x)/(4.0*(1.0-p(x))))
wait(x) = 1000*gr(x)/(4*mu*(1-p(x)))

set samples 5000
plot p(x) title "Traffic Intensity" with lines linetype 1 lw 2 axes x1y2, \
gr(x) title "Probability All Servers Busy" with lines linetype 2 lw 2 axes x1y2, \
r2(x) title "Average Response Time" with lines linetype 3 lw 2 axes x1y1, \
wait(x) title "Average Waiting Time" with lines linetype 4 lw 2 axes x1y1