<?php

function println($x) {
    echo "$x\n";
}

$f = eval('return function() {echo "pizda";};');
$f();
