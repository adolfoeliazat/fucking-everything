<?php

// define('PHI_RUN_QUICK_TESTS', true);
// define('PHI_RUN_QUICK_STDLIB_TESTS', true);

$_SERVER['QUERY_STRING'] = 'pathInfo=/rpc/Miranda';

try {
    require_once 'phi-engine.php';
    require_once 'fuckaroundapsback/aps-back.php';
} catch (Throwable $e) {
    error_log("\n" . $e->getMessage() . "\n" . $e->getFile() . ':' . $e->getLine() . "\n\n" . $e->getTraceAsString());
}


