!#/bin/bash

pushd src/com
mkdir  classes
javac -d classes -classpath ../../matlabsrc/jars/jxbrowser-5.4.3.jar help/*.java
cd classes
jar cf ../lib/cluetube.jar  com
cd ..
rm -r classes
popd
