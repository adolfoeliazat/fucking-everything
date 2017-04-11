<?php

define('PHI_RUN_QUICK_TESTS', true);
define('PHI_RUN_QUICK_STDLIB_TESTS', true);

$_SERVER['QUERY_STRING'] = 'pathInfo=/rpc/Miranda';

require_once 'phi-engine.php';
require_once 'fuck-around--aps-back.php';


