reset
set title "M/M/4 Model for the Middleware" font ",12"

set key font ",10"
set xrange [0:50000]
set yrange [0:2]
set y2range [0:1]

set ytics 0.1 nomirror tc default 
set y2tics 0.1 nomirror tc default
set xtics 5000 nomirror tc default
set ylabel "Avg. Response/Waiting Time [ms]" font ",12" tc default
set y2label "Traffic Intensity/Probability" font ",12" tc default

set xlabel "Arrival Rate [queries/s]" font ",12"
set key left top

set object circle at 39841,0.1557 size 300.0 fc rgb "red" 
set object circle at 32685,0.1482 size 300.0 fc rgb "red" 
set object circle at 20054,0.1442 size 300.0 fc rgb "red" 

fac(x) = gamma(x+1)
mu =12500.0
servicetime = 0.00008
p(x) = x/(4.0*mu)
p04(x) = 1/(1+((4*p(x))**4)/(fac(4)*(1-p(x)))+(4*p(x))+((4*p(x))**2)/(fac(2))+((4*p(x))**3)/(fac(3)))
gr(x) = ((4.0*p(x))**4.0)/(fac(4)*(1.0-p(x)))*p04(x)
r2(x) = ((1/mu)*1000)*(1+gr(x)/(4*(1-p(x))))
wait(x) = 1000*gr(x)/(4*mu*(1-p(x)))

set samples 10000
plot p(x) title "Traffic Intensity" with lines linetype 1 lw 2 axes x1y2, \
gr(x) title "Probability All Servers Busy" with lines linetype 2 lw 2 axes x1y2, \
r2(x) title "Average Response Time" with lines linetype 3 lw 2 axes x1y1, \
wait(x) title "Average Waiting Time" with lines linetype 4 lw 2 axes x1y1