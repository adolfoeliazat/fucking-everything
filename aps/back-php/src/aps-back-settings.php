<?php

define("INTO_COMPOSER_VENDOR_DIR", "E:\\fegh\\composer\\vendor");

require_once(INTO_COMPOSER_VENDOR_DIR . "/autoload.php");

$isDevMode = true;
$config = Doctrine\ORM\Tools\Setup::createAnnotationMetadataConfiguration(array(__DIR__ . "/src"), $isDevMode);

$dbParams = array(
    'driver'   => 'pdo_mysql',
    'user'     => 'boobs',
    'password' => 'tits',
    'dbname'   => 'boobs',
);


$entityManager = Doctrine\ORM\EntityManager::create($dbParams, $config);