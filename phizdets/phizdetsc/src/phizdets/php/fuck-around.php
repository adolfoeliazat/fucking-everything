<?php ; if (phiEvaluateToBoolean(new PhiBinaryOperation("@@5", "===", new PhiUnaryOperation("@@2", "prefix", "typeof", new PhiNameRef("kotlin")), new PhiStringLiteral("undefined")))) {
  phiThrow(new PhiNew(new PhiNameRef("Error"), array(new PhiStringLiteral("Error loading module 'phi-gross-test-1'. Its dependency 'kotlin' was not found. Please, check whether 'kotlin' is loaded prior to 'phi-gross-test-1'."))));
}
phiExpressionStatement(new PhiBinaryOperation("@@743", "=", new PhiBrackets(new PhiThis(), new PhiStringLiteral("phi-gross-test-1")), new PhiInvocation(new PhiFunctionExpression(null, array("_", "Kotlin"), function () {
  phiExpressionStatement(new PhiFunctionExpression("NotNullOnceVar", array(), function () {
    phiExpressionStatement(new PhiBinaryOperation("@@17", "=", new PhiDot(new PhiThis(), "value_0"), new PhiNullLiteral("@@15")));
  }));
  phiExpressionStatement(new PhiFunctionExpression("notNullOnce", array(), function () {
    return phiEvaluate(new PhiNew(new PhiNameRef("NotNullOnceVar"), array()));
  }));
  phiExpressionStatement(new PhiFunctionExpression("sayShit", array("shit"), function () {
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("phiPrintln"), array(new PhiBinaryOperation("@@37", "+", new PhiBinaryOperation("@@34", "+", new PhiBinaryOperation("@@30", "+", new PhiBinaryOperation("@@27", "+", new PhiStringLiteral("<b>"), new PhiDot(new PhiNameRef("shit"), "text")), new PhiStringLiteral(", ")), new PhiDot(new PhiNameRef("shit"), "name")), new PhiStringLiteral("<\/b>")))));
  }));
  phiExpressionStatement(new PhiFunctionExpression("phiPrintln", array("x"), function () {
    phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiInvocation(new PhiNameRef("plus"), array(new PhiNameRef("x"), new PhiStringLiteral("\n"))))));
  }));
  phiExpressionStatement(new PhiFunctionExpression("Shit", array("name", "text"), function () {
    phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNameRef("ShitParent"), "call"), array(new PhiThis(), new PhiStringLiteral("fucking-a"), new PhiStringLiteral("fucking-b"))));
    phiExpressionStatement(new PhiBinaryOperation("@@57", "=", new PhiDot(new PhiThis(), "name"), new PhiNameRef("name")));
    phiExpressionStatement(new PhiBinaryOperation("@@62", "=", new PhiDot(new PhiThis(), "text"), new PhiNameRef("text")));
  }));
  phiExpressionStatement(new PhiFunctionExpression("ShitParent", array("a", "b"), function () {
    phiExpressionStatement(new PhiBinaryOperation("@@68", "=", new PhiDot(new PhiThis(), "a"), new PhiNameRef("a")));
    phiExpressionStatement(new PhiBinaryOperation("@@73", "=", new PhiDot(new PhiThis(), "b"), new PhiNameRef("b")));
    phiExpressionStatement(new PhiBinaryOperation("@@78", "=", new PhiDot(new PhiThis(), "prelude"), new PhiStringLiteral("Now I'm really gonna say it...")));
  }));
  phiExpressionStatement(new PhiFunctionExpression("testNotNullOnce_init\$lambda_2", array(), function () {
    phiExpressionStatement(new PhiBinaryOperation("@@85", "=", new PhiDot(new PhiInvocation(new PhiNameRef("testNotNullOnce\$Q_getInstance"), array()), "a"), new PhiStringLiteral("pizda again")));
  }));
  phiExpressionStatement(new PhiFunctionExpression("testNotNullOnce_init\$lambda_1", array("it"), function () {
    return phiEvaluate(new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "isType"), array(new PhiNameRef("it"), new PhiNameRef("IllegalStateException"))));
  }));
  phiExpressionStatement(new PhiFunctionExpression("testNotNullOnce_init\$lambda_0", array(), function () {
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("phiPrintln"), array(new PhiBinaryOperation("@@99", "+", new PhiStringLiteral("Q.a = "), new PhiDot(new PhiInvocation(new PhiNameRef("testNotNullOnce\$Q_getInstance"), array()), "a")))));
  }));
  phiExpressionStatement(new PhiFunctionExpression("testNotNullOnce_init\$lambda", array("it"), function () {
    return phiEvaluate(new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "isType"), array(new PhiNameRef("it"), new PhiNameRef("IllegalStateException"))));
  }));
  phiExpressionStatement(new PhiFunctionExpression("testNotNullOnce\$Q_getInstance", array(), function () {
    if (phiEvaluateToBoolean(new PhiBinaryOperation("@@112", "===", new PhiNameRef("testNotNullOnce\$Q_instance"), new PhiNullLiteral("@@110")))) {
      phiExpressionStatement(new PhiNew(new PhiNameRef("testNotNullOnce\$Q"), array()));
    }
    return phiEvaluate(new PhiNameRef("testNotNullOnce\$Q_instance"));
  }));
  phiExpressionStatement(new PhiFunctionExpression("testNotNullOnce\$Q", array(), function () {
    phiExpressionStatement(new PhiBinaryOperation("@@120", "=", new PhiNameRef("testNotNullOnce\$Q_instance"), new PhiThis()));
    phiExpressionStatement(new PhiBinaryOperation("@@126", "=", new PhiDot(new PhiThis(), "a\$delegate"), new PhiInvocation(new PhiNameRef("notNullOnce"), array())));
  }));
  phiExpressionStatement(new PhiFunctionExpression("testNotNullOnce", array(), function () {
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("assertException"), array(new PhiNameRef("testNotNullOnce_init\$lambda"), new PhiStringLiteral("Property `a` should be initialized before get."), new PhiStringLiteral("81060a82-59e8-4bbc-b733-501d71fdb169"), new PhiNameRef("testNotNullOnce_init\$lambda_0"))));
    phiExpressionStatement(new PhiBinaryOperation("@@139", "=", new PhiDot(new PhiInvocation(new PhiNameRef("testNotNullOnce\$Q_getInstance"), array()), "a"), new PhiStringLiteral("pizda")));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("phiPrintln"), array(new PhiBinaryOperation("@@146", "+", new PhiStringLiteral("Q.a = "), new PhiDot(new PhiInvocation(new PhiNameRef("testNotNullOnce\$Q_getInstance"), array()), "a")))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("assertEquals"), array(new PhiStringLiteral("pizda"), new PhiDot(new PhiInvocation(new PhiNameRef("testNotNullOnce\$Q_getInstance"), array()), "a"), new PhiStringLiteral("9181db8b-07ec-45e1-87fb-d738c2d235e3"))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("assertException"), array(new PhiNameRef("testNotNullOnce_init\$lambda_1"), new PhiStringLiteral("Property `a` should be assigned only once"), new PhiStringLiteral("2be3d76a-cab1-4ae7-ad8e-3ac0a601bdd2"), new PhiNameRef("testNotNullOnce_init\$lambda_2"))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("phiPrintln"), array(new PhiBinaryOperation("@@167", "+", new PhiStringLiteral("Q.a = "), new PhiDot(new PhiInvocation(new PhiNameRef("testNotNullOnce\$Q_getInstance"), array()), "a")))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("assertEquals"), array(new PhiStringLiteral("pizda"), new PhiDot(new PhiInvocation(new PhiNameRef("testNotNullOnce\$Q_getInstance"), array()), "a"), new PhiStringLiteral("b55ae4d8-9ae2-40a5-b0a7-6dd71bf98bf1"))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("phiPrintln"), array(new PhiStringLiteral("testNotNullOnce: PASSED"))));
  }));
  phiExpressionStatement(new PhiFunctionExpression("testCheck", array(), function () {
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("assertException"), array(new PhiNameRef("testCheck\$lambda\$lambda"), new PhiStringLiteral("vagina"), new PhiStringLiteral("7f2510ff-4753-4ea3-8d39-dcfe63ea910a"), new PhiNameRef("testCheck\$lambda\$lambda_0"))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("phiPrintln"), array(new PhiStringLiteral("testCheck: PASSED"))));
  }));
  phiExpressionStatement(new PhiFunctionExpression("testCheck\$lambda\$lambda_0", array(), function () {
    if (phiEvaluateToBoolean(new PhiUnaryOperation("@@193", "prefix", "!", new PhiBooleanLiteral("@@191", false)))) {
      phiVars("@@196", array(array("message", new PhiStringLiteral("vagina"))));
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
        phiVars("@@221", array(array("qwe", new PhiInvocation(new PhiNameRef("testType"), array(new PhiNameRef("e"))))));
        if (phiEvaluateToBoolean(new PhiUnaryOperation("@@223", "prefix", "!", new PhiNameRef("qwe"))))
          phiExpressionStatement(new PhiInvocation(new PhiNameRef("fail"), array(new PhiBinaryOperation("@@244", "+", new PhiBinaryOperation("@@234", "+", new PhiBinaryOperation("@@231", "+", new PhiBinaryOperation("@@228", "+", new PhiStringLiteral("assertException failed: "), new PhiNameRef("assertionID")), new PhiStringLiteral("\n")), new PhiStringLiteral("Actual exception: ")), new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "toString"), array(new PhiDot(new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "getKClassFromExpression"), array(new PhiNameRef("e"))), "simpleName")))))));
        phiExpressionStatement(new PhiInvocation(new PhiNameRef("assertEquals"), array(new PhiNameRef("expectedMessage"), new PhiDot(new PhiNameRef("e"), "message"), new PhiBinaryOperation("@@253", "+", new PhiStringLiteral("message-"), new PhiNameRef("assertionID")))));
      }
       else
        phiThrow(new PhiNameRef("e"));
    }
  }));
  phiExpressionStatement(new PhiFunctionExpression("insideLines", array("s"), function () {
    phiVars("@@259", array(array("line", new PhiStringLiteral("----------------------------------------------------------\n"))));
    return phiEvaluate(new PhiBinaryOperation("@@265", "+", new PhiBinaryOperation("@@262", "+", new PhiStringLiteral("\n----------------------------------------------------------\n"), new PhiNameRef("s")), new PhiStringLiteral("\n----------------------------------------------------------\n")));
  }));
  phiExpressionStatement(new PhiFunctionExpression("fail", array("msg"), function () {
    phiThrow(new PhiNew(new PhiNameRef("AssertionError"), array(new PhiInvocation(new PhiNameRef("insideLines"), array(new PhiNameRef("msg"))))));
  }));
  phiExpressionStatement(new PhiFunctionExpression("assertEquals", array("expected", "actual", "assertionID"), function () {
    if (phiEvaluateToBoolean(new PhiUnaryOperation("@@279", "prefix", "!", new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "equals"), array(new PhiNameRef("expected"), new PhiNameRef("actual")))))) {
      phiExpressionStatement(new PhiInvocation(new PhiNameRef("fail"), array(new PhiBinaryOperation("@@308", "+", new PhiBinaryOperation("@@302", "+", new PhiBinaryOperation("@@299", "+", new PhiBinaryOperation("@@296", "+", new PhiBinaryOperation("@@290", "+", new PhiBinaryOperation("@@287", "+", new PhiBinaryOperation("@@284", "+", new PhiStringLiteral("assertEquals failed: "), new PhiNameRef("assertionID")), new PhiStringLiteral("\n")), new PhiStringLiteral("Expected: ")), new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "toString"), array(new PhiNameRef("expected")))), new PhiStringLiteral("\n")), new PhiStringLiteral("Actual: ")), new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "toString"), array(new PhiNameRef("actual")))))));
    }
  }));
  phiExpressionStatement(new PhiFunctionExpression("assertTrue", array("b", "assertionID"), function () {
    if (phiEvaluateToBoolean(new PhiUnaryOperation("@@313", "prefix", "!", new PhiNameRef("b")))) {
      phiExpressionStatement(new PhiInvocation(new PhiNameRef("fail"), array(new PhiBinaryOperation("@@318", "+", new PhiStringLiteral("assertTrue failed: "), new PhiNameRef("assertionID")))));
    }
  }));
  phiExpressionStatement(new PhiFunctionExpression("main", array("args"), function () {
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("sayShit"), array(new PhiNew(new PhiNameRef("Shit"), array(new PhiStringLiteral("Archibald"), new PhiStringLiteral("Fuck you"))))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("testCheck"), array()));
    phiExpressionStatement(new PhiNew(new PhiNameRef("testNotNullOnce"), array()));
  }));
  phiExpressionStatement(new PhiStringLiteral("use strict"));
  phiVars("@@337", array(array("AssertionError", new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "AssertionError"))));
  phiVars("@@339", array(array("Throwable", new PhiNameRef("Error"))));
  phiVars("@@343", array(array("IllegalStateException", new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "IllegalStateException"))));
  phiVars("@@347", array(array("plus", new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "plus_cmbeuq\$"))));
  phiVars("@@352", array(array("ReadWriteProperty", new PhiDot(new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "properties"), "ReadWriteProperty"))));
  phiExpressionStatement(new PhiBinaryOperation("@@360", "=", new PhiDot(new PhiNameRef("Shit"), "prototype"), new PhiInvocation(new PhiDot(new PhiNameRef("Object"), "create"), array(new PhiDot(new PhiNameRef("ShitParent"), "prototype")))));
  phiExpressionStatement(new PhiBinaryOperation("@@366", "=", new PhiDot(new PhiDot(new PhiNameRef("Shit"), "prototype"), "constructor"), new PhiNameRef("Shit")));
  phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNameRef("Object"), "defineProperty"), array(new PhiDot(new PhiNameRef("testNotNullOnce\$Q"), "prototype"), new PhiStringLiteral("a"), new PhiObjectLiteral("@@395", array(array(new PhiStringLiteral("get"), new PhiFunctionExpression(null, array(), function () {
    return phiEvaluate(new PhiInvocation(new PhiDot(new PhiDot(new PhiThis(), "a\$delegate"), "getValue_lrcp0p\$"), array(new PhiThis(), new PhiNew(new PhiDot(new PhiNameRef("Kotlin"), "PropertyMetadata"), array(new PhiStringLiteral("a"))))));
  })), array(new PhiStringLiteral("set"), new PhiFunctionExpression(null, array("a"), function () {
    phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiDot(new PhiThis(), "a\$delegate"), "setValue_9rddgb\$"), array(new PhiThis(), new PhiNew(new PhiDot(new PhiNameRef("Kotlin"), "PropertyMetadata"), array(new PhiStringLiteral("a"))), new PhiNameRef("a"))));
  })))))));
  phiExpressionStatement(new PhiBinaryOperation("@@410", "=", new PhiDot(new PhiNameRef("testNotNullOnce\$Q"), "\$metadata\$"), new PhiObjectLiteral("@@408", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "OBJECT")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("Q")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array()))))));
  phiVars("@@414", array(array("testNotNullOnce\$Q_instance", new PhiNullLiteral("@@412"))));
  phiExpressionStatement(new PhiBinaryOperation("@@427", "=", new PhiDot(new PhiNameRef("testNotNullOnce"), "\$metadata\$"), new PhiObjectLiteral("@@425", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "CLASS")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("testNotNullOnce")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array()))))));
  phiExpressionStatement(new PhiBinaryOperation("@@469", "=", new PhiDot(new PhiDot(new PhiNameRef("ShitParent"), "prototype"), "sayIt_61zpoe\$"), new PhiFunctionExpression(null, array("c"), function () {
    phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@439", "+", new PhiBinaryOperation("@@436", "+", new PhiStringLiteral("a = "), new PhiDot(new PhiThis(), "a")), new PhiStringLiteral("\n")))));
    phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@449", "+", new PhiBinaryOperation("@@446", "+", new PhiStringLiteral("b = "), new PhiDot(new PhiThis(), "b")), new PhiStringLiteral("\n")))));
    phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@458", "+", new PhiBinaryOperation("@@455", "+", new PhiStringLiteral("c = "), new PhiNameRef("c")), new PhiStringLiteral("\n")))));
    phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@465", "+", new PhiDot(new PhiThis(), "prelude"), new PhiStringLiteral("\n")))));
  })));
  phiExpressionStatement(new PhiBinaryOperation("@@483", "=", new PhiDot(new PhiNameRef("ShitParent"), "\$metadata\$"), new PhiObjectLiteral("@@481", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "CLASS")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("ShitParent")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array()))))));
  phiExpressionStatement(new PhiBinaryOperation("@@516", "=", new PhiDot(new PhiDot(new PhiNameRef("Shit"), "prototype"), "sayIt_61zpoe\$"), new PhiFunctionExpression(null, array("c"), function () {
    phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiDot(new PhiDot(new PhiNameRef("ShitParent"), "prototype"), "sayIt_61zpoe\$"), "call"), array(new PhiThis(), new PhiStringLiteral("fucking-c"))));
    phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@512", "+", new PhiBinaryOperation("@@509", "+", new PhiBinaryOperation("@@506", "+", new PhiBinaryOperation("@@502", "+", new PhiBinaryOperation("@@499", "+", new PhiStringLiteral("<b>"), new PhiDot(new PhiThis(), "name")), new PhiStringLiteral(", ")), new PhiDot(new PhiThis(), "text")), new PhiNameRef("c")), new PhiStringLiteral("<\/b>")))));
  })));
  phiExpressionStatement(new PhiBinaryOperation("@@531", "=", new PhiDot(new PhiNameRef("Shit"), "\$metadata\$"), new PhiObjectLiteral("@@529", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "CLASS")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("Shit")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array(new PhiNameRef("ShitParent"))))))));
  phiExpressionStatement(new PhiBinaryOperation("@@562", "=", new PhiDot(new PhiDot(new PhiNameRef("NotNullOnceVar"), "prototype"), "getValue_lrcp0p\$"), new PhiFunctionExpression(null, array("thisRef", "property"), function () {
    phiVars("@@539", array(array("tmp\$", new PhiUnaryOperation("@@536", "prefix", "void", new PhiNumberLiteral("@@something", 0)))));
    phiExpressionStatement(new PhiBinaryOperation("@@543", "=", new PhiNameRef("tmp\$"), new PhiDot(new PhiThis(), "value_0")));
    if (phiEvaluateToBoolean(new PhiBinaryOperation("@@548", "==", new PhiNameRef("tmp\$"), new PhiNullLiteral("@@546")))) {
      phiThrow(new PhiNew(new PhiNameRef("IllegalStateException"), array(new PhiBinaryOperation("@@557", "+", new PhiBinaryOperation("@@554", "+", new PhiStringLiteral("Property `"), new PhiDot(new PhiNameRef("property"), "callableName")), new PhiStringLiteral("` should be initialized before get.")))));
    }
    return phiEvaluate(new PhiNameRef("tmp\$"));
  })));
  phiExpressionStatement(new PhiBinaryOperation("@@597", "=", new PhiDot(new PhiDot(new PhiNameRef("NotNullOnceVar"), "prototype"), "setValue_9rddgb\$"), new PhiFunctionExpression(null, array("thisRef", "property", "value"), function () {
    if (phiEvaluateToBoolean(new PhiUnaryOperation("@@573", "prefix", "!", new PhiBinaryOperation("@@571", "==", new PhiDot(new PhiThis(), "value_0"), new PhiNullLiteral("@@569"))))) {
      phiVars("@@583", array(array("message", new PhiBinaryOperation("@@581", "+", new PhiBinaryOperation("@@578", "+", new PhiStringLiteral("Property `"), new PhiDot(new PhiNameRef("property"), "callableName")), new PhiStringLiteral("` should be assigned only once")))));
      phiThrow(new PhiNew(new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "IllegalStateException"), array(new PhiInvocation(new PhiDot(new PhiNameRef("message"), "toString"), array()))));
    }
    phiExpressionStatement(new PhiBinaryOperation("@@594", "=", new PhiDot(new PhiThis(), "value_0"), new PhiNameRef("value")));
  })));
  phiExpressionStatement(new PhiBinaryOperation("@@612", "=", new PhiDot(new PhiNameRef("NotNullOnceVar"), "\$metadata\$"), new PhiObjectLiteral("@@610", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "CLASS")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("NotNullOnceVar")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array(new PhiNameRef("ReadWriteProperty"))))))));
  phiVars("@@624", array(array("package\$phi", new PhiBinaryOperation("@@622", "||", new PhiDot(new PhiNameRef("_"), "phi"), new PhiBinaryOperation("@@620", "=", new PhiDot(new PhiNameRef("_"), "phi"), new PhiObjectLiteral("@@618", array()))))));
  phiVars("@@635", array(array("package\$gross", new PhiBinaryOperation("@@633", "||", new PhiDot(new PhiNameRef("package\$phi"), "gross"), new PhiBinaryOperation("@@631", "=", new PhiDot(new PhiNameRef("package\$phi"), "gross"), new PhiObjectLiteral("@@629", array()))))));
  phiVars("@@646", array(array("package\$test", new PhiBinaryOperation("@@644", "||", new PhiDot(new PhiNameRef("package\$gross"), "test"), new PhiBinaryOperation("@@642", "=", new PhiDot(new PhiNameRef("package\$gross"), "test"), new PhiObjectLiteral("@@640", array()))))));
  phiVars("@@657", array(array("package\$one", new PhiBinaryOperation("@@655", "||", new PhiDot(new PhiNameRef("package\$test"), "one"), new PhiBinaryOperation("@@653", "=", new PhiDot(new PhiNameRef("package\$test"), "one"), new PhiObjectLiteral("@@651", array()))))));
  phiExpressionStatement(new PhiBinaryOperation("@@661", "=", new PhiDot(new PhiNameRef("package\$one"), "main_kand9s\$"), new PhiNameRef("main")));
  phiExpressionStatement(new PhiBinaryOperation("@@666", "=", new PhiDot(new PhiNameRef("package\$one"), "assertTrue_8kj6y5\$"), new PhiNameRef("assertTrue")));
  phiExpressionStatement(new PhiBinaryOperation("@@671", "=", new PhiDot(new PhiNameRef("package\$one"), "assertEquals_d0xna2\$"), new PhiNameRef("assertEquals")));
  phiExpressionStatement(new PhiBinaryOperation("@@676", "=", new PhiDot(new PhiNameRef("package\$one"), "assertException_7saol7\$"), new PhiNameRef("assertException")));
  phiExpressionStatement(new PhiBinaryOperation("@@681", "=", new PhiDot(new PhiNameRef("package\$one"), "testCheck"), new PhiNameRef("testCheck")));
  phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNameRef("Object"), "defineProperty"), array(new PhiNameRef("testNotNullOnce"), new PhiStringLiteral("Q"), new PhiObjectLiteral("@@689", array(array(new PhiNameRef("get"), new PhiNameRef("testNotNullOnce\$Q_getInstance")))))));
  phiExpressionStatement(new PhiBinaryOperation("@@695", "=", new PhiDot(new PhiNameRef("package\$one"), "testNotNullOnce"), new PhiNameRef("testNotNullOnce")));
  phiExpressionStatement(new PhiBinaryOperation("@@700", "=", new PhiDot(new PhiNameRef("package\$one"), "ShitParent"), new PhiNameRef("ShitParent")));
  phiExpressionStatement(new PhiBinaryOperation("@@705", "=", new PhiDot(new PhiNameRef("package\$one"), "Shit"), new PhiNameRef("Shit")));
  phiExpressionStatement(new PhiBinaryOperation("@@710", "=", new PhiDot(new PhiNameRef("package\$one"), "phiPrintln_pdl1vj\$"), new PhiNameRef("phiPrintln")));
  phiExpressionStatement(new PhiBinaryOperation("@@715", "=", new PhiDot(new PhiNameRef("package\$one"), "notNullOnce_30y1fr\$"), new PhiNameRef("notNullOnce")));
  phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "defineModule"), array(new PhiStringLiteral("phi-gross-test-1"), new PhiNameRef("_"))));
  phiExpressionStatement(new PhiInvocation(new PhiNameRef("main"), array(new PhiArrayLiteral(array()))));
  return phiEvaluate(new PhiNameRef("_"));
}), array(new PhiConditional(new PhiBinaryOperation("@@733", "===", new PhiUnaryOperation("@@730", "prefix", "typeof", new PhiBrackets(new PhiThis(), new PhiStringLiteral("phi-gross-test-1"))), new PhiStringLiteral("undefined")), new PhiObjectLiteral("@@735", array()), new PhiBrackets(new PhiThis(), new PhiStringLiteral("phi-gross-test-1"))), new PhiNameRef("kotlin")))));
