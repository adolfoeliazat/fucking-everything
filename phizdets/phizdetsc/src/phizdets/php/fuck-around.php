<?php ; if (phiEvaluateToBoolean(new PhiBinaryOperation("@@2", "===", new PhiUnaryOperation("@@1", "prefix", "typeof", new PhiNameRef("kotlin")), new PhiStringLiteral("undefined")))) {
  phiThrow(new PhiNew(new PhiNameRef("Error"), array(new PhiStringLiteral("Error loading module 'phi-gross-test-1'. Its dependency 'kotlin' was not found. Please, check whether 'kotlin' is loaded prior to 'phi-gross-test-1'."))));
}
$GLOBALS['shit'] = 64; phiExpressionStatement(new PhiBinaryOperation("@@98", "=", new PhiBrackets(new PhiThis(), new PhiStringLiteral("phi-gross-test-1")), new PhiInvocation(new PhiFunctionExpression(null, array("_", "Kotlin"), function () {
  $GLOBALS['shit'] = 2; phiExpressionStatement(new PhiFunctionExpression("NotNullOnceVar", array(), function () {
    $GLOBALS['shit'] = 1; phiExpressionStatement(new PhiBinaryOperation("@@4", "=", new PhiDot(new PhiThis(), "value_0"), new PhiNullLiteral("@@3")));
  }));
  $GLOBALS['shit'] = 3; phiExpressionStatement(new PhiFunctionExpression("notNullOnce", array(), function () {
    return phiEvaluate(new PhiNew(new PhiNameRef("NotNullOnceVar"), array()));
  }));
  $GLOBALS['shit'] = 5; phiExpressionStatement(new PhiFunctionExpression("sayShit", array("shit"), function () {
    $GLOBALS['shit'] = 4; phiExpressionStatement(new PhiInvocation(new PhiNameRef("phiPrintln"), array(new PhiBinaryOperation("@@8", "+", new PhiBinaryOperation("@@7", "+", new PhiBinaryOperation("@@6", "+", new PhiBinaryOperation("@@5", "+", new PhiStringLiteral("<b>"), new PhiDot(new PhiNameRef("shit"), "text")), new PhiStringLiteral(", ")), new PhiDot(new PhiNameRef("shit"), "name")), new PhiStringLiteral("<\/b>")))));
  }));
  $GLOBALS['shit'] = 7; phiExpressionStatement(new PhiFunctionExpression("phiPrintln", array("x"), function () {
    $GLOBALS['shit'] = 6; phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@9", "+", new PhiNameRef("x"), new PhiStringLiteral("\n")))));
  }));
  $GLOBALS['shit'] = 11; phiExpressionStatement(new PhiFunctionExpression("Shit", array("name", "text"), function () {
    $GLOBALS['shit'] = 8; phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNameRef("ShitParent"), "call"), array(new PhiThis(), new PhiStringLiteral("fucking-a"), new PhiStringLiteral("fucking-b"))));
    $GLOBALS['shit'] = 9; phiExpressionStatement(new PhiBinaryOperation("@@10", "=", new PhiDot(new PhiThis(), "name"), new PhiNameRef("name")));
    $GLOBALS['shit'] = 10; phiExpressionStatement(new PhiBinaryOperation("@@11", "=", new PhiDot(new PhiThis(), "text"), new PhiNameRef("text")));
  }));
  $GLOBALS['shit'] = 15; phiExpressionStatement(new PhiFunctionExpression("ShitParent", array("a", "b"), function () {
    $GLOBALS['shit'] = 12; phiExpressionStatement(new PhiBinaryOperation("@@12", "=", new PhiDot(new PhiThis(), "a"), new PhiNameRef("a")));
    $GLOBALS['shit'] = 13; phiExpressionStatement(new PhiBinaryOperation("@@13", "=", new PhiDot(new PhiThis(), "b"), new PhiNameRef("b")));
    $GLOBALS['shit'] = 14; phiExpressionStatement(new PhiBinaryOperation("@@14", "=", new PhiDot(new PhiThis(), "prelude"), new PhiStringLiteral("Now I'm really gonna say it...")));
  }));
  $GLOBALS['shit'] = 17; phiExpressionStatement(new PhiFunctionExpression("test1\$Q_getInstance", array(), function () {
    if (phiEvaluateToBoolean(new PhiBinaryOperation("@@16", "===", new PhiNameRef("test1\$Q_instance"), new PhiNullLiteral("@@15")))) {
      $GLOBALS['shit'] = 16; phiExpressionStatement(new PhiNew(new PhiNameRef("test1\$Q"), array()));
    }
    return phiEvaluate(new PhiNameRef("test1\$Q_instance"));
  }));
  $GLOBALS['shit'] = 20; phiExpressionStatement(new PhiFunctionExpression("test1\$Q", array(), function () {
    $GLOBALS['shit'] = 18; phiExpressionStatement(new PhiBinaryOperation("@@17", "=", new PhiNameRef("test1\$Q_instance"), new PhiThis()));
    $GLOBALS['shit'] = 19; phiExpressionStatement(new PhiBinaryOperation("@@18", "=", new PhiDot(new PhiThis(), "a\$delegate"), new PhiInvocation(new PhiNameRef("notNullOnce"), array())));
  }));
  $GLOBALS['shit'] = 25; phiExpressionStatement(new PhiFunctionExpression("test1", array(), function () {
    $GLOBALS['shit'] = 21; phiExpressionStatement(new PhiBinaryOperation("@@19", "=", new PhiDot(new PhiInvocation(new PhiNameRef("test1\$Q_getInstance"), array()), "a"), new PhiStringLiteral("pizda")));
    $GLOBALS['shit'] = 22; phiExpressionStatement(new PhiInvocation(new PhiNameRef("phiPrintln"), array(new PhiDot(new PhiInvocation(new PhiNameRef("test1\$Q_getInstance"), array()), "a"))));
    $GLOBALS['shit'] = 23; phiExpressionStatement(new PhiBinaryOperation("@@20", "=", new PhiDot(new PhiInvocation(new PhiNameRef("test1\$Q_getInstance"), array()), "a"), new PhiStringLiteral("pizda again")));
    $GLOBALS['shit'] = 24; phiExpressionStatement(new PhiInvocation(new PhiNameRef("phiPrintln"), array(new PhiDot(new PhiInvocation(new PhiNameRef("test1\$Q_getInstance"), array()), "a"))));
  }));
  $GLOBALS['shit'] = 27; phiExpressionStatement(new PhiFunctionExpression("testCheck", array(), function () {
    try {
      if (phiEvaluateToBoolean(new PhiUnaryOperation("@@22", "prefix", "!", new PhiBooleanLiteral("@@21", false)))) {
        phiVars("@@23", array(array("message", new PhiStringLiteral("pizda"))));
        phiThrow(new PhiNew(new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "IllegalStateException"), array(new PhiInvocation(new PhiDot(new PhiNameRef("message"), "toString"), array()))));
      }
    }
     catch (Exception $__phiException) {
      phiVars(array(array('e', $__phiException->phiValue)));;
      $GLOBALS['shit'] = 26; phiExpressionStatement(new PhiInvocation(new PhiNameRef("phiPrintln"), array(new PhiStringLiteral("Well..."))));
    }
  }));
  $GLOBALS['shit'] = 31; phiExpressionStatement(new PhiFunctionExpression("main", array("args"), function () {
    $GLOBALS['shit'] = 28; phiExpressionStatement(new PhiInvocation(new PhiNameRef("sayShit"), array(new PhiNew(new PhiNameRef("Shit"), array(new PhiStringLiteral("Archibald"), new PhiStringLiteral("Fuck you"))))));
    $GLOBALS['shit'] = 29; phiExpressionStatement(new PhiInvocation(new PhiNameRef("testCheck"), array()));
    $GLOBALS['shit'] = 30; phiExpressionStatement(new PhiNew(new PhiNameRef("test1"), array()));
  }));
  $GLOBALS['shit'] = 32; phiExpressionStatement(new PhiStringLiteral("use strict"));
  phiVars("@@24", array(array("IllegalStateException", new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "IllegalStateException"))));
  phiVars("@@25", array(array("ReadWriteProperty", new PhiDot(new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "properties"), "ReadWriteProperty"))));
  $GLOBALS['shit'] = 33; phiExpressionStatement(new PhiBinaryOperation("@@26", "=", new PhiDot(new PhiNameRef("Shit"), "prototype"), new PhiInvocation(new PhiDot(new PhiNameRef("Object"), "create"), array(new PhiDot(new PhiNameRef("ShitParent"), "prototype")))));
  $GLOBALS['shit'] = 34; phiExpressionStatement(new PhiBinaryOperation("@@27", "=", new PhiDot(new PhiDot(new PhiNameRef("Shit"), "prototype"), "constructor"), new PhiNameRef("Shit")));
  $GLOBALS['shit'] = 36; phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNameRef("Object"), "defineProperty"), array(new PhiDot(new PhiNameRef("test1\$Q"), "prototype"), new PhiStringLiteral("a"), new PhiObjectLiteral("@@28", array(array(new PhiStringLiteral("get"), new PhiFunctionExpression(null, array(), function () {
    return phiEvaluate(new PhiInvocation(new PhiDot(new PhiDot(new PhiThis(), "a\$delegate"), "getValue_lrcp0p\$"), array(new PhiThis(), new PhiNew(new PhiDot(new PhiNameRef("Kotlin"), "PropertyMetadata"), array(new PhiStringLiteral("a"))))));
  })), array(new PhiStringLiteral("set"), new PhiFunctionExpression(null, array("a"), function () {
    $GLOBALS['shit'] = 35; phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiDot(new PhiNew(new PhiNameRef("PhiThis"), array()), "a\$delegate"), "setValue_9rddgb\$"), array(new PhiThis(), new PhiNew(new PhiDot(new PhiNameRef("Kotlin"), "PropertyMetadata"), array(new PhiStringLiteral("a"))), new PhiNameRef("a"))));
  })))))));
  $GLOBALS['shit'] = 37; phiExpressionStatement(new PhiBinaryOperation("@@30", "=", new PhiDot(new PhiNameRef("test1\$Q"), "\$metadata\$"), new PhiObjectLiteral("@@29", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "OBJECT")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("Q")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array()))))));
  phiVars("@@32", array(array("test1\$Q_instance", new PhiNullLiteral("@@31"))));
  $GLOBALS['shit'] = 38; phiExpressionStatement(new PhiBinaryOperation("@@34", "=", new PhiDot(new PhiNameRef("test1"), "\$metadata\$"), new PhiObjectLiteral("@@33", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "CLASS")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("test1")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array()))))));
  $GLOBALS['shit'] = 43; phiExpressionStatement(new PhiBinaryOperation("@@42", "=", new PhiDot(new PhiDot(new PhiNameRef("ShitParent"), "prototype"), "sayIt_61zpoe\$"), new PhiFunctionExpression(null, array("c"), function () {
    $GLOBALS['shit'] = 39; phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@36", "+", new PhiBinaryOperation("@@35", "+", new PhiStringLiteral("a = "), new PhiDot(new PhiThis(), "a")), new PhiStringLiteral("\n")))));
    $GLOBALS['shit'] = 40; phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@38", "+", new PhiBinaryOperation("@@37", "+", new PhiStringLiteral("b = "), new PhiDot(new PhiThis(), "b")), new PhiStringLiteral("\n")))));
    $GLOBALS['shit'] = 41; phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@40", "+", new PhiBinaryOperation("@@39", "+", new PhiStringLiteral("c = "), new PhiNameRef("c")), new PhiStringLiteral("\n")))));
    $GLOBALS['shit'] = 42; phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@41", "+", new PhiDot(new PhiThis(), "prelude"), new PhiStringLiteral("\n")))));
  })));
  $GLOBALS['shit'] = 44; phiExpressionStatement(new PhiBinaryOperation("@@44", "=", new PhiDot(new PhiNameRef("ShitParent"), "\$metadata\$"), new PhiObjectLiteral("@@43", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "CLASS")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("ShitParent")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array()))))));
  $GLOBALS['shit'] = 47; phiExpressionStatement(new PhiBinaryOperation("@@50", "=", new PhiDot(new PhiDot(new PhiNameRef("Shit"), "prototype"), "sayIt_61zpoe\$"), new PhiFunctionExpression(null, array("c"), function () {
    $GLOBALS['shit'] = 45; phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiDot(new PhiDot(new PhiNameRef("ShitParent"), "prototype"), "sayIt_61zpoe\$"), "call"), array(new PhiThis(), new PhiStringLiteral("fucking-c"))));
    $GLOBALS['shit'] = 46; phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@49", "+", new PhiBinaryOperation("@@48", "+", new PhiBinaryOperation("@@47", "+", new PhiBinaryOperation("@@46", "+", new PhiBinaryOperation("@@45", "+", new PhiStringLiteral("<b>"), new PhiDot(new PhiThis(), "name")), new PhiStringLiteral(", ")), new PhiDot(new PhiThis(), "text")), new PhiNameRef("c")), new PhiStringLiteral("<\/b>")))));
  })));
  $GLOBALS['shit'] = 48; phiExpressionStatement(new PhiBinaryOperation("@@52", "=", new PhiDot(new PhiNameRef("Shit"), "\$metadata\$"), new PhiObjectLiteral("@@51", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "CLASS")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("Shit")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array(new PhiNameRef("ShitParent"))))))));
  $GLOBALS['shit'] = 50; phiExpressionStatement(new PhiBinaryOperation("@@60", "=", new PhiDot(new PhiDot(new PhiNameRef("NotNullOnceVar"), "prototype"), "getValue_lrcp0p\$"), new PhiFunctionExpression(null, array("thisRef", "property"), function () {
    phiVars("@@54", array(array("tmp\$", new PhiUnaryOperation("@@53", "prefix", "void", new PhiNumberLiteral("@@something", 0)))));
    $GLOBALS['shit'] = 49; phiExpressionStatement(new PhiBinaryOperation("@@55", "=", new PhiNameRef("tmp\$"), new PhiDot(new PhiThis(), "value_0")));
    if (phiEvaluateToBoolean(new PhiBinaryOperation("@@57", "==", new PhiNameRef("tmp\$"), new PhiNullLiteral("@@56")))) {
      phiThrow(new PhiNew(new PhiNameRef("IllegalStateException"), array(new PhiBinaryOperation("@@59", "+", new PhiBinaryOperation("@@58", "+", new PhiStringLiteral("Property `"), new PhiDot(new PhiNameRef("property"), "callableName")), new PhiStringLiteral("` should be initialized before get.")))));
    }
    return phiEvaluate(new PhiNameRef("tmp\$"));
  })));
  $GLOBALS['shit'] = 52; phiExpressionStatement(new PhiBinaryOperation("@@68", "=", new PhiDot(new PhiDot(new PhiNameRef("NotNullOnceVar"), "prototype"), "setValue_9rddgb\$"), new PhiFunctionExpression(null, array("thisRef", "property", "value"), function () {
    if (phiEvaluateToBoolean(new PhiUnaryOperation("@@63", "prefix", "!", new PhiBinaryOperation("@@62", "==", new PhiDot(new PhiThis(), "value_0"), new PhiNullLiteral("@@61"))))) {
      phiVars("@@66", array(array("message", new PhiBinaryOperation("@@65", "+", new PhiBinaryOperation("@@64", "+", new PhiStringLiteral("Property `"), new PhiDot(new PhiNameRef("property"), "callableName")), new PhiStringLiteral("` should be assigned only once")))));
      phiThrow(new PhiNew(new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "IllegalStateException"), array(new PhiInvocation(new PhiDot(new PhiNameRef("message"), "toString"), array()))));
    }
    $GLOBALS['shit'] = 51; phiExpressionStatement(new PhiBinaryOperation("@@67", "=", new PhiDot(new PhiThis(), "value_0"), new PhiNameRef("value")));
  })));
  $GLOBALS['shit'] = 53; phiExpressionStatement(new PhiBinaryOperation("@@70", "=", new PhiDot(new PhiNameRef("NotNullOnceVar"), "\$metadata\$"), new PhiObjectLiteral("@@69", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "CLASS")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("NotNullOnceVar")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array(new PhiNameRef("ReadWriteProperty"))))))));
  phiVars("@@74", array(array("package\$phi", new PhiBinaryOperation("@@73", "||", new PhiDot(new PhiNameRef("_"), "phi"), new PhiBinaryOperation("@@72", "=", new PhiDot(new PhiNameRef("_"), "phi"), new PhiObjectLiteral("@@71", array()))))));
  phiVars("@@78", array(array("package\$gross", new PhiBinaryOperation("@@77", "||", new PhiDot(new PhiNameRef("package\$phi"), "gross"), new PhiBinaryOperation("@@76", "=", new PhiDot(new PhiNameRef("package\$phi"), "gross"), new PhiObjectLiteral("@@75", array()))))));
  phiVars("@@82", array(array("package\$test", new PhiBinaryOperation("@@81", "||", new PhiDot(new PhiNameRef("package\$gross"), "test"), new PhiBinaryOperation("@@80", "=", new PhiDot(new PhiNameRef("package\$gross"), "test"), new PhiObjectLiteral("@@79", array()))))));
  phiVars("@@86", array(array("package\$one", new PhiBinaryOperation("@@85", "||", new PhiDot(new PhiNameRef("package\$test"), "one"), new PhiBinaryOperation("@@84", "=", new PhiDot(new PhiNameRef("package\$test"), "one"), new PhiObjectLiteral("@@83", array()))))));
  $GLOBALS['shit'] = 54; phiExpressionStatement(new PhiBinaryOperation("@@87", "=", new PhiDot(new PhiNameRef("package\$one"), "main_kand9s\$"), new PhiNameRef("main")));
  $GLOBALS['shit'] = 55; phiExpressionStatement(new PhiBinaryOperation("@@88", "=", new PhiDot(new PhiNameRef("package\$one"), "testCheck"), new PhiNameRef("testCheck")));
  $GLOBALS['shit'] = 56; phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNameRef("Object"), "defineProperty"), array(new PhiNameRef("test1"), new PhiStringLiteral("Q"), new PhiObjectLiteral("@@89", array(array(new PhiNameRef("get"), new PhiNameRef("test1\$Q_getInstance")))))));
  $GLOBALS['shit'] = 57; phiExpressionStatement(new PhiBinaryOperation("@@90", "=", new PhiDot(new PhiNameRef("package\$one"), "test1"), new PhiNameRef("test1")));
  $GLOBALS['shit'] = 58; phiExpressionStatement(new PhiBinaryOperation("@@91", "=", new PhiDot(new PhiNameRef("package\$one"), "ShitParent"), new PhiNameRef("ShitParent")));
  $GLOBALS['shit'] = 59; phiExpressionStatement(new PhiBinaryOperation("@@92", "=", new PhiDot(new PhiNameRef("package\$one"), "Shit"), new PhiNameRef("Shit")));
  $GLOBALS['shit'] = 60; phiExpressionStatement(new PhiBinaryOperation("@@93", "=", new PhiDot(new PhiNameRef("package\$one"), "phiPrintln_61zpoe\$"), new PhiNameRef("phiPrintln")));
  $GLOBALS['shit'] = 61; phiExpressionStatement(new PhiBinaryOperation("@@94", "=", new PhiDot(new PhiNameRef("package\$one"), "notNullOnce_30y1fr\$"), new PhiNameRef("notNullOnce")));
  $GLOBALS['shit'] = 62; phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "defineModule"), array(new PhiStringLiteral("phi-gross-test-1"), new PhiNameRef("_"))));
  $GLOBALS['shit'] = 63; phiExpressionStatement(new PhiInvocation(new PhiNameRef("main"), array(new PhiArrayLiteral(array()))));
  return phiEvaluate(new PhiNameRef("_"));
}), array(new PhiConditional(new PhiBinaryOperation("@@96", "===", new PhiUnaryOperation("@@95", "prefix", "typeof", new PhiBrackets(new PhiThis(), new PhiStringLiteral("phi-gross-test-1"))), new PhiStringLiteral("undefined")), new PhiObjectLiteral("@@97", array()), new PhiBrackets(new PhiThis(), new PhiStringLiteral("phi-gross-test-1"))), new PhiNameRef("kotlin")))));
