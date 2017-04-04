<?php ; if (phiEvaluateToBoolean(new PhiBinaryOperation("@@2", "===", new PhiUnaryOperation("@@1", "prefix", "typeof", new PhiNameRef("kotlin")), new PhiStringLiteral("undefined")))) {
  phiThrow(new PhiNew(new PhiNameRef("Error"), array(new PhiStringLiteral("Error loading module 'phi-gross-test-1'. Its dependency 'kotlin' was not found. Please, check whether 'kotlin' is loaded prior to 'phi-gross-test-1'."))));
}
$GLOBALS['shit'] = 75; phiExpressionStatement(new PhiBinaryOperation("@@125", "=", new PhiBrackets(new PhiThis(), new PhiStringLiteral("phi-gross-test-1")), new PhiInvocation(new PhiFunctionExpression(null, array("_", "Kotlin"), function () {
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
    $GLOBALS['shit'] = 6; phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiInvocation(new PhiNameRef("plus"), array(new PhiNameRef("x"), new PhiStringLiteral("\n"))))));
  }));
  $GLOBALS['shit'] = 11; phiExpressionStatement(new PhiFunctionExpression("Shit", array("name", "text"), function () {
    $GLOBALS['shit'] = 8; phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNameRef("ShitParent"), "call"), array(new PhiThis(), new PhiStringLiteral("fucking-a"), new PhiStringLiteral("fucking-b"))));
    $GLOBALS['shit'] = 9; phiExpressionStatement(new PhiBinaryOperation("@@9", "=", new PhiDot(new PhiThis(), "name"), new PhiNameRef("name")));
    $GLOBALS['shit'] = 10; phiExpressionStatement(new PhiBinaryOperation("@@10", "=", new PhiDot(new PhiThis(), "text"), new PhiNameRef("text")));
  }));
  $GLOBALS['shit'] = 15; phiExpressionStatement(new PhiFunctionExpression("ShitParent", array("a", "b"), function () {
    $GLOBALS['shit'] = 12; phiExpressionStatement(new PhiBinaryOperation("@@11", "=", new PhiDot(new PhiThis(), "a"), new PhiNameRef("a")));
    $GLOBALS['shit'] = 13; phiExpressionStatement(new PhiBinaryOperation("@@12", "=", new PhiDot(new PhiThis(), "b"), new PhiNameRef("b")));
    $GLOBALS['shit'] = 14; phiExpressionStatement(new PhiBinaryOperation("@@13", "=", new PhiDot(new PhiThis(), "prelude"), new PhiStringLiteral("Now I'm really gonna say it...")));
  }));
  $GLOBALS['shit'] = 17; phiExpressionStatement(new PhiFunctionExpression("test1\$Q_getInstance", array(), function () {
    if (phiEvaluateToBoolean(new PhiBinaryOperation("@@15", "===", new PhiNameRef("test1\$Q_instance"), new PhiNullLiteral("@@14")))) {
      $GLOBALS['shit'] = 16; phiExpressionStatement(new PhiNew(new PhiNameRef("test1\$Q"), array()));
    }
    return phiEvaluate(new PhiNameRef("test1\$Q_instance"));
  }));
  $GLOBALS['shit'] = 20; phiExpressionStatement(new PhiFunctionExpression("test1\$Q", array(), function () {
    $GLOBALS['shit'] = 18; phiExpressionStatement(new PhiBinaryOperation("@@16", "=", new PhiNameRef("test1\$Q_instance"), new PhiThis()));
    $GLOBALS['shit'] = 19; phiExpressionStatement(new PhiBinaryOperation("@@17", "=", new PhiDot(new PhiThis(), "a\$delegate"), new PhiInvocation(new PhiNameRef("notNullOnce"), array())));
  }));
  $GLOBALS['shit'] = 25; phiExpressionStatement(new PhiFunctionExpression("test1", array(), function () {
    $GLOBALS['shit'] = 21; phiExpressionStatement(new PhiBinaryOperation("@@18", "=", new PhiDot(new PhiInvocation(new PhiNameRef("test1\$Q_getInstance"), array()), "a"), new PhiStringLiteral("pizda")));
    $GLOBALS['shit'] = 22; phiExpressionStatement(new PhiInvocation(new PhiNameRef("phiPrintln"), array(new PhiDot(new PhiInvocation(new PhiNameRef("test1\$Q_getInstance"), array()), "a"))));
    $GLOBALS['shit'] = 23; phiExpressionStatement(new PhiBinaryOperation("@@19", "=", new PhiDot(new PhiInvocation(new PhiNameRef("test1\$Q_getInstance"), array()), "a"), new PhiStringLiteral("pizda again")));
    $GLOBALS['shit'] = 24; phiExpressionStatement(new PhiInvocation(new PhiNameRef("phiPrintln"), array(new PhiDot(new PhiInvocation(new PhiNameRef("test1\$Q_getInstance"), array()), "a"))));
  }));
  $GLOBALS['shit'] = 30; phiExpressionStatement(new PhiFunctionExpression("testCheck", array(), function () {
    try {
      if (phiEvaluateToBoolean(new PhiUnaryOperation("@@21", "prefix", "!", new PhiBooleanLiteral("@@20", false)))) {
        phiVars("@@22", array(array("message", new PhiStringLiteral("vagina"))));
        phiThrow(new PhiNew(new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "IllegalStateException"), array(new PhiInvocation(new PhiDot(new PhiNameRef("message"), "toString"), array()))));
      }
    }
     catch (Exception $__phiException) {
      Phi::getCurrentEnv()->setVar('e', $__phiException->phiValue);
      if (phiEvaluateToBoolean(new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "isType"), array(new PhiNameRef("e"), new PhiNameRef("Throwable"))))) {
        $GLOBALS['shit'] = 26; phiExpressionStatement(new PhiInvocation(new PhiNameRef("assertEquals"), array(new PhiStringLiteral("vagina"), new PhiDot(new PhiNameRef("e"), "message"), new PhiStringLiteral("4234fdd2-c2c3-4aad-bbe8-473dde979cd5"))));
        phiVars("@@23", array(array("assertionID", new PhiStringLiteral("7f2510ff-4753-4ea3-8d39-dcfe63ea910a"))));
        if (phiEvaluateToBoolean(new PhiUnaryOperation("@@24", "prefix", "!", new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "isType"), array(new PhiNameRef("e"), new PhiNameRef("NullPointerException")))))) {
          $GLOBALS['shit'] = 27; phiExpressionStatement(new PhiInvocation(new PhiNameRef("fail"), array(new PhiBinaryOperation("@@28", "+", new PhiBinaryOperation("@@27", "+", new PhiBinaryOperation("@@26", "+", new PhiBinaryOperation("@@25", "+", new PhiStringLiteral("assertException failed: "), new PhiNameRef("assertionID")), new PhiStringLiteral("\n")), new PhiStringLiteral("Actual exception: ")), new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "toString"), array(new PhiDot(new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "getKClassFromExpression"), array(new PhiNameRef("e"))), "simpleName")))))));
        }
        $GLOBALS['shit'] = 28; phiExpressionStatement(new PhiInvocation(new PhiNameRef("phiPrintln"), array(new PhiDot(new PhiNameRef("e"), "message"))));
      }
       else
        phiThrow(new PhiNameRef("e"));
    }
    try {
      if (phiEvaluateToBoolean(new PhiUnaryOperation("@@30", "prefix", "!", new PhiBooleanLiteral("@@29", false)))) {
        phiVars("@@31", array(array("message_0", new PhiStringLiteral("boobs"))));
        phiThrow(new PhiNew(new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "IllegalStateException"), array(new PhiInvocation(new PhiDot(new PhiNameRef("message_0"), "toString"), array()))));
      }
    }
     catch (Exception $__phiException) {
      Phi::getCurrentEnv()->setVar('e', $__phiException->phiValue);
      if (phiEvaluateToBoolean(new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "isType"), array(new PhiNameRef("e"), new PhiNameRef("IllegalStateException"))))) {
        $GLOBALS['shit'] = 29; phiExpressionStatement(new PhiInvocation(new PhiNameRef("phiPrintln"), array(new PhiStringLiteral("Well again..."))));
      }
       else
        phiThrow(new PhiNameRef("e"));
    }
  }));
  $GLOBALS['shit'] = 31; phiExpressionStatement(new PhiFunctionExpression("insideLines", array("s"), function () {
    phiVars("@@32", array(array("line", new PhiStringLiteral("----------------------------------------------------------\n"))));
    return phiEvaluate(new PhiBinaryOperation("@@34", "+", new PhiBinaryOperation("@@33", "+", new PhiStringLiteral("\n----------------------------------------------------------\n"), new PhiNameRef("s")), new PhiStringLiteral("\n----------------------------------------------------------\n")));
  }));
  $GLOBALS['shit'] = 32; phiExpressionStatement(new PhiFunctionExpression("fail", array("msg"), function () {
    phiThrow(new PhiNew(new PhiNameRef("AssertionError"), array(new PhiInvocation(new PhiNameRef("insideLines"), array(new PhiNameRef("msg"))))));
  }));
  $GLOBALS['shit'] = 34; phiExpressionStatement(new PhiFunctionExpression("assertEquals", array("expected", "actual", "assertionID"), function () {
    if (phiEvaluateToBoolean(new PhiUnaryOperation("@@35", "prefix", "!", new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "equals"), array(new PhiNameRef("expected"), new PhiNameRef("actual")))))) {
      $GLOBALS['shit'] = 33; phiExpressionStatement(new PhiInvocation(new PhiNameRef("fail"), array(new PhiBinaryOperation("@@42", "+", new PhiBinaryOperation("@@41", "+", new PhiBinaryOperation("@@40", "+", new PhiBinaryOperation("@@39", "+", new PhiBinaryOperation("@@38", "+", new PhiBinaryOperation("@@37", "+", new PhiBinaryOperation("@@36", "+", new PhiStringLiteral("assertEquals failed: "), new PhiNameRef("assertionID")), new PhiStringLiteral("\n")), new PhiStringLiteral("Expected: ")), new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "toString"), array(new PhiNameRef("expected")))), new PhiStringLiteral("\n")), new PhiStringLiteral("Actual: ")), new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "toString"), array(new PhiNameRef("actual")))))));
    }
  }));
  $GLOBALS['shit'] = 36; phiExpressionStatement(new PhiFunctionExpression("assertTrue", array("b", "assertionID"), function () {
    if (phiEvaluateToBoolean(new PhiUnaryOperation("@@43", "prefix", "!", new PhiNameRef("b")))) {
      $GLOBALS['shit'] = 35; phiExpressionStatement(new PhiInvocation(new PhiNameRef("fail"), array(new PhiBinaryOperation("@@44", "+", new PhiStringLiteral("assertTrue failed: "), new PhiNameRef("assertionID")))));
    }
  }));
  $GLOBALS['shit'] = 40; phiExpressionStatement(new PhiFunctionExpression("main", array("args"), function () {
    $GLOBALS['shit'] = 37; phiExpressionStatement(new PhiInvocation(new PhiNameRef("sayShit"), array(new PhiNew(new PhiNameRef("Shit"), array(new PhiStringLiteral("Archibald"), new PhiStringLiteral("Fuck you"))))));
    $GLOBALS['shit'] = 38; phiExpressionStatement(new PhiInvocation(new PhiNameRef("testCheck"), array()));
    $GLOBALS['shit'] = 39; phiExpressionStatement(new PhiNew(new PhiNameRef("test1"), array()));
  }));
  $GLOBALS['shit'] = 41; phiExpressionStatement(new PhiStringLiteral("use strict"));
  phiVars("@@45", array(array("AssertionError", new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "AssertionError"))));
  phiVars("@@46", array(array("NullPointerException", new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "NullPointerException"))));
  phiVars("@@47", array(array("Throwable", new PhiNameRef("Error"))));
  phiVars("@@48", array(array("IllegalStateException", new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "IllegalStateException"))));
  phiVars("@@49", array(array("plus", new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "plus_cmbeuq\$"))));
  phiVars("@@50", array(array("ReadWriteProperty", new PhiDot(new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "properties"), "ReadWriteProperty"))));
  $GLOBALS['shit'] = 42; phiExpressionStatement(new PhiBinaryOperation("@@51", "=", new PhiDot(new PhiNameRef("Shit"), "prototype"), new PhiInvocation(new PhiDot(new PhiNameRef("Object"), "create"), array(new PhiDot(new PhiNameRef("ShitParent"), "prototype")))));
  $GLOBALS['shit'] = 43; phiExpressionStatement(new PhiBinaryOperation("@@52", "=", new PhiDot(new PhiDot(new PhiNameRef("Shit"), "prototype"), "constructor"), new PhiNameRef("Shit")));
  $GLOBALS['shit'] = 45; phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNameRef("Object"), "defineProperty"), array(new PhiDot(new PhiNameRef("test1\$Q"), "prototype"), new PhiStringLiteral("a"), new PhiObjectLiteral("@@53", array(array(new PhiStringLiteral("get"), new PhiFunctionExpression(null, array(), function () {
    return phiEvaluate(new PhiInvocation(new PhiDot(new PhiDot(new PhiThis(), "a\$delegate"), "getValue_lrcp0p\$"), array(new PhiThis(), new PhiNew(new PhiDot(new PhiNameRef("Kotlin"), "PropertyMetadata"), array(new PhiStringLiteral("a"))))));
  })), array(new PhiStringLiteral("set"), new PhiFunctionExpression(null, array("a"), function () {
    $GLOBALS['shit'] = 44; phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiDot(new PhiNew(new PhiNameRef("PhiThis"), array()), "a\$delegate"), "setValue_9rddgb\$"), array(new PhiThis(), new PhiNew(new PhiDot(new PhiNameRef("Kotlin"), "PropertyMetadata"), array(new PhiStringLiteral("a"))), new PhiNameRef("a"))));
  })))))));
  $GLOBALS['shit'] = 46; phiExpressionStatement(new PhiBinaryOperation("@@55", "=", new PhiDot(new PhiNameRef("test1\$Q"), "\$metadata\$"), new PhiObjectLiteral("@@54", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "OBJECT")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("Q")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array()))))));
  phiVars("@@57", array(array("test1\$Q_instance", new PhiNullLiteral("@@56"))));
  $GLOBALS['shit'] = 47; phiExpressionStatement(new PhiBinaryOperation("@@59", "=", new PhiDot(new PhiNameRef("test1"), "\$metadata\$"), new PhiObjectLiteral("@@58", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "CLASS")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("test1")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array()))))));
  $GLOBALS['shit'] = 52; phiExpressionStatement(new PhiBinaryOperation("@@67", "=", new PhiDot(new PhiDot(new PhiNameRef("ShitParent"), "prototype"), "sayIt_61zpoe\$"), new PhiFunctionExpression(null, array("c"), function () {
    $GLOBALS['shit'] = 48; phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@61", "+", new PhiBinaryOperation("@@60", "+", new PhiStringLiteral("a = "), new PhiDot(new PhiThis(), "a")), new PhiStringLiteral("\n")))));
    $GLOBALS['shit'] = 49; phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@63", "+", new PhiBinaryOperation("@@62", "+", new PhiStringLiteral("b = "), new PhiDot(new PhiThis(), "b")), new PhiStringLiteral("\n")))));
    $GLOBALS['shit'] = 50; phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@65", "+", new PhiBinaryOperation("@@64", "+", new PhiStringLiteral("c = "), new PhiNameRef("c")), new PhiStringLiteral("\n")))));
    $GLOBALS['shit'] = 51; phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@66", "+", new PhiDot(new PhiThis(), "prelude"), new PhiStringLiteral("\n")))));
  })));
  $GLOBALS['shit'] = 53; phiExpressionStatement(new PhiBinaryOperation("@@69", "=", new PhiDot(new PhiNameRef("ShitParent"), "\$metadata\$"), new PhiObjectLiteral("@@68", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "CLASS")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("ShitParent")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array()))))));
  $GLOBALS['shit'] = 56; phiExpressionStatement(new PhiBinaryOperation("@@75", "=", new PhiDot(new PhiDot(new PhiNameRef("Shit"), "prototype"), "sayIt_61zpoe\$"), new PhiFunctionExpression(null, array("c"), function () {
    $GLOBALS['shit'] = 54; phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiDot(new PhiDot(new PhiNameRef("ShitParent"), "prototype"), "sayIt_61zpoe\$"), "call"), array(new PhiThis(), new PhiStringLiteral("fucking-c"))));
    $GLOBALS['shit'] = 55; phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@74", "+", new PhiBinaryOperation("@@73", "+", new PhiBinaryOperation("@@72", "+", new PhiBinaryOperation("@@71", "+", new PhiBinaryOperation("@@70", "+", new PhiStringLiteral("<b>"), new PhiDot(new PhiThis(), "name")), new PhiStringLiteral(", ")), new PhiDot(new PhiThis(), "text")), new PhiNameRef("c")), new PhiStringLiteral("<\/b>")))));
  })));
  $GLOBALS['shit'] = 57; phiExpressionStatement(new PhiBinaryOperation("@@77", "=", new PhiDot(new PhiNameRef("Shit"), "\$metadata\$"), new PhiObjectLiteral("@@76", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "CLASS")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("Shit")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array(new PhiNameRef("ShitParent"))))))));
  $GLOBALS['shit'] = 59; phiExpressionStatement(new PhiBinaryOperation("@@85", "=", new PhiDot(new PhiDot(new PhiNameRef("NotNullOnceVar"), "prototype"), "getValue_lrcp0p\$"), new PhiFunctionExpression(null, array("thisRef", "property"), function () {
    phiVars("@@79", array(array("tmp\$", new PhiUnaryOperation("@@78", "prefix", "void", new PhiNumberLiteral("@@something", 0)))));
    $GLOBALS['shit'] = 58; phiExpressionStatement(new PhiBinaryOperation("@@80", "=", new PhiNameRef("tmp\$"), new PhiDot(new PhiThis(), "value_0")));
    if (phiEvaluateToBoolean(new PhiBinaryOperation("@@82", "==", new PhiNameRef("tmp\$"), new PhiNullLiteral("@@81")))) {
      phiThrow(new PhiNew(new PhiNameRef("IllegalStateException"), array(new PhiBinaryOperation("@@84", "+", new PhiBinaryOperation("@@83", "+", new PhiStringLiteral("Property `"), new PhiDot(new PhiNameRef("property"), "callableName")), new PhiStringLiteral("` should be initialized before get.")))));
    }
    return phiEvaluate(new PhiNameRef("tmp\$"));
  })));
  $GLOBALS['shit'] = 61; phiExpressionStatement(new PhiBinaryOperation("@@93", "=", new PhiDot(new PhiDot(new PhiNameRef("NotNullOnceVar"), "prototype"), "setValue_9rddgb\$"), new PhiFunctionExpression(null, array("thisRef", "property", "value"), function () {
    if (phiEvaluateToBoolean(new PhiUnaryOperation("@@88", "prefix", "!", new PhiBinaryOperation("@@87", "==", new PhiDot(new PhiThis(), "value_0"), new PhiNullLiteral("@@86"))))) {
      phiVars("@@91", array(array("message", new PhiBinaryOperation("@@90", "+", new PhiBinaryOperation("@@89", "+", new PhiStringLiteral("Property `"), new PhiDot(new PhiNameRef("property"), "callableName")), new PhiStringLiteral("` should be assigned only once")))));
      phiThrow(new PhiNew(new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "IllegalStateException"), array(new PhiInvocation(new PhiDot(new PhiNameRef("message"), "toString"), array()))));
    }
    $GLOBALS['shit'] = 60; phiExpressionStatement(new PhiBinaryOperation("@@92", "=", new PhiDot(new PhiThis(), "value_0"), new PhiNameRef("value")));
  })));
  $GLOBALS['shit'] = 62; phiExpressionStatement(new PhiBinaryOperation("@@95", "=", new PhiDot(new PhiNameRef("NotNullOnceVar"), "\$metadata\$"), new PhiObjectLiteral("@@94", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "CLASS")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("NotNullOnceVar")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array(new PhiNameRef("ReadWriteProperty"))))))));
  phiVars("@@99", array(array("package\$phi", new PhiBinaryOperation("@@98", "||", new PhiDot(new PhiNameRef("_"), "phi"), new PhiBinaryOperation("@@97", "=", new PhiDot(new PhiNameRef("_"), "phi"), new PhiObjectLiteral("@@96", array()))))));
  phiVars("@@103", array(array("package\$gross", new PhiBinaryOperation("@@102", "||", new PhiDot(new PhiNameRef("package\$phi"), "gross"), new PhiBinaryOperation("@@101", "=", new PhiDot(new PhiNameRef("package\$phi"), "gross"), new PhiObjectLiteral("@@100", array()))))));
  phiVars("@@107", array(array("package\$test", new PhiBinaryOperation("@@106", "||", new PhiDot(new PhiNameRef("package\$gross"), "test"), new PhiBinaryOperation("@@105", "=", new PhiDot(new PhiNameRef("package\$gross"), "test"), new PhiObjectLiteral("@@104", array()))))));
  phiVars("@@111", array(array("package\$one", new PhiBinaryOperation("@@110", "||", new PhiDot(new PhiNameRef("package\$test"), "one"), new PhiBinaryOperation("@@109", "=", new PhiDot(new PhiNameRef("package\$test"), "one"), new PhiObjectLiteral("@@108", array()))))));
  $GLOBALS['shit'] = 63; phiExpressionStatement(new PhiBinaryOperation("@@112", "=", new PhiDot(new PhiNameRef("package\$one"), "main_kand9s\$"), new PhiNameRef("main")));
  $GLOBALS['shit'] = 64; phiExpressionStatement(new PhiBinaryOperation("@@113", "=", new PhiDot(new PhiNameRef("package\$one"), "assertTrue_8kj6y5\$"), new PhiNameRef("assertTrue")));
  $GLOBALS['shit'] = 65; phiExpressionStatement(new PhiBinaryOperation("@@114", "=", new PhiDot(new PhiNameRef("package\$one"), "assertEquals_d0xna2\$"), new PhiNameRef("assertEquals")));
  $GLOBALS['shit'] = 66; phiExpressionStatement(new PhiBinaryOperation("@@115", "=", new PhiDot(new PhiNameRef("package\$one"), "testCheck"), new PhiNameRef("testCheck")));
  $GLOBALS['shit'] = 67; phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNameRef("Object"), "defineProperty"), array(new PhiNameRef("test1"), new PhiStringLiteral("Q"), new PhiObjectLiteral("@@116", array(array(new PhiNameRef("get"), new PhiNameRef("test1\$Q_getInstance")))))));
  $GLOBALS['shit'] = 68; phiExpressionStatement(new PhiBinaryOperation("@@117", "=", new PhiDot(new PhiNameRef("package\$one"), "test1"), new PhiNameRef("test1")));
  $GLOBALS['shit'] = 69; phiExpressionStatement(new PhiBinaryOperation("@@118", "=", new PhiDot(new PhiNameRef("package\$one"), "ShitParent"), new PhiNameRef("ShitParent")));
  $GLOBALS['shit'] = 70; phiExpressionStatement(new PhiBinaryOperation("@@119", "=", new PhiDot(new PhiNameRef("package\$one"), "Shit"), new PhiNameRef("Shit")));
  $GLOBALS['shit'] = 71; phiExpressionStatement(new PhiBinaryOperation("@@120", "=", new PhiDot(new PhiNameRef("package\$one"), "phiPrintln_pdl1vj\$"), new PhiNameRef("phiPrintln")));
  $GLOBALS['shit'] = 72; phiExpressionStatement(new PhiBinaryOperation("@@121", "=", new PhiDot(new PhiNameRef("package\$one"), "notNullOnce_30y1fr\$"), new PhiNameRef("notNullOnce")));
  $GLOBALS['shit'] = 73; phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "defineModule"), array(new PhiStringLiteral("phi-gross-test-1"), new PhiNameRef("_"))));
  $GLOBALS['shit'] = 74; phiExpressionStatement(new PhiInvocation(new PhiNameRef("main"), array(new PhiArrayLiteral(array()))));
  return phiEvaluate(new PhiNameRef("_"));
}), array(new PhiConditional(new PhiBinaryOperation("@@123", "===", new PhiUnaryOperation("@@122", "prefix", "typeof", new PhiBrackets(new PhiThis(), new PhiStringLiteral("phi-gross-test-1"))), new PhiStringLiteral("undefined")), new PhiObjectLiteral("@@124", array()), new PhiBrackets(new PhiThis(), new PhiStringLiteral("phi-gross-test-1"))), new PhiNameRef("kotlin")))));
