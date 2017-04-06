<?php

function phiS32ToBits($x) {
    $isNegative = $x < 0;
    if ($isNegative)
        $x = -$x;

    $bits = base_convert(strval($x), 10, 2);
    if (strlen($bits) > 32)
        throw new PhiIllegalStateException("83fb6b93-b954-4564-9216-af3af3245d65");
    $bits = str_repeat('0', 32 - strlen($bits)) . $bits;

    if ($isNegative)
        $bits = phiNegateAndAddOne($bits);

    return $bits;
}

function phiNegateAndAddOne($bits) {
    for ($i = 0; $i < strlen($bits); ++$i) {
        $bits[$i] = $bits[$i] === '0' ? '1' : '0';
    }
    // phiPrintln("Negated: $bits");
    for ($i = strlen($bits) - 1; $i >= 0; --$i) {
        if ($bits[$i] === '0') {
            $bits[$i] = '1';
            break;
        } else {
            $bits[$i] = '0';
        }
    }
    // phiPrintln("+1: $bits");
    return $bits;
}

function fuckingLong_lowHighToString($low, $high) {
    $bits = phiS32ToBits($high) . phiS32ToBits($low);
    // phiPrintln("Long bits: $bits");

    $isNegative = $bits[0] === '1';
    if ($isNegative) {
        $bits = phiNegateAndAddOne($bits);
    }

    $long = '0';
    for ($i = 0; $i < 64; ++$i) {
        if ($bits[$i] === '1') {
            $exp = 63 - $i;
            $long = bcadd($long, bcpow('2', strval($exp)));
        }
    }
    if ($isNegative)
        $long = "-$long";

    return $long;
}

// Expected: 922337203685477580
$long = fuckingLong_lowHighToString(-858993460, 214748364);
phiPrintln("long = $long");


function phiPrintln($x) {
    echo $x . "\n";
}

function phiPrint($x) {
    echo $x;
}

