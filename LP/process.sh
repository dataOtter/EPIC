#!/bin/csh -f

set PARAMS=""

rm -f xxxErrors.txt;
touch xxxErrors.txt;

rm -f Analysis.txt;
touch Analysis.txt;

printf "# Ratio-margin\t""Law-error\n" > Errors.txt;

foreach F (`ls lp*.txt | sort`)
    set cmd1="printf city_year\t";
    set cmd2="paste xxxp0.leg ";

    set err=`echo $F | sed -e 's/\.txt//' | sed -e 's/lp//'` 

#    ./lp_solve $F > zzzLP$err.txt
# FILTER OUT NEAR ZERO VALUES
    ./lp_solve $PARAMS $F | sed -e "s/[0-9]*\.[0-9]*e-[0-9]*/0/" > zzzLP$err.txt

    set bad=`cat zzzLP$err.txt | grep failed | wc -l`;
    if ("$bad" == "1") then
	echo "* $err failed"
	continue
    endif

    set bad=`cat zzzLP$err.txt | grep infeasible | wc -l`;
    if ("$bad" == "1") then
	echo "* $err infeasible"
	continue
    endif

    echo "  $err feasible"

    cat zzzLP$err.txt | grep "^pL1.*" | sed -e 's/  */ /' | cut -f2 -d' ' > xxx$err.err
    printf "$err\t" >> xxxErrors.txt;
    cat xxx$err.err >> xxxErrors.txt;

    foreach P (`cat $F | grep "^pos_" | sed -e 's/.*: //' | sed -e 's/  */ /' | cut -f1 -d' ' | sed -e 's/\([^_]*\).*/\1/' | grep "^p" | sort -u | grep -v L1`)
	cat zzzLP$err.txt | grep "^$P.*" | sed -e 's/  */ /' | cut -f2 -d' ' > xxx$P.txt
	cat zzzLP$err.txt | grep "^$P.*" | sed -e 's/  */ /' | cut -f1 -d' ' | sed -e 's/[^_]*_//' > xxx$P.leg

	set cmd1="$cmd1$P\t"
	set cmd2="$cmd2 xxx$P.txt"
    end

    echo "# ========" >> Analysis.txt;
    printf "# Ratio-margin\t$err percent\n" >> Analysis.txt;

    set cmd1="$cmd1\n"
    $cmd1 >> Analysis.txt;
    $cmd2 >> Analysis.txt;
end

cat  xxxErrors.txt | sort -k1 -n >> Errors.txt;

rm xxx*;

sleep 2;

cat Analysis.txt | tr '\t' , > Analysis.csv

cat Analysis.txt;
cat Errors.txt;
