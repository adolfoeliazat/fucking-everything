<?php ; @define('PHI_KILL_LONG_LOOPS', true); if (phiEvaluateToBoolean(new PhiBinaryOperation("@@5", "===", new PhiUnaryOperation("@@2", "prefix", "typeof", new PhiNameRef("kotlin")), new PhiStringLiteral("undefined")))) {
  phiThrow(new PhiNew(new PhiNameRef("Error"), array(new PhiStringLiteral("Error loading module 'phi-gross-test-1'. Its dependency 'kotlin' was not found. Please, check whether 'kotlin' is loaded prior to 'phi-gross-test-1'."))));
}
phiExpressionStatement(new PhiBinaryOperation("@@1000", "=", new PhiBrackets(new PhiThis(), new PhiStringLiteral("phi-gross-test-1")), new PhiInvocation(new PhiFunctionExpression(null, array("_", "Kotlin"), function () {
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
  phiExpressionStatement(new PhiFunctionExpression("TestNotNullOnce\$runTest\$lambda_2", array(), function () {
    phiExpressionStatement(new PhiBinaryOperation("@@85", "=", new PhiDot(new PhiInvocation(new PhiNameRef("TestNotNullOnce\$Q_getInstance"), array()), "a"), new PhiStringLiteral("pizda again")));
  }));
  phiExpressionStatement(new PhiFunctionExpression("TestNotNullOnce\$runTest\$lambda_1", array("it"), function () {
    return phiEvaluate(new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "isType"), array(new PhiNameRef("it"), new PhiNameRef("IllegalStateException"))));
  }));
  phiExpressionStatement(new PhiFunctionExpression("TestNotNullOnce\$runTest\$lambda_0", array(), function () {
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("phiPrintln"), array(new PhiBinaryOperation("@@99", "+", new PhiStringLiteral("Q.a = "), new PhiDot(new PhiInvocation(new PhiNameRef("TestNotNullOnce\$Q_getInstance"), array()), "a")))));
  }));
  phiExpressionStatement(new PhiFunctionExpression("TestNotNullOnce\$runTest\$lambda", array("it"), function () {
    return phiEvaluate(new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "isType"), array(new PhiNameRef("it"), new PhiNameRef("IllegalStateException"))));
  }));
  phiExpressionStatement(new PhiFunctionExpression("TestNotNullOnce\$Q_getInstance", array(), function () {
    if (phiEvaluateToBoolean(new PhiBinaryOperation("@@112", "===", new PhiNameRef("TestNotNullOnce\$Q_instance"), new PhiNullLiteral("@@110")))) {
      phiExpressionStatement(new PhiNew(new PhiNameRef("TestNotNullOnce\$Q"), array()));
    }
    return phiEvaluate(new PhiNameRef("TestNotNullOnce\$Q_instance"));
  }));
  phiExpressionStatement(new PhiFunctionExpression("TestNotNullOnce\$Q", array(), function () {
    phiExpressionStatement(new PhiBinaryOperation("@@120", "=", new PhiNameRef("TestNotNullOnce\$Q_instance"), new PhiThis()));
    phiExpressionStatement(new PhiBinaryOperation("@@126", "=", new PhiDot(new PhiThis(), "a\$delegate"), new PhiInvocation(new PhiNameRef("notNullOnce"), array())));
  }));
  phiExpressionStatement(new PhiFunctionExpression("TestNotNullOnce", array(), function () {
  }));
  phiExpressionStatement(new PhiFunctionExpression("TestCheck\$runTest\$lambda_0", array(), function () {
    if (phiEvaluateToBoolean(new PhiUnaryOperation("@@132", "prefix", "!", new PhiBooleanLiteral("@@130", false)))) {
      phiVars("@@135", array(array("message", new PhiStringLiteral("vagina"))));
      phiThrow(new PhiNew(new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "IllegalStateException"), array(new PhiInvocation(new PhiDot(new PhiNameRef("message"), "toString"), array()))));
    }
  }));
  phiExpressionStatement(new PhiFunctionExpression("TestCheck\$runTest\$lambda", array("it"), function () {
    return phiEvaluate(new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "isType"), array(new PhiNameRef("it"), new PhiNameRef("IllegalStateException"))));
  }));
  phiExpressionStatement(new PhiFunctionExpression("TestCheck", array(), function () {
  }));
  phiExpressionStatement(new PhiFunctionExpression("runTest", array("test"), function () {
    phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNameRef("test"), "runTest"), array()));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("phiPrintln"), array(new PhiBinaryOperation("@@164", "+", new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "toString"), array(new PhiDot(new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "getKClassFromExpression"), array(new PhiNameRef("test"))), "simpleName"))), new PhiStringLiteral(": PASSED")))));
  }));
  phiExpressionStatement(new PhiFunctionExpression("Test", array(), function () {
  }));
  phiExpressionStatement(new PhiFunctionExpression("assertException", array("testType", "expectedMessage", "assertionID", "block"), function () {
    try {
      phiExpressionStatement(new PhiInvocation(new PhiNameRef("block"), array()));
    }
     catch (Exception $__phiException) {
      Phi::getCurrentEnv()->setVar('e', $__phiException->phiValue);
      if (phiEvaluateToBoolean(new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "isType"), array(new PhiNameRef("e"), new PhiNameRef("Throwable"))))) {
        phiVars("@@179", array(array("qwe", new PhiInvocation(new PhiNameRef("testType"), array(new PhiNameRef("e"))))));
        if (phiEvaluateToBoolean(new PhiUnaryOperation("@@181", "prefix", "!", new PhiNameRef("qwe"))))
          phiExpressionStatement(new PhiInvocation(new PhiNameRef("fail"), array(new PhiBinaryOperation("@@202", "+", new PhiBinaryOperation("@@192", "+", new PhiBinaryOperation("@@189", "+", new PhiBinaryOperation("@@186", "+", new PhiStringLiteral("assertException failed: "), new PhiNameRef("assertionID")), new PhiStringLiteral("\n")), new PhiStringLiteral("Actual exception: ")), new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "toString"), array(new PhiDot(new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "getKClassFromExpression"), array(new PhiNameRef("e"))), "simpleName")))))));
        phiExpressionStatement(new PhiInvocation(new PhiNameRef("assertEquals"), array(new PhiNameRef("expectedMessage"), new PhiDot(new PhiNameRef("e"), "message"), new PhiBinaryOperation("@@211", "+", new PhiStringLiteral("message-"), new PhiNameRef("assertionID")))));
      }
       else
        phiThrow(new PhiNameRef("e"));
    }
  }));
  phiExpressionStatement(new PhiFunctionExpression("insideLines", array("s"), function () {
    phiVars("@@217", array(array("line", new PhiStringLiteral("----------------------------------------------------------\n"))));
    return phiEvaluate(new PhiBinaryOperation("@@223", "+", new PhiBinaryOperation("@@220", "+", new PhiStringLiteral("\n----------------------------------------------------------\n"), new PhiNameRef("s")), new PhiStringLiteral("\n----------------------------------------------------------\n")));
  }));
  phiExpressionStatement(new PhiFunctionExpression("fail", array("msg"), function () {
    phiThrow(new PhiNew(new PhiNameRef("AssertionError"), array(new PhiInvocation(new PhiNameRef("insideLines"), array(new PhiNameRef("msg"))))));
  }));
  phiExpressionStatement(new PhiFunctionExpression("assertEquals", array("expected", "actual", "assertionID"), function () {
    if (phiEvaluateToBoolean(new PhiUnaryOperation("@@237", "prefix", "!", new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "equals"), array(new PhiNameRef("expected"), new PhiNameRef("actual")))))) {
      phiExpressionStatement(new PhiInvocation(new PhiNameRef("fail"), array(new PhiBinaryOperation("@@266", "+", new PhiBinaryOperation("@@260", "+", new PhiBinaryOperation("@@257", "+", new PhiBinaryOperation("@@254", "+", new PhiBinaryOperation("@@248", "+", new PhiBinaryOperation("@@245", "+", new PhiBinaryOperation("@@242", "+", new PhiStringLiteral("assertEquals failed: "), new PhiNameRef("assertionID")), new PhiStringLiteral("\n")), new PhiStringLiteral("Expected: ")), new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "toString"), array(new PhiNameRef("expected")))), new PhiStringLiteral("\n")), new PhiStringLiteral("Actual: ")), new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "toString"), array(new PhiNameRef("actual")))))));
    }
  }));
  phiExpressionStatement(new PhiFunctionExpression("assertTrue", array("b", "assertionID"), function () {
    if (phiEvaluateToBoolean(new PhiUnaryOperation("@@271", "prefix", "!", new PhiNameRef("b")))) {
      phiExpressionStatement(new PhiInvocation(new PhiNameRef("fail"), array(new PhiBinaryOperation("@@276", "+", new PhiStringLiteral("assertTrue failed: "), new PhiNameRef("assertionID")))));
    }
  }));
  phiExpressionStatement(new PhiFunctionExpression("TestPrint", array(), function () {
  }));
  phiExpressionStatement(new PhiFunctionExpression("TestLong", array(), function () {
  }));
  phiExpressionStatement(new PhiFunctionExpression("main", array("args"), function () {
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("sayShit"), array(new PhiNew(new PhiNameRef("Shit"), array(new PhiStringLiteral("Archibald"), new PhiStringLiteral("Fuck you"))))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("runTest"), array(new PhiNew(new PhiNameRef("TestCheck"), array()))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("runTest"), array(new PhiNew(new PhiNameRef("TestNotNullOnce"), array()))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("runTest"), array(new PhiNew(new PhiNameRef("TestPrint"), array()))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("runTest"), array(new PhiNew(new PhiNameRef("TestLong"), array()))));
  }));
  phiExpressionStatement(new PhiStringLiteral("use strict"));
  phiVars("@@310", array(array("println", new PhiDot(new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "io"), "println_s8jyv4\$"))));
  phiVars("@@315", array(array("print", new PhiDot(new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "io"), "print_s8jyv4\$"))));
  phiVars("@@319", array(array("AssertionError", new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "AssertionError"))));
  phiVars("@@321", array(array("Throwable", new PhiNameRef("Error"))));
  phiVars("@@325", array(array("IllegalStateException", new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "IllegalStateException"))));
  phiVars("@@329", array(array("plus", new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "plus_cmbeuq\$"))));
  phiVars("@@334", array(array("ReadWriteProperty", new PhiDot(new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "properties"), "ReadWriteProperty"))));
  phiExpressionStatement(new PhiBinaryOperation("@@342", "=", new PhiDot(new PhiNameRef("Shit"), "prototype"), new PhiInvocation(new PhiDot(new PhiNameRef("Object"), "create"), array(new PhiDot(new PhiNameRef("ShitParent"), "prototype")))));
  phiExpressionStatement(new PhiBinaryOperation("@@348", "=", new PhiDot(new PhiDot(new PhiNameRef("Shit"), "prototype"), "constructor"), new PhiNameRef("Shit")));
  phiExpressionStatement(new PhiBinaryOperation("@@450", "=", new PhiDot(new PhiDot(new PhiNameRef("TestLong"), "prototype"), "runTest"), new PhiFunctionExpression(null, array(), function () {
    phiVars("@@360", array(array("a", new PhiNew(new PhiDot(new PhiNameRef("Kotlin"), "Long"), array(new PhiNumberLiteral("@@355", -858993460), new PhiNumberLiteral("@@357", 214748364))))));
    phiVars("@@368", array(array("b", new PhiNew(new PhiDot(new PhiNameRef("Kotlin"), "Long"), array(new PhiNumberLiteral("@@363", 2061584302), new PhiNumberLiteral("@@365", 21474836))))));
    phiVars("@@376", array(array("c", new PhiNew(new PhiDot(new PhiNameRef("Kotlin"), "Long"), array(new PhiNumberLiteral("@@371", 1202590842), new PhiNumberLiteral("@@373", 236223201))))));
    phiVars("@@384", array(array("n", new PhiNew(new PhiDot(new PhiNameRef("Kotlin"), "Long"), array(new PhiNumberLiteral("@@379", -2139819130), new PhiNumberLiteral("@@381", -236224799))))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("println"), array(new PhiStringLiteral("a = 922337203685477580"))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("assertEquals"), array(new PhiStringLiteral("922337203685477580"), new PhiInvocation(new PhiDot(new PhiNew(new PhiDot(new PhiNameRef("Kotlin"), "Long"), array(new PhiNumberLiteral("@@392", -858993460), new PhiNumberLiteral("@@394", 214748364))), "toString"), array()), new PhiStringLiteral("4bed3e4b-e165-474b-9e8d-aa933e0438f5"))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("println"), array(new PhiStringLiteral("b = 92233720368547758"))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("assertEquals"), array(new PhiStringLiteral("92233720368547758"), new PhiInvocation(new PhiDot(new PhiNew(new PhiDot(new PhiNameRef("Kotlin"), "Long"), array(new PhiNumberLiteral("@@408", 2061584302), new PhiNumberLiteral("@@410", 21474836))), "toString"), array()), new PhiStringLiteral("b40b9861-85bf-43c5-a28c-f96cfbbbcd3e"))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("println"), array(new PhiStringLiteral("c = 1014570924054025338"))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("assertEquals"), array(new PhiStringLiteral("1014570924054025338"), new PhiInvocation(new PhiDot(new PhiNew(new PhiDot(new PhiNameRef("Kotlin"), "Long"), array(new PhiNumberLiteral("@@424", 1202590842), new PhiNumberLiteral("@@426", 236223201))), "toString"), array()), new PhiStringLiteral("dee46c9d-940b-48ff-aa78-b58a9aa49540"))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("println"), array(new PhiStringLiteral("n = -1014577784054025338"))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("assertEquals"), array(new PhiStringLiteral("-1014577784054025338"), new PhiInvocation(new PhiDot(new PhiNew(new PhiDot(new PhiNameRef("Kotlin"), "Long"), array(new PhiNumberLiteral("@@440", -2139819130), new PhiNumberLiteral("@@442", -236224799))), "toString"), array()), new PhiStringLiteral("8e0f7bec-18a5-40b0-8548-b194f2aca50a"))));
  })));
  phiExpressionStatement(new PhiBinaryOperation("@@457", "=", new PhiDot(new PhiDot(new PhiNameRef("TestLong"), "prototype"), "returnSame_s8cxhz\$"), new PhiFunctionExpression(null, array("x"), function () {
    return phiEvaluate(new PhiNameRef("x"));
  })));
  phiExpressionStatement(new PhiBinaryOperation("@@472", "=", new PhiDot(new PhiNameRef("TestLong"), "\$metadata\$"), new PhiObjectLiteral("@@470", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "CLASS")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("TestLong")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array(new PhiNameRef("Test"))))))));
  phiExpressionStatement(new PhiBinaryOperation("@@492", "=", new PhiDot(new PhiDot(new PhiNameRef("TestPrint"), "prototype"), "runTest"), new PhiFunctionExpression(null, array(), function () {
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("print"), array(new PhiStringLiteral("fuck shit"))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("println"), array(new PhiStringLiteral(" bitch"))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("println"), array(new PhiNumberLiteral("@@484", 123))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("println"), array(new PhiBooleanLiteral("@@488", true))));
  })));
  phiExpressionStatement(new PhiBinaryOperation("@@507", "=", new PhiDot(new PhiNameRef("TestPrint"), "\$metadata\$"), new PhiObjectLiteral("@@505", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "CLASS")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("TestPrint")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array(new PhiNameRef("Test"))))))));
  phiExpressionStatement(new PhiBinaryOperation("@@521", "=", new PhiDot(new PhiNameRef("Test"), "\$metadata\$"), new PhiObjectLiteral("@@519", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "INTERFACE")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("Test")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array()))))));
  phiExpressionStatement(new PhiBinaryOperation("@@533", "=", new PhiDot(new PhiDot(new PhiNameRef("TestCheck"), "prototype"), "runTest"), new PhiFunctionExpression(null, array(), function () {
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("assertException"), array(new PhiNameRef("TestCheck\$runTest\$lambda"), new PhiStringLiteral("vagina"), new PhiStringLiteral("7f2510ff-4753-4ea3-8d39-dcfe63ea910a"), new PhiNameRef("TestCheck\$runTest\$lambda_0"))));
  })));
  phiExpressionStatement(new PhiBinaryOperation("@@548", "=", new PhiDot(new PhiNameRef("TestCheck"), "\$metadata\$"), new PhiObjectLiteral("@@546", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "CLASS")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("TestCheck")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array(new PhiNameRef("Test"))))))));
  phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNameRef("Object"), "defineProperty"), array(new PhiDot(new PhiNameRef("TestNotNullOnce\$Q"), "prototype"), new PhiStringLiteral("a"), new PhiObjectLiteral("@@577", array(array(new PhiStringLiteral("get"), new PhiFunctionExpression(null, array(), function () {
    return phiEvaluate(new PhiInvocation(new PhiDot(new PhiDot(new PhiThis(), "a\$delegate"), "getValue_lrcp0p\$"), array(new PhiThis(), new PhiNew(new PhiDot(new PhiNameRef("Kotlin"), "PropertyMetadata"), array(new PhiStringLiteral("a"))))));
  })), array(new PhiStringLiteral("set"), new PhiFunctionExpression(null, array("a"), function () {
    phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiDot(new PhiThis(), "a\$delegate"), "setValue_9rddgb\$"), array(new PhiThis(), new PhiNew(new PhiDot(new PhiNameRef("Kotlin"), "PropertyMetadata"), array(new PhiStringLiteral("a"))), new PhiNameRef("a"))));
  })))))));
  phiExpressionStatement(new PhiBinaryOperation("@@592", "=", new PhiDot(new PhiNameRef("TestNotNullOnce\$Q"), "\$metadata\$"), new PhiObjectLiteral("@@590", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "OBJECT")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("Q")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array()))))));
  phiVars("@@596", array(array("TestNotNullOnce\$Q_instance", new PhiNullLiteral("@@594"))));
  phiExpressionStatement(new PhiBinaryOperation("@@649", "=", new PhiDot(new PhiDot(new PhiNameRef("TestNotNullOnce"), "prototype"), "runTest"), new PhiFunctionExpression(null, array(), function () {
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("assertException"), array(new PhiNameRef("TestNotNullOnce\$runTest\$lambda"), new PhiStringLiteral("Property `a` should be initialized before get."), new PhiStringLiteral("81060a82-59e8-4bbc-b733-501d71fdb169"), new PhiNameRef("TestNotNullOnce\$runTest\$lambda_0"))));
    phiExpressionStatement(new PhiBinaryOperation("@@610", "=", new PhiDot(new PhiInvocation(new PhiNameRef("TestNotNullOnce\$Q_getInstance"), array()), "a"), new PhiStringLiteral("pizda")));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("phiPrintln"), array(new PhiBinaryOperation("@@617", "+", new PhiStringLiteral("Q.a = "), new PhiDot(new PhiInvocation(new PhiNameRef("TestNotNullOnce\$Q_getInstance"), array()), "a")))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("assertEquals"), array(new PhiStringLiteral("pizda"), new PhiDot(new PhiInvocation(new PhiNameRef("TestNotNullOnce\$Q_getInstance"), array()), "a"), new PhiStringLiteral("9181db8b-07ec-45e1-87fb-d738c2d235e3"))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("assertException"), array(new PhiNameRef("TestNotNullOnce\$runTest\$lambda_1"), new PhiStringLiteral("Property `a` should be assigned only once"), new PhiStringLiteral("2be3d76a-cab1-4ae7-ad8e-3ac0a601bdd2"), new PhiNameRef("TestNotNullOnce\$runTest\$lambda_2"))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("phiPrintln"), array(new PhiBinaryOperation("@@638", "+", new PhiStringLiteral("Q.a = "), new PhiDot(new PhiInvocation(new PhiNameRef("TestNotNullOnce\$Q_getInstance"), array()), "a")))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef("assertEquals"), array(new PhiStringLiteral("pizda"), new PhiDot(new PhiInvocation(new PhiNameRef("TestNotNullOnce\$Q_getInstance"), array()), "a"), new PhiStringLiteral("b55ae4d8-9ae2-40a5-b0a7-6dd71bf98bf1"))));
  })));
  phiExpressionStatement(new PhiBinaryOperation("@@664", "=", new PhiDot(new PhiNameRef("TestNotNullOnce"), "\$metadata\$"), new PhiObjectLiteral("@@662", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "CLASS")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("TestNotNullOnce")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array(new PhiNameRef("Test"))))))));
  phiExpressionStatement(new PhiBinaryOperation("@@706", "=", new PhiDot(new PhiDot(new PhiNameRef("ShitParent"), "prototype"), "sayIt_61zpoe\$"), new PhiFunctionExpression(null, array("c"), function () {
    phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@676", "+", new PhiBinaryOperation("@@673", "+", new PhiStringLiteral("a = "), new PhiDot(new PhiThis(), "a")), new PhiStringLiteral("\n")))));
    phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@686", "+", new PhiBinaryOperation("@@683", "+", new PhiStringLiteral("b = "), new PhiDot(new PhiThis(), "b")), new PhiStringLiteral("\n")))));
    phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@695", "+", new PhiBinaryOperation("@@692", "+", new PhiStringLiteral("c = "), new PhiNameRef("c")), new PhiStringLiteral("\n")))));
    phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@702", "+", new PhiDot(new PhiThis(), "prelude"), new PhiStringLiteral("\n")))));
  })));
  phiExpressionStatement(new PhiBinaryOperation("@@720", "=", new PhiDot(new PhiNameRef("ShitParent"), "\$metadata\$"), new PhiObjectLiteral("@@718", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "CLASS")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("ShitParent")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array()))))));
  phiExpressionStatement(new PhiBinaryOperation("@@753", "=", new PhiDot(new PhiDot(new PhiNameRef("Shit"), "prototype"), "sayIt_61zpoe\$"), new PhiFunctionExpression(null, array("c"), function () {
    phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiDot(new PhiDot(new PhiNameRef("ShitParent"), "prototype"), "sayIt_61zpoe\$"), "call"), array(new PhiThis(), new PhiStringLiteral("fucking-c"))));
    phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef("phiPrint"), array(new PhiBinaryOperation("@@749", "+", new PhiBinaryOperation("@@746", "+", new PhiBinaryOperation("@@743", "+", new PhiBinaryOperation("@@739", "+", new PhiBinaryOperation("@@736", "+", new PhiStringLiteral("<b>"), new PhiDot(new PhiThis(), "name")), new PhiStringLiteral(", ")), new PhiDot(new PhiThis(), "text")), new PhiNameRef("c")), new PhiStringLiteral("<\/b>")))));
  })));
  phiExpressionStatement(new PhiBinaryOperation("@@768", "=", new PhiDot(new PhiNameRef("Shit"), "\$metadata\$"), new PhiObjectLiteral("@@766", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "CLASS")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("Shit")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array(new PhiNameRef("ShitParent"))))))));
  phiExpressionStatement(new PhiBinaryOperation("@@799", "=", new PhiDot(new PhiDot(new PhiNameRef("NotNullOnceVar"), "prototype"), "getValue_lrcp0p\$"), new PhiFunctionExpression(null, array("thisRef", "property"), function () {
    phiVars("@@776", array(array("tmp\$", new PhiUnaryOperation("@@773", "prefix", "void", new PhiNumberLiteral("@@something", 0)))));
    phiExpressionStatement(new PhiBinaryOperation("@@780", "=", new PhiNameRef("tmp\$"), new PhiDot(new PhiThis(), "value_0")));
    if (phiEvaluateToBoolean(new PhiBinaryOperation("@@785", "==", new PhiNameRef("tmp\$"), new PhiNullLiteral("@@783")))) {
      phiThrow(new PhiNew(new PhiNameRef("IllegalStateException"), array(new PhiBinaryOperation("@@794", "+", new PhiBinaryOperation("@@791", "+", new PhiStringLiteral("Property `"), new PhiDot(new PhiNameRef("property"), "callableName")), new PhiStringLiteral("` should be initialized before get.")))));
    }
    return phiEvaluate(new PhiNameRef("tmp\$"));
  })));
  phiExpressionStatement(new PhiBinaryOperation("@@834", "=", new PhiDot(new PhiDot(new PhiNameRef("NotNullOnceVar"), "prototype"), "setValue_9rddgb\$"), new PhiFunctionExpression(null, array("thisRef", "property", "value"), function () {
    if (phiEvaluateToBoolean(new PhiUnaryOperation("@@810", "prefix", "!", new PhiBinaryOperation("@@808", "==", new PhiDot(new PhiThis(), "value_0"), new PhiNullLiteral("@@806"))))) {
      phiVars("@@820", array(array("message", new PhiBinaryOperation("@@818", "+", new PhiBinaryOperation("@@815", "+", new PhiStringLiteral("Property `"), new PhiDot(new PhiNameRef("property"), "callableName")), new PhiStringLiteral("` should be assigned only once")))));
      phiThrow(new PhiNew(new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "kotlin"), "IllegalStateException"), array(new PhiInvocation(new PhiDot(new PhiNameRef("message"), "toString"), array()))));
    }
    phiExpressionStatement(new PhiBinaryOperation("@@831", "=", new PhiDot(new PhiThis(), "value_0"), new PhiNameRef("value")));
  })));
  phiExpressionStatement(new PhiBinaryOperation("@@849", "=", new PhiDot(new PhiNameRef("NotNullOnceVar"), "\$metadata\$"), new PhiObjectLiteral("@@847", array(array(new PhiNameRef("kind"), new PhiDot(new PhiDot(new PhiNameRef("Kotlin"), "Kind"), "CLASS")), array(new PhiNameRef("simpleName"), new PhiStringLiteral("NotNullOnceVar")), array(new PhiNameRef("interfaces"), new PhiArrayLiteral(array(new PhiNameRef("ReadWriteProperty"))))))));
  phiVars("@@861", array(array("package\$phi", new PhiBinaryOperation("@@859", "||", new PhiDot(new PhiNameRef("_"), "phi"), new PhiBinaryOperation("@@857", "=", new PhiDot(new PhiNameRef("_"), "phi"), new PhiObjectLiteral("@@855", array()))))));
  phiVars("@@872", array(array("package\$gross", new PhiBinaryOperation("@@870", "||", new PhiDot(new PhiNameRef("package\$phi"), "gross"), new PhiBinaryOperation("@@868", "=", new PhiDot(new PhiNameRef("package\$phi"), "gross"), new PhiObjectLiteral("@@866", array()))))));
  phiVars("@@883", array(array("package\$test", new PhiBinaryOperation("@@881", "||", new PhiDot(new PhiNameRef("package\$gross"), "test"), new PhiBinaryOperation("@@879", "=", new PhiDot(new PhiNameRef("package\$gross"), "test"), new PhiObjectLiteral("@@877", array()))))));
  phiVars("@@894", array(array("package\$one", new PhiBinaryOperation("@@892", "||", new PhiDot(new PhiNameRef("package\$test"), "one"), new PhiBinaryOperation("@@890", "=", new PhiDot(new PhiNameRef("package\$test"), "one"), new PhiObjectLiteral("@@888", array()))))));
  phiExpressionStatement(new PhiBinaryOperation("@@898", "=", new PhiDot(new PhiNameRef("package\$one"), "main_kand9s\$"), new PhiNameRef("main")));
  phiExpressionStatement(new PhiBinaryOperation("@@903", "=", new PhiDot(new PhiNameRef("package\$one"), "TestLong"), new PhiNameRef("TestLong")));
  phiExpressionStatement(new PhiBinaryOperation("@@908", "=", new PhiDot(new PhiNameRef("package\$one"), "TestPrint"), new PhiNameRef("TestPrint")));
  phiExpressionStatement(new PhiBinaryOperation("@@913", "=", new PhiDot(new PhiNameRef("package\$one"), "assertTrue_8kj6y5\$"), new PhiNameRef("assertTrue")));
  phiExpressionStatement(new PhiBinaryOperation("@@918", "=", new PhiDot(new PhiNameRef("package\$one"), "assertEquals_d0xna2\$"), new PhiNameRef("assertEquals")));
  phiExpressionStatement(new PhiBinaryOperation("@@923", "=", new PhiDot(new PhiNameRef("package\$one"), "assertException_7saol7\$"), new PhiNameRef("assertException")));
  phiExpressionStatement(new PhiBinaryOperation("@@928", "=", new PhiDot(new PhiNameRef("package\$one"), "Test"), new PhiNameRef("Test")));
  phiExpressionStatement(new PhiBinaryOperation("@@933", "=", new PhiDot(new PhiNameRef("package\$one"), "runTest_fwlgj3\$"), new PhiNameRef("runTest")));
  phiExpressionStatement(new PhiBinaryOperation("@@938", "=", new PhiDot(new PhiNameRef("package\$one"), "TestCheck"), new PhiNameRef("TestCheck")));
  phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNameRef("Object"), "defineProperty"), array(new PhiNameRef("TestNotNullOnce"), new PhiStringLiteral("Q"), new PhiObjectLiteral("@@946", array(array(new PhiNameRef("get"), new PhiNameRef("TestNotNullOnce\$Q_getInstance")))))));
  phiExpressionStatement(new PhiBinaryOperation("@@952", "=", new PhiDot(new PhiNameRef("package\$one"), "TestNotNullOnce"), new PhiNameRef("TestNotNullOnce")));
  phiExpressionStatement(new PhiBinaryOperation("@@957", "=", new PhiDot(new PhiNameRef("package\$one"), "ShitParent"), new PhiNameRef("ShitParent")));
  phiExpressionStatement(new PhiBinaryOperation("@@962", "=", new PhiDot(new PhiNameRef("package\$one"), "Shit"), new PhiNameRef("Shit")));
  phiExpressionStatement(new PhiBinaryOperation("@@967", "=", new PhiDot(new PhiNameRef("package\$one"), "phiPrintln_pdl1vj\$"), new PhiNameRef("phiPrintln")));
  phiExpressionStatement(new PhiBinaryOperation("@@972", "=", new PhiDot(new PhiNameRef("package\$one"), "notNullOnce_30y1fr\$"), new PhiNameRef("notNullOnce")));
  phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNameRef("Kotlin"), "defineModule"), array(new PhiStringLiteral("phi-gross-test-1"), new PhiNameRef("_"))));
  phiExpressionStatement(new PhiInvocation(new PhiNameRef("main"), array(new PhiArrayLiteral(array()))));
  return phiEvaluate(new PhiNameRef("_"));
}), array(new PhiConditional(new PhiBinaryOperation("@@990", "===", new PhiUnaryOperation("@@987", "prefix", "typeof", new PhiBrackets(new PhiThis(), new PhiStringLiteral("phi-gross-test-1"))), new PhiStringLiteral("undefined")), new PhiObjectLiteral("@@992", array()), new PhiBrackets(new PhiThis(), new PhiStringLiteral("phi-gross-test-1"))), new PhiNameRef("kotlin")))));
