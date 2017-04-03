<?php ; if (phiEvaluateToBoolean(new PhiBinaryOperation("@@1", "===", new PhiUnaryOperation("prefix", "typeof", new PhiNameRef("kotlin")), new PhiStringLiteral("undefined")))) {
  phiThrow(new PhiNew(new PhiNameRef("Error"), array(new PhiStringLiteral("Error loading module 'phi-gross-test-1'. Its dependency 'kotlin' was not found. Please, check whether 'kotlin' is loaded prior to 'phi-gross-test-1'."))));
}
$GLOBALS['shit'] = 47; phiExpressionStatement(new PhiBinaryOperation("@@76", "=", new PhiBrackets(new PhiThis(), new PhiStringLiteral("phi-gross-test-1")), new PhiInvocation(new PhiFunctionExpression(null, array("_", "Kotlin"), function () {
  $GLOBALS['shit'] = 2; phiExpressionStatement(new PhiFunctionExpression("NotNullOnceVar", array(), function () {
    $GLOBALS['shit'] = 1; phiExpressionStatement(new PhiBinaryOperation("@@3", "=", new PhiDot(new PhiThis(), "value_0"), new PhiNullLiteral("@@2")));
  }));
  $GLOBALS['shit'] = 3; phiExpressionStatement(new PhiFunctionExpression("notNullOnce", array(), function () {
    return phiEvaluate(new PhiNew(new PhiNameRef("NotNullOnceVar"), array()));
  }));
  $GLOBALS['shit'] = 5; phiExpressionStatement(new PhiFunctionExpression("sayShit", array("shit"), function () {
    $GLOBALS['shit'] = 4; phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@7", "+", new PhiBinaryOperation("@@6", "+", new PhiBinaryOperation("@@5", "+", new PhiBinaryOperation("@@4", "+", new PhiStringLiteral("<b>"), new PhiDot(new PhiNameRef("shit"), "text")), new PhiStringLiteral(", ")), new PhiDot(new PhiNameRef("shit"), "name")), new PhiStringLiteral("<\/b>")))));
  }));
  $GLOBALS['shit'] = 6; phiExpressionStatement(new PhiFunctionExpression("bbbbbbbbbbb", array(), function () {
  }));
  $GLOBALS['shit'] = 8; phiExpressionStatement(new PhiFunctionExpression("qwe", array(), function () {
    $GLOBALS['shit'] = 7; phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNew(new PhiNameRef("Shit"), array(new PhiStringLiteral("Archibald"), new PhiStringLiteral("fuck you"))), "sayIt_61zpoe\$"), array(new PhiStringLiteral("!!!!!"))));
  }));
  $GLOBALS['shit'] = 12; phiExpressionStatement(new PhiFunctionExpression("Shit", array("name", "text"), function () {
    $GLOBALS['shit'] = 9; phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNameRef("ShitParent"), "call"), array(new PhiThis(), new PhiStringLiteral("fucking-a"), new PhiStringLiteral("fucking-b"))));
    $GLOBALS['shit'] = 10; phiExpressionStatement(new PhiBinaryOperation("@@8", "=", new PhiDot(new PhiThis(), "name"), new PhiNameRef("name")));
    $GLOBALS['shit'] = 11; phiExpressionStatement(new PhiBinaryOperation("@@9", "=", new PhiDot(new PhiThis(), "text"), new PhiNameRef("text")));
  }));
  $GLOBALS['shit'] = 16; phiExpressionStatement(new PhiFunctionExpression("ShitParent", array("a", "b"), function () {
    $GLOBALS['shit'] = 13; phiExpressionStatement(new PhiBinaryOperation("@@10", "=", new PhiDot(new PhiThis(), "a"), new PhiNameRef("a")));
    $GLOBALS['shit'] = 14; phiExpressionStatement(new PhiBinaryOperation("@@11", "=", new PhiDot(new PhiThis(), "b"), new PhiNameRef("b")));
    $GLOBALS['shit'] = 15; phiExpressionStatement(new PhiBinaryOperation("@@12", "=", new PhiDot(new PhiThis(), "prelude"), new PhiStringLiteral("Now I'm really gonna say it...")));
  }));
  $GLOBALS['shit'] = 17; phiExpressionStatement(new PhiFunctionExpression("aaaaaaaaaaa", array(), function () {
  }));
  $GLOBALS['shit'] = 19; phiExpressionStatement(new PhiFunctionExpression("main", array("args"), function () {
    $GLOBALS['shit'] = 18; phiExpressionStatement(new PhiInvocation(new PhiNameRef("sayShit"), array(new PhiNew(new PhiNameRef("Shit"), array(new PhiStringLiteral("Archibald"), new PhiStringLiteral("Fuck you"))))));
  }));
  $GLOBALS['shit'] = 20; phiExpressionStatement(new PhiStringLiteral("use strict"));
  phiVars("@@13", array(array("IllegalStateException", new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "IllegalStateException"))));
  phiVars("@@14", array(array("ReadWriteProperty", new PhiDot(new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "properties"), "ReadWriteProperty"))));
  $GLOBALS['shit'] = 21; phiExpressionStatement(new PhiBinaryOperation("@@15", "=", new PhiDot(new PhiNameRef("Shit"), "prototype"), new PhiInvocation(new PhiDot(new PhiNameRef("Object"), "create"), array(new PhiDot(new PhiNameRef("ShitParent"), "prototype")))));
  $GLOBALS['shit'] = 22; phiExpressionStatement(new PhiBinaryOperation("@@16", "=", new PhiDot(new PhiDot(new PhiNameRef("Shit"), "prototype"), "constructor"), new PhiNameRef("Shit")));
  $GLOBALS['shit'] = 27; phiExpressionStatement(new PhiBinaryOperation("@@24", "=", new PhiDot(new PhiDot(new PhiNameRef("ShitParent"), "prototype"), "sayIt_61zpoe\$"), new PhiFunctionExpression(null, array("c"), function () {
    $GLOBALS['shit'] = 23; phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@18", "+", new PhiBinaryOperation("@@17", "+", new PhiStringLiteral("a = "), new PhiDot(new PhiThis(), "a")), new PhiStringLiteral("\n")))));
    $GLOBALS['shit'] = 24; phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@20", "+", new PhiBinaryOperation("@@19", "+", new PhiStringLiteral("b = "), new PhiDot(new PhiThis(), "b")), new PhiStringLiteral("\n")))));
    $GLOBALS['shit'] = 25; phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@22", "+", new PhiBinaryOperation("@@21", "+", new PhiStringLiteral("c = "), new PhiNameRef("c")), new PhiStringLiteral("\n")))));
    $GLOBALS['shit'] = 26; phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@23", "+", new PhiDot(new PhiThis(), "prelude"), new PhiStringLiteral("\n")))));
  })));
  $GLOBALS['shit'] = 28; phiExpressionStatement(new PhiBinaryOperation("@@26", "=", new PhiDot(new PhiNameRef("ShitParent"), "\$metadata\$"), new PhiObjectLiteral("@@25", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "CLASS")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("ShitParent")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array()))))));
  $GLOBALS['shit'] = 31; phiExpressionStatement(new PhiBinaryOperation("@@32", "=", new PhiDot(new PhiDot(new PhiNameRef("Shit"), "prototype"), "sayIt_61zpoe\$"), new PhiFunctionExpression(null, array("c"), function () {
    $GLOBALS['shit'] = 29; phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiDot(new PhiDot(new PhiNameRef("ShitParent"), "prototype"), "sayIt_61zpoe\$"), "call"), array(new PhiThis(), new PhiStringLiteral("fucking-c"))));
    $GLOBALS['shit'] = 30; phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@31", "+", new PhiBinaryOperation("@@30", "+", new PhiBinaryOperation("@@29", "+", new PhiBinaryOperation("@@28", "+", new PhiBinaryOperation("@@27", "+", new PhiStringLiteral("<b>"), new PhiDot(new PhiThis(), "name")), new PhiStringLiteral(", ")), new PhiDot(new PhiThis(), "text")), new PhiNameRef("c")), new PhiStringLiteral("<\/b>")))));
  })));
  $GLOBALS['shit'] = 32; phiExpressionStatement(new PhiBinaryOperation("@@34", "=", new PhiDot(new PhiNameRef("Shit"), "\$metadata\$"), new PhiObjectLiteral("@@33", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "CLASS")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("Shit")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array(new PhiNameRef("ShitParent"))))))));
  $GLOBALS['shit'] = 34; phiExpressionStatement(new PhiBinaryOperation("@@41", "=", new PhiDot(new PhiDot(new PhiNameRef("NotNullOnceVar"), "prototype"), "getValue_lrcp0p\$"), new PhiFunctionExpression(null, array("thisRef", "property"), function () {
    phiVars("@@35", array(array("tmp\$", new PhiUnaryOperation("prefix", "void", new PhiNumberLiteral("@@something", 0)))));
    $GLOBALS['shit'] = 33; phiExpressionStatement(new PhiBinaryOperation("@@36", "=", new PhiNameRef("tmp\$"), new PhiDot(new PhiThis(), "value_0")));
    if (phiEvaluateToBoolean(new PhiBinaryOperation("@@38", "==", new PhiNameRef("tmp\$"), new PhiNullLiteral("@@37")))) {
      phiThrow(new PhiNew(new PhiNameRef("IllegalStateException"), array(new PhiBinaryOperation("@@40", "+", new PhiBinaryOperation("@@39", "+", new PhiStringLiteral("Property `"), new PhiDot(new PhiNameRef("property"), "callableName")), new PhiStringLiteral("` should be initialized before get.")))));
    }
    return phiEvaluate(new PhiNameRef("tmp\$"));
  })));
  $GLOBALS['shit'] = 36; phiExpressionStatement(new PhiBinaryOperation("@@48", "=", new PhiDot(new PhiDot(new PhiNameRef("NotNullOnceVar"), "prototype"), "setValue_9rddgb\$"), new PhiFunctionExpression(null, array("thisRef", "property", "value"), function () {
    if (phiEvaluateToBoolean(new PhiUnaryOperation("prefix", "!", new PhiBinaryOperation("@@43", "==", new PhiDot(new PhiThis(), "value_0"), new PhiNullLiteral("@@42"))))) {
      phiVars("@@46", array(array("message", new PhiBinaryOperation("@@45", "+", new PhiBinaryOperation("@@44", "+", new PhiStringLiteral("Property `"), new PhiDot(new PhiNameRef("property"), "callableName")), new PhiStringLiteral("` should be assigned only once")))));
      phiThrow(new PhiNew(new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "IllegalStateException"), array(new PhiInvocation(new PhiDot(new PhiNameRef("message"), "toString"), array()))));
    }
    $GLOBALS['shit'] = 35; phiExpressionStatement(new PhiBinaryOperation("@@47", "=", new PhiDot(new PhiThis(), "value_0"), new PhiNameRef("value")));
  })));
  $GLOBALS['shit'] = 37; phiExpressionStatement(new PhiBinaryOperation("@@50", "=", new PhiDot(new PhiNameRef("NotNullOnceVar"), "\$metadata\$"), new PhiObjectLiteral("@@49", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "CLASS")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("NotNullOnceVar")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array(new PhiNameRef("ReadWriteProperty"))))))));
  phiVars("@@54", array(array("package\$phi", new PhiBinaryOperation("@@53", "||", new PhiDot(new PhiNameRef("_"), "phi"), new PhiBinaryOperation("@@52", "=", new PhiDot(new PhiNameRef("_"), "phi"), new PhiObjectLiteral("@@51", array()))))));
  phiVars("@@58", array(array("package\$gross", new PhiBinaryOperation("@@57", "||", new PhiDot(new PhiNameRef("package\$phi"), "gross"), new PhiBinaryOperation("@@56", "=", new PhiDot(new PhiNameRef("package\$phi"), "gross"), new PhiObjectLiteral("@@55", array()))))));
  phiVars("@@62", array(array("package\$test", new PhiBinaryOperation("@@61", "||", new PhiDot(new PhiNameRef("package\$gross"), "test"), new PhiBinaryOperation("@@60", "=", new PhiDot(new PhiNameRef("package\$gross"), "test"), new PhiObjectLiteral("@@59", array()))))));
  phiVars("@@66", array(array("package\$one", new PhiBinaryOperation("@@65", "||", new PhiDot(new PhiNameRef("package\$test"), "one"), new PhiBinaryOperation("@@64", "=", new PhiDot(new PhiNameRef("package\$test"), "one"), new PhiObjectLiteral("@@63", array()))))));
  $GLOBALS['shit'] = 38; phiExpressionStatement(new PhiBinaryOperation("@@67", "=", new PhiDot(new PhiNameRef("package\$one"), "main_kand9s\$"), new PhiNameRef("main")));
  $GLOBALS['shit'] = 39; phiExpressionStatement(new PhiBinaryOperation("@@68", "=", new PhiDot(new PhiNameRef("package\$one"), "aaaaaaaaaaa"), new PhiNameRef("aaaaaaaaaaa")));
  $GLOBALS['shit'] = 40; phiExpressionStatement(new PhiBinaryOperation("@@69", "=", new PhiDot(new PhiNameRef("package\$one"), "ShitParent"), new PhiNameRef("ShitParent")));
  $GLOBALS['shit'] = 41; phiExpressionStatement(new PhiBinaryOperation("@@70", "=", new PhiDot(new PhiNameRef("package\$one"), "Shit"), new PhiNameRef("Shit")));
  $GLOBALS['shit'] = 42; phiExpressionStatement(new PhiBinaryOperation("@@71", "=", new PhiDot(new PhiNameRef("package\$one"), "qwe"), new PhiNameRef("qwe")));
  $GLOBALS['shit'] = 43; phiExpressionStatement(new PhiBinaryOperation("@@72", "=", new PhiDot(new PhiNameRef("package\$one"), "bbbbbbbbbbb"), new PhiNameRef("bbbbbbbbbbb")));
  $GLOBALS['shit'] = 44; phiExpressionStatement(new PhiBinaryOperation("@@73", "=", new PhiDot(new PhiNameRef("package\$one"), "notNullOnce_30y1fr\$"), new PhiNameRef("notNullOnce")));
  $GLOBALS['shit'] = 45; phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "defineModule"), array(new PhiStringLiteral("phi-gross-test-1"), new PhiNameRef("_"))));
  $GLOBALS['shit'] = 46; phiExpressionStatement(new PhiInvocation(new PhiNameRef("main"), array(new PhiArrayLiteral(array()))));
  return phiEvaluate(new PhiNameRef("_"));
}), array(new PhiConditional(new PhiBinaryOperation("@@74", "===", new PhiUnaryOperation("prefix", "typeof", new PhiBrackets(new PhiThis(), new PhiStringLiteral("phi-gross-test-1"))), new PhiStringLiteral("undefined")), new PhiObjectLiteral("@@75", array()), new PhiBrackets(new PhiThis(), new PhiStringLiteral("phi-gross-test-1"))), new PhiNameRef("kotlin")))));
