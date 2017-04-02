<?php ; if (phiEvaluateToBoolean(new PhiBinaryOperation('@@1', '===', new PhiUnaryOperation('prefix', 'typeof', new PhiNameRef('kotlin')), new PhiStringLiteral('undefined')))) {
  phiThrow(new PhiNew(new PhiNameRef('Error'), array(new PhiStringLiteral("Error loading module 'phi-gross-test-1'. Its dependency 'kotlin' was not found. Please, check whether 'kotlin' is loaded prior to 'phi-gross-test-1'."))));
}
$GLOBALS['shit'] = 28; phiExpressionStatement(new PhiBinaryOperation('@@49', '=', new PhiBrackets(new PhiThis(), new PhiStringLiteral('phi-gross-test-1')), new PhiInvocation(new PhiFunctionExpression(null, array('_', 'Kotlin'), function () {
  $GLOBALS['shit'] = 1; phiExpressionStatement(new PhiStringLiteral('use strict'));
  phiVars(array(array('IllegalStateException', new PhiDot(new PhiDot(new PhiNameRef('Kotlin'), 'kotlin'), 'IllegalStateException'))));
  phiVars(array(array('ReadWriteProperty', new PhiDot(new PhiDot(new PhiDot(new PhiNameRef('Kotlin'), 'kotlin'), 'properties'), 'ReadWriteProperty'))));
  $GLOBALS['shit'] = 3; phiExpressionStatement(new PhiFunctionExpression('main', array('args'), function () {
    $GLOBALS['shit'] = 2; phiExpressionStatement(new PhiInvocation(new PhiNameRef('sayShit'), array(new PhiNew(new PhiNameRef('Shit'), array(new PhiStringLiteral('Archibald'), new PhiStringLiteral('Fuck you'))))));
  }));
  $GLOBALS['shit'] = 6; phiExpressionStatement(new PhiFunctionExpression('Shit', array('name', 'text'), function () {
    $GLOBALS['shit'] = 4; phiExpressionStatement(new PhiBinaryOperation('@@2', '=', new PhiDot(new PhiThis(), 'name'), new PhiNameRef('name')));
    $GLOBALS['shit'] = 5; phiExpressionStatement(new PhiBinaryOperation('@@3', '=', new PhiDot(new PhiThis(), 'text'), new PhiNameRef('text')));
  }));
  $GLOBALS['shit'] = 8; phiExpressionStatement(new PhiBinaryOperation('@@8', '=', new PhiDot(new PhiDot(new PhiNameRef('Shit'), 'prototype'), 'sayIt'), new PhiFunctionExpression(null, array(), function () {
    $GLOBALS['shit'] = 7; phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef('phiPrint'), array(new PhiBinaryOperation('@@7', '+', new PhiBinaryOperation('@@6', '+', new PhiBinaryOperation('@@5', '+', new PhiBinaryOperation('@@4', '+', new PhiStringLiteral('<b>'), new PhiDot(new PhiThis(), 'text')), new PhiStringLiteral(', ')), new PhiDot(new PhiThis(), 'name')), new PhiStringLiteral('<\/b>')))));
  })));
  $GLOBALS['shit'] = 9; phiExpressionStatement(new PhiBinaryOperation('@@10', '=', new PhiDot(new PhiNameRef('Shit'), '$metadata$'), new PhiObjectLiteral('@@9', array(array(new PhiNameRef('kind'), new PhiDot(new PhiDot(new PhiNameRef('Kotlin'), 'Kind'), 'CLASS')), array(new PhiNameRef('simpleName'), new PhiStringLiteral('Shit')), array(new PhiNameRef('interfaces'), new PhiArrayLiteral(array()))))));
  $GLOBALS['shit'] = 11; phiExpressionStatement(new PhiFunctionExpression('qwe', array(), function () {
    $GLOBALS['shit'] = 10; phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNew(new PhiNameRef('Shit'), array(new PhiStringLiteral('Archibald'), new PhiStringLiteral('fuck you'))), 'sayIt'), array()));
  }));
  $GLOBALS['shit'] = 13; phiExpressionStatement(new PhiFunctionExpression('sayShit', array('shit'), function () {
    $GLOBALS['shit'] = 12; phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef('phiPrint'), array(new PhiBinaryOperation('@@14', '+', new PhiBinaryOperation('@@13', '+', new PhiBinaryOperation('@@12', '+', new PhiBinaryOperation('@@11', '+', new PhiStringLiteral('<b>'), new PhiDot(new PhiNameRef('shit'), 'text')), new PhiStringLiteral(', ')), new PhiDot(new PhiNameRef('shit'), 'name')), new PhiStringLiteral('<\/b>')))));
  }));
  $GLOBALS['shit'] = 14; phiExpressionStatement(new PhiFunctionExpression('notNullOnce', array(), function () {
    return new PhiNew(new PhiNameRef('NotNullOnceVar'), array());
  }));
  $GLOBALS['shit'] = 16; phiExpressionStatement(new PhiFunctionExpression('NotNullOnceVar', array(), function () {
    $GLOBALS['shit'] = 15; phiExpressionStatement(new PhiBinaryOperation('@@16', '=', new PhiDot(new PhiThis(), 'value_0'), new PhiNullLiteral('@@15')));
  }));
  $GLOBALS['shit'] = 18; phiExpressionStatement(new PhiBinaryOperation('@@22', '=', new PhiDot(new PhiDot(new PhiNameRef('NotNullOnceVar'), 'prototype'), 'getValue_lrcp0p$'), new PhiFunctionExpression(null, array('thisRef', 'property'), function () {
    phiVars(array(array('tmp$', )));
    $GLOBALS['shit'] = 17; phiExpressionStatement(new PhiBinaryOperation('@@17', '=', new PhiNameRef('tmp$'), new PhiDot(new PhiThis(), 'value_0')));
    if (phiEvaluateToBoolean(new PhiBinaryOperation('@@19', '==', new PhiNameRef('tmp$'), new PhiNullLiteral('@@18')))) {
      phiThrow(new PhiNew(new PhiNameRef('IllegalStateException'), array(new PhiBinaryOperation('@@21', '+', new PhiBinaryOperation('@@20', '+', new PhiStringLiteral('Property `'), new PhiDot(new PhiNameRef('property'), 'callableName')), new PhiStringLiteral('` should be initialized before get.')))));
    }
    return new PhiNameRef('tmp$');
  })));
  $GLOBALS['shit'] = 20; phiExpressionStatement(new PhiBinaryOperation('@@28', '=', new PhiDot(new PhiDot(new PhiNameRef('NotNullOnceVar'), 'prototype'), 'setValue_9rddgb$'), new PhiFunctionExpression(null, array('thisRef', 'property', 'value'), function () {
    if (phiEvaluateToBoolean(new PhiUnaryOperation('prefix', '!', new PhiBinaryOperation('@@24', '==', new PhiDot(new PhiThis(), 'value_0'), new PhiNullLiteral('@@23'))))) {
      phiVars(array(array('message', new PhiBinaryOperation('@@26', '+', new PhiBinaryOperation('@@25', '+', new PhiStringLiteral('Property `'), new PhiDot(new PhiNameRef('property'), 'callableName')), new PhiStringLiteral('` should be assigned only once')))));
      phiThrow(new PhiNew(new PhiDot(new PhiDot(new PhiNameRef('Kotlin'), 'kotlin'), 'IllegalStateException'), array(new PhiInvocation(new PhiDot(new PhiNameRef('message'), 'toString'), array()))));
    }
    $GLOBALS['shit'] = 19; phiExpressionStatement(new PhiBinaryOperation('@@27', '=', new PhiDot(new PhiThis(), 'value_0'), new PhiNameRef('value')));
  })));
  $GLOBALS['shit'] = 21; phiExpressionStatement(new PhiBinaryOperation('@@30', '=', new PhiDot(new PhiNameRef('NotNullOnceVar'), '$metadata$'), new PhiObjectLiteral('@@29', array(array(new PhiNameRef('kind'), new PhiDot(new PhiDot(new PhiNameRef('Kotlin'), 'Kind'), 'CLASS')), array(new PhiNameRef('simpleName'), new PhiStringLiteral('NotNullOnceVar')), array(new PhiNameRef('interfaces'), new PhiArrayLiteral(array(new PhiNameRef('ReadWriteProperty'))))))));
  phiVars(array(array('package$phi', new PhiBinaryOperation('@@33', '||', new PhiDot(new PhiNameRef('_'), 'phi'), new PhiBinaryOperation('@@32', '=', new PhiDot(new PhiNameRef('_'), 'phi'), new PhiObjectLiteral('@@31', array()))))));
  phiVars(array(array('package$gross', new PhiBinaryOperation('@@36', '||', new PhiDot(new PhiNameRef('package$phi'), 'gross'), new PhiBinaryOperation('@@35', '=', new PhiDot(new PhiNameRef('package$phi'), 'gross'), new PhiObjectLiteral('@@34', array()))))));
  phiVars(array(array('package$test', new PhiBinaryOperation('@@39', '||', new PhiDot(new PhiNameRef('package$gross'), 'test'), new PhiBinaryOperation('@@38', '=', new PhiDot(new PhiNameRef('package$gross'), 'test'), new PhiObjectLiteral('@@37', array()))))));
  phiVars(array(array('package$one', new PhiBinaryOperation('@@42', '||', new PhiDot(new PhiNameRef('package$test'), 'one'), new PhiBinaryOperation('@@41', '=', new PhiDot(new PhiNameRef('package$test'), 'one'), new PhiObjectLiteral('@@40', array()))))));
  $GLOBALS['shit'] = 22; phiExpressionStatement(new PhiBinaryOperation('@@43', '=', new PhiDot(new PhiNameRef('package$one'), 'main_kand9s$'), new PhiNameRef('main')));
  $GLOBALS['shit'] = 23; phiExpressionStatement(new PhiBinaryOperation('@@44', '=', new PhiDot(new PhiNameRef('package$one'), 'Shit'), new PhiNameRef('Shit')));
  $GLOBALS['shit'] = 24; phiExpressionStatement(new PhiBinaryOperation('@@45', '=', new PhiDot(new PhiNameRef('package$one'), 'qwe'), new PhiNameRef('qwe')));
  $GLOBALS['shit'] = 25; phiExpressionStatement(new PhiBinaryOperation('@@46', '=', new PhiDot(new PhiNameRef('package$one'), 'notNullOnce_30y1fr$'), new PhiNameRef('notNullOnce')));
  $GLOBALS['shit'] = 26; phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNameRef('Kotlin'), 'defineModule'), array(new PhiStringLiteral('phi-gross-test-1'), new PhiNameRef('_'))));
  $GLOBALS['shit'] = 27; phiExpressionStatement(new PhiInvocation(new PhiNameRef('main'), array(new PhiArrayLiteral(array()))));
  return new PhiNameRef('_');
}), array(new PhiConditional(new PhiBinaryOperation('@@47', '===', new PhiUnaryOperation('prefix', 'typeof', new PhiBrackets(new PhiThis(), new PhiStringLiteral('phi-gross-test-1'))), new PhiStringLiteral('undefined')), new PhiObjectLiteral('@@48', array()), new PhiBrackets(new PhiThis(), new PhiStringLiteral('phi-gross-test-1'))), new PhiNameRef('kotlin')))));
