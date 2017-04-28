#!/bin/bash

stamp=20170427

outdir=/mnt/e/bak/$stamp
echo Backing shit up...

rm -rf $outdir
mkdir $outdir

cd /mnt/e/fegh
zip -r $outdir/fe-$stamp.zip . \
    -x /.git* \
    -x /node_modules/* \
    -x /composer/vendor/* \
    -x /out/* \
    -x /lib-gradle/* \
    -x /.gradle/* \
    -x /phizdets/phizdets-idea/eclipse-lib/*

cd /mnt/e/work/aps
zip -r $outdir/aps-$stamp.zip . \
    -x /.git* \
    -x /node_modules/* \
    -x /back/.gradle/* \
    -x /back/built/* \
    -x /back/lib-gradle/* \
    -x /back/out/* \
    -x /front/out/* \
    -x /javaagent/out/* \
    -x /kotlin-js-playground/node_modules/* \
    -x /kotlin-js-playground/out/* \
    -x /kotlin-jvm-playground/out/* \
    -x /tools/out/*

echo OK

