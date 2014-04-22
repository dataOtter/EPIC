#!/bin/csh -f
rm -rf LP-package
rm -rf analysis-package
mkdir analysis-package
mkdir LP-package
cp analysis/*.csv analysis-package
cp LP/*.txt LP/*.csv LP-package
