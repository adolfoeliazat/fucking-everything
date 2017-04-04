<?php ; if (phiEvaluateToBoolean(new PhiBinaryOperation("@@2", "===", new PhiUnaryOperation("@@1", "prefix", "typeof", new PhiNameRef("kotlin")), new PhiStringLiteral("undefined")))) {
  phiThrow(new PhiNew(new PhiNameRef("Error"), array(new PhiStringLiteral("Error loading module 'phi-gross-test-1'. Its dependency 'kotlin' was not found. Please, check whether 'kotlin' is loaded prior to 'phi-gross-test-1'."))));
}
phiExpressionStatement(new PhiBinaryOperation("@@123", "=", new PhiBrackets(new PhiThis(), new PhiStringLiteral("phi-gross-test-1")), new PhiInvocation(new PhiFunctionExpression(null, array("_", "Kotlin"), function () {
  phiExpressionStatement(new PhiFunctionExpression("NotNullOnceVar", array(), function () {
    phiExpressionStatement(new PhiBinaryOperation("@@4", "=", new PhiDot(new PhiThis(), "value_0"), new PhiNullLiteral("@@3")));
  }));
  phiExpressionStatement(new PhiFunctionExpression("notNullOnce", array(), function () {
    return phiEvaluate(new PhiNew(new PhiNameRef("NotNullOnceVar"), array()));
  }));
  phiExpressionStatement(new PhiFunctionExpression("sayShit", array("shit"), function () {
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("phiPrintln"), array(new PhiBinaryOperation("@@8", "+", new PhiBinaryOperation("@@7", "+", new PhiBinaryOperation("@@6", "+", new PhiBinaryOperation("@@5", "+", new PhiStringLiteral("<b>"), new PhiDot(new PhiNameRef("shit"), "text")), new PhiStringLiteral(", ")), new PhiDot(new PhiNameRef("shit"), "name")), new PhiStringLiteral("<\/b>")))));
  }));
  phiExpressionStatement(new PhiFunctionExpression("phiPrintln", array("x"), function () {
    phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiInvocation(new PhiNameRef("plus"), array(new PhiNameRef("x"), new PhiStringLiteral("\n"))))));
  }));
  phiExpressionStatement(new PhiFunctionExpression("Shit", array("name", "text"), function () {
    phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNameRef("ShitParent"), "call"), array(new PhiThis(), new PhiStringLiteral("fucking-a"), new PhiStringLiteral("fucking-b"))));
    phiExpressionStatement(new PhiBinaryOperation("@@9", "=", new PhiDot(new PhiThis(), "name"), new PhiNameRef("name")));
    phiExpressionStatement(new PhiBinaryOperation("@@10", "=", new PhiDot(new PhiThis(), "text"), new PhiNameRef("text")));
  }));
  phiExpressionStatement(new PhiFunctionExpression("ShitParent", array("a", "b"), function () {
    phiExpressionStatement(new PhiBinaryOperation("@@11", "=", new PhiDot(new PhiThis(), "a"), new PhiNameRef("a")));
    phiExpressionStatement(new PhiBinaryOperation("@@12", "=", new PhiDot(new PhiThis(), "b"), new PhiNameRef("b")));
    phiExpressionStatement(new PhiBinaryOperation("@@13", "=", new PhiDot(new PhiThis(), "prelude"), new PhiStringLiteral("Now I'm really gonna say it...")));
  }));
  phiExpressionStatement(new PhiFunctionExpression("test1\$Q_getInstance", array(), function () {
    if (phiEvaluateToBoolean(new PhiBinaryOperation("@@15", "===", new PhiNameRef("test1\$Q_instance"), new PhiNullLiteral("@@14")))) {
      phiExpressionStatement(new PhiNew(new PhiNameRef("test1\$Q"), array()));
    }
    return phiEvaluate(new PhiNameRef("test1\$Q_instance"));
  }));
  phiExpressionStatement(new PhiFunctionExpression("test1\$Q", array(), function () {
    phiExpressionStatement(new PhiBinaryOperation("@@16", "=", new PhiNameRef("test1\$Q_instance"), new PhiThis()));
    phiExpressionStatement(new PhiBinaryOperation("@@17", "=", new PhiDot(new PhiThis(), "a\$delegate"), new PhiInvocation(new PhiNameRef("notNullOnce"), array())));
  }));
  phiExpressionStatement(new PhiFunctionExpression("test1", array(), function () {
    phiExpressionStatement(new PhiBinaryOperation("@@18", "=", new PhiDot(new PhiInvocation(new PhiNameRef("test1\$Q_getInstance"), array()), "a"), new PhiStringLiteral("pizda")));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("phiPrintln"), array(new PhiDot(new PhiInvocation(new PhiNameRef("test1\$Q_getInstance"), array()), "a"))));
    phiExpressionStatement(new PhiBinaryOperation("@@19", "=", new PhiDot(new PhiInvocation(new PhiNameRef("test1\$Q_getInstance"), array()), "a"), new PhiStringLiteral("pizda again")));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("phiPrintln"), array(new PhiDot(new PhiInvocation(new PhiNameRef("test1\$Q_getInstance"), array()), "a"))));
  }));
  phiExpressionStatement(new PhiFunctionExpression("testCheck", array(), function () {
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("assertException"), array(new PhiNameRef("testCheck\$lambda\$lambda"), new PhiStringLiteral("vagina"), new PhiStringLiteral("7f2510ff-4753-4ea3-8d39-dcfe63ea910a"), new PhiNameRef("testCheck\$lambda\$lambda_0"))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("phiPrintln"), array(new PhiStringLiteral("testCheck: PASSED"))));
  }));
  phiExpressionStatement(new PhiFunctionExpression("testCheck\$lambda\$lambda_0", array(), function () {
    if (phiEvaluateToBoolean(new PhiUnaryOperation("@@21", "prefix", "!", new PhiBooleanLiteral("@@20", false)))) {
      phiVars("@@22", array(array("message", new PhiStringLiteral("vagina"))));
      phiThrow(new PhiNew(new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "IllegalStateException"), array(new PhiInvocation(new PhiDot(new PhiNameRef("message"), "toString"), array()))));
    }
  }));
  phiExpressionStatement(new PhiFunctionExpression("testCheck\$lambda\$lambda", array("it"), function () {
    return phiEvaluate(new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "isType"), array(new PhiNameRef("it"), new PhiNameRef("IllegalStateException"))));
  }));
  phiExpressionStatement(new PhiFunctionExpression("assertException", array("testType", "expectedMessage", "assertionID", "block"), function () {
    try {
      phiExpressionStatement(new PhiInvocation(new PhiNameRef("block"), array()));
    }
     catch (Exception $__phiException) {
      Phi::getCurrentEnv()->setVar('e', $__phiException->phiValue);
      if (phiEvaluateToBoolean(new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "isType"), array(new PhiNameRef("e"), new PhiNameRef("Throwable"))))) {
        phiVars("@@23", array(array("qwe", new PhiInvocation(new PhiNameRef("testType"), array(new PhiNameRef("e"))))));
        if (phiEvaluateToBoolean(new PhiUnaryOperation("@@24", "prefix", "!", new PhiNameRef("qwe"))))
          phiExpressionStatement(new PhiInvocation(new PhiNameRef("fail"), array(new PhiBinaryOperation("@@28", "+", new PhiBinaryOperation("@@27", "+", new PhiBinaryOperation("@@26", "+", new PhiBinaryOperation("@@25", "+", new PhiStringLiteral("assertException failed: "), new PhiNameRef("assertionID")), new PhiStringLiteral("\n")), new PhiStringLiteral("Actual exception: ")), new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "toString"), array(new PhiDot(new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "getKClassFromExpression"), array(new PhiNameRef("e"))), "simpleName")))))));
        phiExpressionStatement(new PhiInvocation(new PhiNameRef("assertEquals"), array(new PhiNameRef("expectedMessage"), new PhiDot(new PhiNameRef("e"), "message"), new PhiBinaryOperation("@@29", "+", new PhiStringLiteral("message-"), new PhiNameRef("assertionID")))));
      }
       else
        phiThrow(new PhiNameRef("e"));
    }
  }));
  phiExpressionStatement(new PhiFunctionExpression("insideLines", array("s"), function () {
    phiVars("@@30", array(array("line", new PhiStringLiteral("----------------------------------------------------------\n"))));
    return phiEvaluate(new PhiBinaryOperation("@@32", "+", new PhiBinaryOperation("@@31", "+", new PhiStringLiteral("\n----------------------------------------------------------\n"), new PhiNameRef("s")), new PhiStringLiteral("\n----------------------------------------------------------\n")));
  }));
  phiExpressionStatement(new PhiFunctionExpression("fail", array("msg"), function () {
    phiThrow(new PhiNew(new PhiNameRef("AssertionError"), array(new PhiInvocation(new PhiNameRef("insideLines"), array(new PhiNameRef("msg"))))));
  }));
  phiExpressionStatement(new PhiFunctionExpression("assertEquals", array("expected", "actual", "assertionID"), function () {
    if (phiEvaluateToBoolean(new PhiUnaryOperation("@@33", "prefix", "!", new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "equals"), array(new PhiNameRef("expected"), new PhiNameRef("actual")))))) {
      phiExpressionStatement(new PhiInvocation(new PhiNameRef("fail"), array(new PhiBinaryOperation("@@40", "+", new PhiBinaryOperation("@@39", "+", new PhiBinaryOperation("@@38", "+", new PhiBinaryOperation("@@37", "+", new PhiBinaryOperation("@@36", "+", new PhiBinaryOperation("@@35", "+", new PhiBinaryOperation("@@34", "+", new PhiStringLiteral("assertEquals failed: "), new PhiNameRef("assertionID")), new PhiStringLiteral("\n")), new PhiStringLiteral("Expected: ")), new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "toString"), array(new PhiNameRef("expected")))), new PhiStringLiteral("\n")), new PhiStringLiteral("Actual: ")), new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "toString"), array(new PhiNameRef("actual")))))));
    }
  }));
  phiExpressionStatement(new PhiFunctionExpression("assertTrue", array("b", "assertionID"), function () {
    if (phiEvaluateToBoolean(new PhiUnaryOperation("@@41", "prefix", "!", new PhiNameRef("b")))) {
      phiExpressionStatement(new PhiInvocation(new PhiNameRef("fail"), array(new PhiBinaryOperation("@@42", "+", new PhiStringLiteral("assertTrue failed: "), new PhiNameRef("assertionID")))));
    }
  }));
  phiExpressionStatement(new PhiFunctionExpression("main", array("args"), function () {
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("sayShit"), array(new PhiNew(new PhiNameRef("Shit"), array(new PhiStringLiteral("Archibald"), new PhiStringLiteral("Fuck you"))))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("testCheck"), array()));
    phiExpressionStatement(new PhiNew(new PhiNameRef("test1"), array()));
  }));
  phiExpressionStatement(new PhiStringLiteral("use strict"));
  phiVars("@@43", array(array("AssertionError", new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "AssertionError"))));
  phiVars("@@44", array(array("Throwable", new PhiNameRef("Error"))));
  phiVars("@@45", array(array("IllegalStateException", new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "IllegalStateException"))));
  phiVars("@@46", array(array("plus", new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "plus_cmbeuq\$"))));
  phiVars("@@47", array(array("ReadWriteProperty", new PhiDot(new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "properties"), "ReadWriteProperty"))));
  phiExpressionStatement(new PhiBinaryOperation("@@48", "=", new PhiDot(new PhiNameRef("Shit"), "prototype"), new PhiInvocation(new PhiDot(new PhiNameRef("Object"), "create"), array(new PhiDot(new PhiNameRef("ShitParent"), "prototype")))));
  phiExpressionStatement(new PhiBinaryOperation("@@49", "=", new PhiDot(new PhiDot(new PhiNameRef("Shit"), "prototype"), "constructor"), new PhiNameRef("Shit")));
  phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNameRef("Object"), "defineProperty"), array(new PhiDot(new PhiNameRef("test1\$Q"), "prototype"), new PhiStringLiteral("a"), new PhiObjectLiteral("@@50", array(array(new PhiStringLiteral("get"), new PhiFunctionExpression(null, array(), function () {
    return phiEvaluate(new PhiInvocation(new PhiDot(new PhiDot(new PhiThis(), "a\$delegate"), "getValue_lrcp0p\$"), array(new PhiThis(), new PhiNew(new PhiDot(new PhiNameRef("Kotlin"), "PropertyMetadata"), array(new PhiStringLiteral("a"))))));
  })), array(new PhiStringLiteral("set"), new PhiFunctionExpression(null, array("a"), function () {
    phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiDot(new PhiNew(new PhiNameRef("PhiThis"), array()), "a\$delegate"), "setValue_9rddgb\$"), array(new PhiThis(), new PhiNew(new PhiDot(new PhiNameRef("Kotlin"), "PropertyMetadata"), array(new PhiStringLiteral("a"))), new PhiNameRef("a"))));
  })))))));
  phiExpressionStatement(new PhiBinaryOperation("@@52", "=", new PhiDot(new PhiNameRef("test1\$Q"), "\$metadata\$"), new PhiObjectLiteral("@@51", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "OBJECT")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("Q")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array()))))));
  phiVars("@@54", array(array("test1\$Q_instance", new PhiNullLiteral("@@53"))));
  phiExpressionStatement(new PhiBinaryOperation("@@56", "=", new PhiDot(new PhiNameRef("test1"), "\$metadata\$"), new PhiObjectLiteral("@@55", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "CLASS")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("test1")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array()))))));
  phiExpressionStatement(new PhiBinaryOperation("@@64", "=", new PhiDot(new PhiDot(new PhiNameRef("ShitParent"), "prototype"), "sayIt_61zpoe\$"), new PhiFunctionExpression(null, array("c"), function () {
    phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@58", "+", new PhiBinaryOperation("@@57", "+", new PhiStringLiteral("a = "), new PhiDot(new PhiThis(), "a")), new PhiStringLiteral("\n")))));
    phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@60", "+", new PhiBinaryOperation("@@59", "+", new PhiStringLiteral("b = "), new PhiDot(new PhiThis(), "b")), new PhiStringLiteral("\n")))));
    phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@62", "+", new PhiBinaryOperation("@@61", "+", new PhiStringLiteral("c = "), new PhiNameRef("c")), new PhiStringLiteral("\n")))));
    phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@63", "+", new PhiDot(new PhiThis(), "prelude"), new PhiStringLiteral("\n")))));
  })));
  phiExpressionStatement(new PhiBinaryOperation("@@66", "=", new PhiDot(new PhiNameRef("ShitParent"), "\$metadata\$"), new PhiObjectLiteral("@@65", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "CLASS")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("ShitParent")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array()))))));
  phiExpressionStatement(new PhiBinaryOperation("@@72", "=", new PhiDot(new PhiDot(new PhiNameRef("Shit"), "prototype"), "sayIt_61zpoe\$"), new PhiFunctionExpression(null, array("c"), function () {
    phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiDot(new PhiDot(new PhiNameRef("ShitParent"), "prototype"), "sayIt_61zpoe\$"), "call"), array(new PhiThis(), new PhiStringLiteral("fucking-c"))));
    phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@71", "+", new PhiBinaryOperation("@@70", "+", new PhiBinaryOperation("@@69", "+", new PhiBinaryOperation("@@68", "+", new PhiBinaryOperation("@@67", "+", new PhiStringLiteral("<b>"), new PhiDot(new PhiThis(), "name")), new PhiStringLiteral(", ")), new PhiDot(new PhiThis(), "text")), new PhiNameRef("c")), new PhiStringLiteral("<\/b>")))));
  })));
  phiExpressionStatement(new PhiBinaryOperation("@@74", "=", new PhiDot(new PhiNameRef("Shit"), "\$metadata\$"), new PhiObjectLiteral("@@73", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "CLASS")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("Shit")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array(new PhiNameRef("ShitParent"))))))));
  phiExpressionStatement(new PhiBinaryOperation("@@82", "=", new PhiDot(new PhiDot(new PhiNameRef("NotNullOnceVar"), "prototype"), "getValue_lrcp0p\$"), new PhiFunctionExpression(null, array("thisRef", "property"), function () {
    phiVars("@@76", array(array("tmp\$", new PhiUnaryOperation("@@75", "prefix", "void", new PhiNumberLiteral("@@something", 0)))));
    phiExpressionStatement(new PhiBinaryOperation("@@77", "=", new PhiNameRef("tmp\$"), new PhiDot(new PhiThis(), "value_0")));
    if (phiEvaluateToBoolean(new PhiBinaryOperation("@@79", "==", new PhiNameRef("tmp\$"), new PhiNullLiteral("@@78")))) {
      phiThrow(new PhiNew(new PhiNameRef("IllegalStateException"), array(new PhiBinaryOperation("@@81", "+", new PhiBinaryOperation("@@80", "+", new PhiStringLiteral("Property `"), new PhiDot(new PhiNameRef("property"), "callableName")), new PhiStringLiteral("` should be initialized before get.")))));
    }
    return phiEvaluate(new PhiNameRef("tmp\$"));
  })));
  phiExpressionStatement(new PhiBinaryOperation("@@90", "=", new PhiDot(new PhiDot(new PhiNameRef("NotNullOnceVar"), "prototype"), "setValue_9rddgb\$"), new PhiFunctionExpression(null, array("thisRef", "property", "value"), function () {
    if (phiEvaluateToBoolean(new PhiUnaryOperation("@@85", "prefix", "!", new PhiBinaryOperation("@@84", "==", new PhiDot(new PhiThis(), "value_0"), new PhiNullLiteral("@@83"))))) {
      phiVars("@@88", array(array("message", new PhiBinaryOperation("@@87", "+", new PhiBinaryOperation("@@86", "+", new PhiStringLiteral("Property `"), new PhiDot(new PhiNameRef("property"), "callableName")), new PhiStringLiteral("` should be assigned only once")))));
      phiThrow(new PhiNew(new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "IllegalStateException"), array(new PhiInvocation(new PhiDot(new PhiNameRef("message"), "toString"), array()))));
    }
    phiExpressionStatement(new PhiBinaryOperation("@@89", "=", new PhiDot(new PhiThis(), "value_0"), new PhiNameRef("value")));
  })));
  phiExpressionStatement(new PhiBinaryOperation("@@92", "=", new PhiDot(new PhiNameRef("NotNullOnceVar"), "\$metadata\$"), new PhiObjectLiteral("@@91", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "CLASS")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("NotNullOnceVar")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array(new PhiNameRef("ReadWriteProperty"))))))));
  phiVars("@@96", array(array("package\$phi", new PhiBinaryOperation("@@95", "||", new PhiDot(new PhiNameRef("_"), "phi"), new PhiBinaryOperation("@@94", "=", new PhiDot(new PhiNameRef("_"), "phi"), new PhiObjectLiteral("@@93", array()))))));
  phiVars("@@100", array(array("package\$gross", new PhiBinaryOperation("@@99", "||", new PhiDot(new PhiNameRef("package\$phi"), "gross"), new PhiBinaryOperation("@@98", "=", new PhiDot(new PhiNameRef("package\$phi"), "gross"), new PhiObjectLiteral("@@97", array()))))));
  phiVars("@@104", array(array("package\$test", new PhiBinaryOperation("@@103", "||", new PhiDot(new PhiNameRef("package\$gross"), "test"), new PhiBinaryOperation("@@102", "=", new PhiDot(new PhiNameRef("package\$gross"), "test"), new PhiObjectLiteral("@@101", array()))))));
  phiVars("@@108", array(array("package\$one", new PhiBinaryOperation("@@107", "||", new PhiDot(new PhiNameRef("package\$test"), "one"), new PhiBinaryOperation("@@106", "=", new PhiDot(new PhiNameRef("package\$test"), "one"), new PhiObjectLiteral("@@105", array()))))));
  phiExpressionStatement(new PhiBinaryOperation("@@109", "=", new PhiDot(new PhiNameRef("package\$one"), "main_kand9s\$"), new PhiNameRef("main")));
  phiExpressionStatement(new PhiBinaryOperation("@@110", "=", new PhiDot(new PhiNameRef("package\$one"), "assertTrue_8kj6y5\$"), new PhiNameRef("assertTrue")));
  phiExpressionStatement(new PhiBinaryOperation("@@111", "=", new PhiDot(new PhiNameRef("package\$one"), "assertEquals_d0xna2\$"), new PhiNameRef("assertEquals")));
  phiExpressionStatement(new PhiBinaryOperation("@@112", "=", new PhiDot(new PhiNameRef("package\$one"), "assertException_7saol7\$"), new PhiNameRef("assertException")));
  phiExpressionStatement(new PhiBinaryOperation("@@113", "=", new PhiDot(new PhiNameRef("package\$one"), "testCheck"), new PhiNameRef("testCheck")));
  phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNameRef("Object"), "defineProperty"), array(new PhiNameRef("test1"), new PhiStringLiteral("Q"), new PhiObjectLiteral("@@114", array(array(new PhiNameRef("get"), new PhiNameRef("test1\$Q_getInstance")))))));
  phiExpressionStatement(new PhiBinaryOperation("@@115", "=", new PhiDot(new PhiNameRef("package\$one"), "test1"), new PhiNameRef("test1")));
  phiExpressionStatement(new PhiBinaryOperation("@@116", "=", new PhiDot(new PhiNameRef("package\$one"), "ShitParent"), new PhiNameRef("ShitParent")));
  phiExpressionStatement(new PhiBinaryOperation("@@117", "=", new PhiDot(new PhiNameRef("package\$one"), "Shit"), new PhiNameRef("Shit")));
  phiExpressionStatement(new PhiBinaryOperation("@@118", "=", new PhiDot(new PhiNameRef("package\$one"), "phiPrintln_pdl1vj\$"), new PhiNameRef("phiPrintln")));
  phiExpressionStatement(new PhiBinaryOperation("@@119", "=", new PhiDot(new PhiNameRef("package\$one"), "notNullOnce_30y1fr\$"), new PhiNameRef("notNullOnce")));
  phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "defineModule"), array(new PhiStringLiteral("phi-gross-test-1"), new PhiNameRef("_"))));
  phiExpressionStatement(new PhiInvocation(new PhiNameRef("main"), array(new PhiArrayLiteral(array()))));
  return phiEvaluate(new PhiNameRef("_"));
}), array(new PhiConditional(new PhiBinaryOperation("@@121", "===", new PhiUnaryOperation("@@120", "prefix", "typeof", new PhiBrackets(new PhiThis(), new PhiStringLiteral("phi-gross-test-1"))), new PhiStringLiteral("undefined")), new PhiObjectLiteral("@@122", array()), new PhiBrackets(new PhiThis(), new PhiStringLiteral("phi-gross-test-1"))), new PhiNameRef("kotlin")))));
