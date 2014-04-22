#!/bin/csh -f

gnuplot < plot.gp

foreach f (`ls -1 *.ps`)
    set f2=`echo $f | sed -e 's/\..*//'`
    if (-f "$f2.eps") then
	rm -f $f2.eps
    endif

    ps2eps -q $f

    echo "$f ==> $f2.eps"
    mv $f2.eps ~/papers/Bhutani-Estimation/figures/
end
