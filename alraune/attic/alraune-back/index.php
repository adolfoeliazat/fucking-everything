<?php

$loadingStartTime = microtime(true);

require 'out-back/shared-x.php';
require 'out-back/alraune-shared.php';
require 'out-back/alraune-back.php';

$loadingTime = microtime(true) - $loadingStartTime;
echo "loadingTime = $loadingTime";

