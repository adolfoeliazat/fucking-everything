<?php

require_once 'phi-engine.php';

$x = call_user_func_array('phiEval', array('return 2 + 3;'));
phiPrintln($x);

