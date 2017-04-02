<?php ; if (phiEvaluateToBoolean(new PhiBinaryOperation("@@1", "===", new PhiUnaryOperation("prefix", "typeof", new PhiNameRef("kotlin")), new PhiStringLiteral("undefined")))) {
  phiThrow(new PhiNew(new PhiNameRef("Error"), array(new PhiStringLiteral("Error loading module 'phi-gross-test-1'. Its dependency 'kotlin' was not found. Please, check whether 'kotlin' is loaded prior to 'phi-gross-test-1'."))));
}
$GLOBALS['shit'] = 47; phiExpressionStatement(new PhiBinaryOperation("@@68", "=", new PhiBrackets(new PhiThis(), new PhiStringLiteral("phi-gross-test-1")), new PhiInvocation(new PhiFunctionExpression(null, array("_", "Kotlin"), function () {
  $GLOBALS['shit'] = 1; phiExpressionStatement(new PhiStringLiteral("use strict"));
  phiVars(array(array("IllegalStateException", new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "IllegalStateException"))));
  phiVars(array(array("ReadWriteProperty", new PhiDot(new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "properties"), "ReadWriteProperty"))));
  $GLOBALS['shit'] = 2; phiExpressionStatement(new PhiBinaryOperation("@@2", "=", new PhiDot(new PhiNameRef("Shit"), "prototype"), new PhiInvocation(new PhiDot(new PhiNameRef("Object"), "create"), array(new PhiDot(new PhiNameRef("ShitParent"), "prototype")))));
  $GLOBALS['shit'] = 3; phiExpressionStatement(new PhiBinaryOperation("@@3", "=", new PhiDot(new PhiDot(new PhiNameRef("Shit"), "prototype"), "constructor"), new PhiNameRef("Shit")));
  $GLOBALS['shit'] = 5; phiExpressionStatement(new PhiFunctionExpression("main", array("args"), function () {
    $GLOBALS['shit'] = 4; phiExpressionStatement(new PhiInvocation(new PhiNameRef("sayShit"), array(new PhiNew(new PhiNameRef("Shit"), array(new PhiStringLiteral("Archibald"), new PhiStringLiteral("Fuck you"))))));
  }));
  $GLOBALS['shit'] = 6; phiExpressionStatement(new PhiFunctionExpression("aaaaaaaaaaa", array(), function () {
  }));
  $GLOBALS['shit'] = 10; phiExpressionStatement(new PhiFunctionExpression("ShitParent", array("a", "b"), function () {
    $GLOBALS['shit'] = 7; phiExpressionStatement(new PhiBinaryOperation("@@4", "=", new PhiDot(new PhiThis(), "a"), new PhiNameRef("a")));
    $GLOBALS['shit'] = 8; phiExpressionStatement(new PhiBinaryOperation("@@5", "=", new PhiDot(new PhiThis(), "b"), new PhiNameRef("b")));
    $GLOBALS['shit'] = 9; phiExpressionStatement(new PhiBinaryOperation("@@6", "=", new PhiDot(new PhiThis(), "prelude"), new PhiStringLiteral("Now I'm really gonna say it...")));
  }));
  $GLOBALS['shit'] = 15; phiExpressionStatement(new PhiBinaryOperation("@@14", "=", new PhiDot(new PhiDot(new PhiNameRef("ShitParent"), "prototype"), "sayIt_61zpoe\$"), new PhiFunctionExpression(null, array("c"), function () {
    $GLOBALS['shit'] = 11; phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@8", "+", new PhiBinaryOperation("@@7", "+", new PhiStringLiteral("a = "), new PhiDot(new PhiThis(), "a")), new PhiStringLiteral("\n")))));
    $GLOBALS['shit'] = 12; phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@10", "+", new PhiBinaryOperation("@@9", "+", new PhiStringLiteral("b = "), new PhiDot(new PhiThis(), "b")), new PhiStringLiteral("\n")))));
    $GLOBALS['shit'] = 13; phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@12", "+", new PhiBinaryOperation("@@11", "+", new PhiStringLiteral("c = "), new PhiNameRef("c")), new PhiStringLiteral("\n")))));
    $GLOBALS['shit'] = 14; phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@13", "+", new PhiDot(new PhiThis(), "prelude"), new PhiStringLiteral("\n")))));
  })));
  $GLOBALS['shit'] = 16; phiExpressionStatement(new PhiBinaryOperation("@@16", "=", new PhiDot(new PhiNameRef("ShitParent"), "\$metadata\$"), new PhiObjectLiteral("@@15", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "CLASS")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("ShitParent")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array()))))));
  $GLOBALS['shit'] = 20; phiExpressionStatement(new PhiFunctionExpression("Shit", array("name", "text"), function () {
    $GLOBALS['shit'] = 17; phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNameRef("ShitParent"), "call"), array(new PhiThis(), new PhiStringLiteral("fucking-a"), new PhiStringLiteral("fucking-b"))));
    $GLOBALS['shit'] = 18; phiExpressionStatement(new PhiBinaryOperation("@@17", "=", new PhiDot(new PhiThis(), "name"), new PhiNameRef("name")));
    $GLOBALS['shit'] = 19; phiExpressionStatement(new PhiBinaryOperation("@@18", "=", new PhiDot(new PhiThis(), "text"), new PhiNameRef("text")));
  }));
  $GLOBALS['shit'] = 23; phiExpressionStatement(new PhiBinaryOperation("@@24", "=", new PhiDot(new PhiDot(new PhiNameRef("Shit"), "prototype"), "sayIt_61zpoe\$"), new PhiFunctionExpression(null, array("c"), function () {
    $GLOBALS['shit'] = 21; phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiDot(new PhiDot(new PhiNameRef("ShitParent"), "prototype"), "sayIt_61zpoe\$"), "call"), array(new PhiThis(), new PhiStringLiteral("fucking-c"))));
    $GLOBALS['shit'] = 22; phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@23", "+", new PhiBinaryOperation("@@22", "+", new PhiBinaryOperation("@@21", "+", new PhiBinaryOperation("@@20", "+", new PhiBinaryOperation("@@19", "+", new PhiStringLiteral("<b>"), new PhiDot(new PhiThis(), "name")), new PhiStringLiteral(", ")), new PhiDot(new PhiThis(), "text")), new PhiNameRef("c")), new PhiStringLiteral("<\/b>")))));
  })));
  $GLOBALS['shit'] = 24; phiExpressionStatement(new PhiBinaryOperation("@@26", "=", new PhiDot(new PhiNameRef("Shit"), "\$metadata\$"), new PhiObjectLiteral("@@25", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "CLASS")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("Shit")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array(new PhiNameRef("ShitParent"))))))));
  $GLOBALS['shit'] = 26; phiExpressionStatement(new PhiFunctionExpression("qwe", array(), function () {
    $GLOBALS['shit'] = 25; phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNew(new PhiNameRef("Shit"), array(new PhiStringLiteral("Archibald"), new PhiStringLiteral("fuck you"))), "sayIt_61zpoe\$"), array(new PhiStringLiteral("!!!!!"))));
  }));
  $GLOBALS['shit'] = 27; phiExpressionStatement(new PhiFunctionExpression("bbbbbbbbbbb", array(), function () {
  }));
  $GLOBALS['shit'] = 29; phiExpressionStatement(new PhiFunctionExpression("sayShit", array("shit"), function () {
    $GLOBALS['shit'] = 28; phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@30", "+", new PhiBinaryOperation("@@29", "+", new PhiBinaryOperation("@@28", "+", new PhiBinaryOperation("@@27", "+", new PhiStringLiteral("<b>"), new PhiDot(new PhiNameRef("shit"), "text")), new PhiStringLiteral(", ")), new PhiDot(new PhiNameRef("shit"), "name")), new PhiStringLiteral("<\/b>")))));
  }));
  $GLOBALS['shit'] = 30; phiExpressionStatement(new PhiFunctionExpression("notNullOnce", array(), function () {
    return new PhiNew(new PhiNameRef("NotNullOnceVar"), array());
  }));
  $GLOBALS['shit'] = 32; phiExpressionStatement(new PhiFunctionExpression("NotNullOnceVar", array(), function () {
    $GLOBALS['shit'] = 31; phiExpressionStatement(new PhiBinaryOperation("@@32", "=", new PhiDot(new PhiThis(), "value_0"), new PhiNullLiteral("@@31")));
  }));
  $GLOBALS['shit'] = 34; phiExpressionStatement(new PhiBinaryOperation("@@38", "=", new PhiDot(new PhiDot(new PhiNameRef("NotNullOnceVar"), "prototype"), "getValue_lrcp0p\$"), new PhiFunctionExpression(null, array("thisRef", "property"), function () {
    phiVars(array(array("tmp\$", )));
    $GLOBALS['shit'] = 33; phiExpressionStatement(new PhiBinaryOperation("@@33", "=", new PhiNameRef("tmp\$"), new PhiDot(new PhiThis(), "value_0")));
    if (phiEvaluateToBoolean(new PhiBinaryOperation("@@35", "==", new PhiNameRef("tmp\$"), new PhiNullLiteral("@@34")))) {
      phiThrow(new PhiNew(new PhiNameRef("IllegalStateException"), array(new PhiBinaryOperation("@@37", "+", new PhiBinaryOperation("@@36", "+", new PhiStringLiteral("Property `"), new PhiDot(new PhiNameRef("property"), "callableName")), new PhiStringLiteral("` should be initialized before get.")))));
    }
    return new PhiNameRef("tmp\$");
  })));
  $GLOBALS['shit'] = 36; phiExpressionStatement(new PhiBinaryOperation("@@44", "=", new PhiDot(new PhiDot(new PhiNameRef("NotNullOnceVar"), "prototype"), "setValue_9rddgb\$"), new PhiFunctionExpression(null, array("thisRef", "property", "value"), function () {
    if (phiEvaluateToBoolean(new PhiUnaryOperation("prefix", "!", new PhiBinaryOperation("@@40", "==", new PhiDot(new PhiThis(), "value_0"), new PhiNullLiteral("@@39"))))) {
      phiVars(array(array("message", new PhiBinaryOperation("@@42", "+", new PhiBinaryOperation("@@41", "+", new PhiStringLiteral("Property `"), new PhiDot(new PhiNameRef("property"), "callableName")), new PhiStringLiteral("` should be assigned only once")))));
      phiThrow(new PhiNew(new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "IllegalStateException"), array(new PhiInvocation(new PhiDot(new PhiNameRef("message"), "toString"), array()))));
    }
    $GLOBALS['shit'] = 35; phiExpressionStatement(new PhiBinaryOperation("@@43", "=", new PhiDot(new PhiThis(), "value_0"), new PhiNameRef("value")));
  })));
  $GLOBALS['shit'] = 37; phiExpressionStatement(new PhiBinaryOperation("@@46", "=", new PhiDot(new PhiNameRef("NotNullOnceVar"), "\$metadata\$"), new PhiObjectLiteral("@@45", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "CLASS")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("NotNullOnceVar")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array(new PhiNameRef("ReadWriteProperty"))))))));
  phiVars(array(array("package\$phi", new PhiBinaryOperation("@@49", "||", new PhiDot(new PhiNameRef("_"), "phi"), new PhiBinaryOperation("@@48", "=", new PhiDot(new PhiNameRef("_"), "phi"), new PhiObjectLiteral("@@47", array()))))));
  phiVars(array(array("package\$gross", new PhiBinaryOperation("@@52", "||", new PhiDot(new PhiNameRef("package\$phi"), "gross"), new PhiBinaryOperation("@@51", "=", new PhiDot(new PhiNameRef("package\$phi"), "gross"), new PhiObjectLiteral("@@50", array()))))));
  phiVars(array(array("package\$test", new PhiBinaryOperation("@@55", "||", new PhiDot(new PhiNameRef("package\$gross"), "test"), new PhiBinaryOperation("@@54", "=", new PhiDot(new PhiNameRef("package\$gross"), "test"), new PhiObjectLiteral("@@53", array()))))));
  phiVars(array(array("package\$one", new PhiBinaryOperation("@@58", "||", new PhiDot(new PhiNameRef("package\$test"), "one"), new PhiBinaryOperation("@@57", "=", new PhiDot(new PhiNameRef("package\$test"), "one"), new PhiObjectLiteral("@@56", array()))))));
  $GLOBALS['shit'] = 38; phiExpressionStatement(new PhiBinaryOperation("@@59", "=", new PhiDot(new PhiNameRef("package\$one"), "main_kand9s\$"), new PhiNameRef("main")));
  $GLOBALS['shit'] = 39; phiExpressionStatement(new PhiBinaryOperation("@@60", "=", new PhiDot(new PhiNameRef("package\$one"), "aaaaaaaaaaa"), new PhiNameRef("aaaaaaaaaaa")));
  $GLOBALS['shit'] = 40; phiExpressionStatement(new PhiBinaryOperation("@@61", "=", new PhiDot(new PhiNameRef("package\$one"), "ShitParent"), new PhiNameRef("ShitParent")));
  $GLOBALS['shit'] = 41; phiExpressionStatement(new PhiBinaryOperation("@@62", "=", new PhiDot(new PhiNameRef("package\$one"), "Shit"), new PhiNameRef("Shit")));
  $GLOBALS['shit'] = 42; phiExpressionStatement(new PhiBinaryOperation("@@63", "=", new PhiDot(new PhiNameRef("package\$one"), "qwe"), new PhiNameRef("qwe")));
  $GLOBALS['shit'] = 43; phiExpressionStatement(new PhiBinaryOperation("@@64", "=", new PhiDot(new PhiNameRef("package\$one"), "bbbbbbbbbbb"), new PhiNameRef("bbbbbbbbbbb")));
  $GLOBALS['shit'] = 44; phiExpressionStatement(new PhiBinaryOperation("@@65", "=", new PhiDot(new PhiNameRef("package\$one"), "notNullOnce_30y1fr\$"), new PhiNameRef("notNullOnce")));
  $GLOBALS['shit'] = 45; phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "defineModule"), array(new PhiStringLiteral("phi-gross-test-1"), new PhiNameRef("_"))));
  $GLOBALS['shit'] = 46; phiExpressionStatement(new PhiInvocation(new PhiNameRef("main"), array(new PhiArrayLiteral(array()))));
  return new PhiNameRef("_");
}), array(new PhiConditional(new PhiBinaryOperation("@@66", "===", new PhiUnaryOperation("prefix", "typeof", new PhiBrackets(new PhiThis(), new PhiStringLiteral("phi-gross-test-1"))), new PhiStringLiteral("undefined")), new PhiObjectLiteral("@@67", array()), new PhiBrackets(new PhiThis(), new PhiStringLiteral("phi-gross-test-1"))), new PhiNameRef("kotlin")))));
