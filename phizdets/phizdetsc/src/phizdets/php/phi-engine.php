<?php

class PhiDot extends PhiExpression {
    /**@var PhiExpression*/ private $qualifier;
    /**@var string*/ private $name;

    /**
     * @param PhiExpression $qualifier
     * @param string $name
     */
    public function __construct($qualifier, $name) {
        $this->qualifier = $qualifier;
        $this->name = $name;
    }

    /**
     * @return PhiExpression
     */
    public function getQualifier() {
        return $this->qualifier;
    }

    /**
     * @return string
     */
    public function getName() {
        return $this->name;
    }

    /**
     * @return PhiValue
     */
    public function evaluate() {
        Phi::imf("5b8b7a6f-a4bf-47ad-bbfc-36d540bc2e6d");
    }
}

class PhiThis extends PhiExpression {
    /**
     * @return PhiValue
     */
    public function evaluate() {
        return Phi::getCurrentEnv()->getThisValue();
    }
}

abstract class PhiExpression {
    /**
     * @return PhiValue
     */
    public abstract function evaluate();
}

class PhiFunction extends PhiValue {
    /**@var PhiFunctionExpression*/ private $expr;

    /**
     * @param PhiFunctionExpression $expr
     */
    public function __construct(PhiFunctionExpression $expr) {
        $this->expr = $expr;
    }

    /**
     * @param PhiObject $receiver
     * @param PhiValue[] $args
     * @return PhiValue
     */
    public function invoke($receiver, $args) {
        Phi::pushEnv();
        Phi::getCurrentEnv()->setThisValue($receiver);
        for ($i = 0; $i < count($args); ++$i) {
            $name = $this->expr->getArgNames()[$i];
            Phi::getCurrentEnv()->setVar($name, $args[$i]);
        }

        call_user_func($this->expr->getBody());
    }

    /**
     * @return string
     */
    public function typeof() {
        return 'function';
    }
}

class PhiObject extends PhiValue {
    /**@var PhiValue[]*/ private $fields = array();

    /**
     * @param string $name
     * @return PhiValue
     */
    function getField($name) {
        $value = @$this->fields[$name];
        if ($value === null)
            return new PhiUndefined();
        else
            return $value;
    }

    /**
     * @param string $name
     * @param PhiValue $value
     */
    function setField($name, $value) {
        $this->fields[$name] = $value;
    }

    /**
     * @return string
     */
    public function typeof() {
        return 'object';
    }
}

class PhiFunctionExpression extends PhiExpression {
    /**@var string*/ private $name;
    /**@var string[]*/ private $argNames;
    /**@var Closure*/ private $body;

    /**
     * @param string $name
     * @param string[] $argNames
     * @param Closure $body
     */
    public function __construct($name, $argNames, $body) {
        $this->name = $name;
        $this->argNames = $argNames;
        $this->body = $body;
    }

    /**
     * @return PhiFunction
     */
    public function evaluate() {
        return new PhiFunction($this);
    }

    /**
     * @return string
     */
    public function getName() {
        return $this->name;
    }

    /**
     * @return string[]
     */
    public function getArgNames() {
        return $this->argNames;
    }

    /**
     * @return Closure
     */
    public function getBody() {
        return $this->body;
    }
}

//function phi__functionExpression($name, $argNames, $body) {
//    return new PhiFunctionExpression($name, $argNames, $body);
////    return array(
////        'debugDescription' => "Function `$name`",
////        'debugBacktrace' => phi__backtrace(),
////        'typeof' => 'function',
////        'name' => $name,
////        'argNames' => $argNames,
////        'function' => $f
////    );
//}

abstract class PhiValue {
    /**
     * @return string
     */
    abstract public function typeof();
}

class PhiUndefined extends PhiValue {
    /**
     * @return string
     */
    public function typeof() {
        return 'undefined';
    }
}

class PhiEnv {
    /**@var PhiEnv*/ private $parent;
    /**@var PhiValue[]*/ private $vars = array();
    /**@var PhiValue*/ private $thisValue;

    /**
     * @param PhiEnv $parent
     */
    function __construct($parent) {
        $this->parent = $parent;
    }

    /**
     * @param string $name
     * @param PhiValue $value
     * @return void
     */
    public function setVar($name, $value) {
        $this->vars[$name] = $value;
    }

    /**
     * @param string $name
     * @return PhiValue
     */
    public function getVar($name) {
        $value = @$this->vars[$name];
        if ($value !== null) {
            return $value;
        } else {
            if ($this->parent === null) {
                Phi::bitch("Variable `$name` not found");
            } else {
                return $this->parent->getVar($name);
            }
        }
    }

    /**
     * @return PhiEnv
     */
    public function getParent() {
        return $this->parent;
    }

    /**
     * @param PhiObject $thisValue
     */
    public function setThisValue($thisValue) {
        $this->thisValue = $thisValue;
    }

    /**
     * @return PhiValue
     */
    public function getThisValue() {
        if ($this->thisValue === null)
            Phi::bitch("`this` is not bound");
        return $this->thisValue;
    }

    /**
     * @param string $varName
     * @param PhiValue $value
     */
    public function setExistingVarHereOrInParents($varName, $value) {
        if (array_key_exists($varName, $this->vars)) {
            $this->vars[$varName] = $value;
        } else {
            if ($this->parent === null) {
                Phi::bitch("Assigning to non-existent variable `$varName`");
            } else {
                $this->parent->setExistingVarHereOrInParents($varName, $value);
            }
        }
    }

    /**
     * @param String $varName
     * @return boolean
     */
    public function hasVar($varName) {
        if (array_key_exists($varName, $this->vars)) {
            return true;
        } else {
            if ($this->parent === null) {
                return false;
            } else {
                return $this->parent->hasVar($varName);
            }
        }
    }
}

class Phi {
    /**@var PhiEnv*/ private static $currentEnv;

    public static function initEnv() {
        self::$currentEnv = new PhiEnv(null);

        phiFunctionDeclaration('Error', array('message'), function() {
            phiExpressionStatement(
                new PhiBinaryOperation('=',
                                       new PhiDot(new PhiThis(),
                                                  'message'),
                                       new PhiNameRef('message')));
        });

    }

    public static function initStdlib() {
        phiVars(array(array('kotlin', new PhiObjectLiteral(
            array(
            )))));
    }

    /**
     * @return PhiEnv
     */
    public static function getCurrentEnv() {
        return self::$currentEnv;
    }

    public static function pushEnv() {
        self::$currentEnv = new PhiEnv(self::$currentEnv);
    }

    public static function popEnv() {
        self::$currentEnv = self::$currentEnv->getParent();
        if (self::$currentEnv === null)
            Phi::wtf("0a0f0cfa-1ccf-4017-b97c-c064ad0b0015");
    }

    public static function imf($msg) {
        throw new PhiIllegalStateException("Implement me, please, fuck you. $msg");
    }

    public static function wtf($msg) {
        throw new PhiIllegalStateException("WTF: $msg");
    }

    public static function bitch($msg) {
        throw new PhiIllegalStateException($msg);
    }

    public static function check($cond, $msg) {
        if (!$cond) {
            throw new PhiIllegalStateException($msg);
        }
    }

}

function phiFunctionDeclaration($name, $argNames, $body) {
    $expr = new PhiFunctionExpression($name, $argNames, $body);
    Phi::getCurrentEnv()->setVar($name, $expr->evaluate());
}



class PhiIllegalStateException extends Exception {
    function __construct($message) {
        parent::__construct($message);
    }
}

class PhiAssertionError extends Exception {
    function __construct($message) {
        parent::__construct($message);
    }
}

function phiAssertEquals($expectedExported, $actual) {
    $actualExported = var_export($actual, true);
    if ($actualExported !== $expectedExported)
        throw new PhiAssertionError("\nExpected: $expectedExported; \nActual: $actualExported");
}

function phi__printBoolean($b) {
    if ($b) echo 'true';
    else echo 'false';
}




function phi__dumpNameValueSequence() {
    phiPrintln(phi__dumpNameValueSequenceToString(func_get_args()));
}

function phi__dumpNameValueSequenceToString($nameValueSequence, $opts = array()) {
    $count = count($nameValueSequence);
    Phi::check($count > 0 && $count % 2 === 0, "40c23506-2a94-468f-9f07-e32e1b318068    count = $count");

    $res = '';
    for ($i = 0; $i < $count; $i += 2) {
        $name = $nameValueSequence[$i];
        $value = $nameValueSequence[$i + 1];
        $valueExported = var_export($value, true);
        $res .= "$name = $valueExported\n";
    }

    if (isset($opts['indent'])) {
        $lines = explode("\n", $res);
        $res = implode("\n", array_map(function($s) use($opts) {return str_repeat(' ', $opts['indent']) . $s;}, $lines));
    }

    return $res;
}

function phi__invokeWithReceiver(&$receiver, $fPE, $args) {
    // phi__dumpFunctionEnterAndExit('phi__dumpFunctionEnterAndExit', 'receiver', $receiver, 'fPE', $fPE, 'args', $args, 'env', $GLOBALS['phi__currentEnv']); exit();

    $fCell = phi__findCell($fPE);
    // phi__dumpNameValueSequence('fCell', $fCell); exit();
    $f =& $fCell['value'];
    $argNames = $f['argNames'];
    phi__check(count($argNames) === count($args), "c099b7ef-ab67-4155-8b54-388357b3781a");
    $env =& phi__pushEnv();
    for ($i = 0; $i < count($argNames); ++$i) {
        phi__setCell($argNames[$i], phi__evaluate($args[$i]));
    }
    $env['thisCell'] =& phi__makeCell();
    $res = call_user_func($f['function']);
    phi__popEnv();
    return $res;
}

function& phi__currentEnv() {
    return $GLOBALS['phi__currentEnv'];
}

function phi__setCell($name, $value) {
    phiPrintln("-- Setting cell `$name` value to " . var_export($value, true));
    $cell = array();
    phi__currentEnv()['varCells'][$name] =& $cell;
    $cell['value'] = $value;
}


function phi__backtrace() {
    $bt = debug_backtrace();
    $res = array();
    for ($i = 0; $i < count($bt); ++$i) {
        $file = $bt[$i]['file'];
        $lastSlash = strrpos($file, '/');
        if ($lastSlash === false) {
            $lastSlash = strrpos($file, '\\');
            if ($lastSlash === false)
                $lastSlash = -1;
        }
        $file = substr($file, $lastSlash + 1);
        $line = $bt[$i]['line'];
        array_push($res, "$file:$line");
    }
    return $res;
}

class PhiNew extends PhiExpression {
    /**@var PhiExpression*/ private $ctor;
    /**@var PhiExpression[]*/ private $args;

    /**
     * @param PhiExpression $ctor
     * @param PhiExpression[] $args
     */
    public function __construct(PhiExpression $ctor, array $args) {
        $this->ctor = $ctor;
        $this->args = $args;
    }

    /**
     * @return PhiValue
     */
    public function evaluate() {
        // Example:
        //     function Shit(a, b) {this.a = a; this.b = b}
        //     new Shit(a, b)

        $ctorValue = $this->ctor->evaluate();
        if (!($ctorValue instanceof PhiFunction))
            Phi::imf("d3b13f4a-b3d3-4b80-b9e0-45ac964f08f8");
        $inst = new PhiObject();

        $argValues = array();
        foreach ($this->args as $arg)
            array_push($argValues, $arg->evaluate());

        $ctorValue->invoke($inst, $argValues);
        return $inst;
    }
}

function phiPrintln($x) {
    echo $x . "\n";
}

function phi__printlnAndExit($x) {
    phiPrintln($x);
    exit();
}

class PhiBoolean extends PhiValue {
    /**@var boolean*/ private $value;

    /**
     * PhiBoolean constructor.
     * @param bool $value
     */
    public function __construct($value) {
        $this->value = $value;
    }

    /**
     * @return bool
     */
    public function getValue() {
        return $this->value;
    }

    /**
     * @return string
     */
    public function typeof() {
        return 'boolean';
    }
}

class PhiBinaryOperation extends PhiExpression {
    /**@var string*/ private $op;
    /**@var PhiExpression*/ private $lhs;
    /**@var PhiExpression*/ private $rhs;

    /**
     * @param string $op
     * @param PhiExpression $lhs
     * @param PhiExpression $rhs
     */
    public function __construct($op, PhiExpression $lhs, PhiExpression $rhs) {
        $this->op = $op;
        $this->lhs = $lhs;
        $this->rhs = $rhs;
    }

    /**
     * @return string
     */
    public function getOp() {
        return $this->op;
    }

    /**
     * @return PhiExpression
     */
    public function getLhs() {
        return $this->lhs;
    }

    /**
     * @return PhiExpression
     */
    public function getRhs() {
        return $this->rhs;
    }

    /**
     * @return PhiValue
     */
    public function evaluate() {
        $rhsValue = $this->rhs->evaluate();
        if ($this->op === '===') {
            $lhsValue = $this->lhs->evaluate();
            if ($lhsValue instanceof PhiString && $rhsValue instanceof PhiString) {
                return new PhiBoolean($lhsValue->getValue() == $rhsValue->getValue());
            } else {
                $lhsValueClass = get_class($lhsValue);
                $rhsValueClass = get_class($rhsValue);
                Phi::wtf("8614085d-5212-46f6-9516-f256e02788b0    lhsValue is $lhsValueClass; rhsValue is $rhsValueClass");
            }
        }
        else if ($this->op === '=') {
            if ($this->lhs instanceof PhiNameRef) {
                // Ex: a = b
                $varName = $this->lhs->getName();
                Phi::getCurrentEnv()->setExistingVarHereOrInParents($varName, $rhsValue);
            }
            else if ($this->lhs instanceof PhiDot) {
                // Ex: fucking(shit()).b = c
                $object = $this->lhs->getQualifier()->evaluate();
                if (!($object instanceof PhiObject))
                    Phi::bitch("36107ced-0278-40c0-a2ba-3742aeb4330c");
                $fieldName = $this->lhs->getName();
                $object->setField($fieldName, $rhsValue);
            }
            // Ex: a['b'] = c
            else {
                Phi::wtf("a7b2e444-2530-4a2c-a3cf-eb821e3f440b");
            }
        }
        else {
            Phi::wtf("6bb8ca7a-c00a-4f60-9930-211dba14c031    op = {$this->op}");
        }
    }
}

function phi__dumpFunctionEnterAndExit() {
    $args = func_get_args();
    $functionName = $args[0];
    $rest = array_slice($args, 1);

    $location = phi__backtrace()[1];
    phiPrintln("Entering $functionName    $location");
    echo call_user_func('phi__dumpNameValueSequenceToString', $rest, array('indent' => 4));
    exit();
}

function phi__dump($x) {
    echo var_export($x, true) . "\n";
}

function phi__dumpAndExit($x) {
    phi__dump($x);
    exit();
}

function phi__dumpNameValueSequenceAndExit() {
    call_user_func_array('phi__dumpNameValueSequence', func_get_args());
    exit();
}

class PhiNameRef extends PhiExpression {
    /**@var string*/ private $name;

    /**
     * PhiNameRef constructor.
     * @param string $name
     */
    public function __construct($name) {
        $this->name = $name;
    }

    /**
     * @return PhiValue
     */
    public function evaluate() {
        return Phi::getCurrentEnv()->getVar($this->name);
    }

    /**
     * @return string
     */
    public function getName() {
        return $this->name;
    }
}

class PhiStringLiteral extends PhiExpression {
    /**@var string*/ private $value;

    /**
     * PhiNameRef constructor.
     * @param string $value
     */
    public function __construct($value) {
        $this->value = $value;
    }

    /**
     * @return PhiValue
     */
    public function evaluate() {
        return new PhiString($this->value);
    }

    /**
     * @return string
     */
    public function getValue() {
        return $this->value;
    }
}

class PhiString extends PhiValue {
    /**@var string*/ private $value;

    /**
     * PhiString constructor.
     * @param string $value
     */
    public function __construct($value) {
        $this->value = $value;
    }

    /**
     * @return string
     */
    public function getValue() {
        return $this->value;
    }

    /**
     * @return string
     */
    public function typeof() {
        return 'string';
    }
}

/**
 * @param PhiExpression $expr
 */
function phiExpressionStatement($expr) {
    // TODO:vgrechka Turn function expression to function declaration
    $expr->evaluate();
}

/**
 * @param PhiExpression $expr
 * @throws Exception
 */
function phiThrow($expr) {
    /**@var PhiObject $phiValue*/
    $phiValue = $expr->evaluate();
    /**@var PhiString $messagePhiValue*/
    $messagePhiValue = $phiValue->getField('message');
    $exception = new Exception($messagePhiValue->getValue());
    $exception->phiValue = $phiValue;
    throw $exception;
}

/**
 * @param PhiExpression $expr
 * @return boolean
 */
function phiEvaluateToBoolean($expr) {
    $phiValue = $expr->evaluate();
    if (!($phiValue instanceof PhiBoolean))
        Phi::bitch("0c8e6543-b05c-4852-a200-185ac3b36632");
    return $phiValue->getValue();
}

class PhiPrefixOperation extends PhiExpression {
    /**@var string*/ private $op;
    /**@var PhiExpression*/ private $arg;

    /**
     * @param string $op
     * @param PhiExpression $arg
     */
    public function __construct($op, $arg) {
        $this->op = $op;
        $this->arg = $arg;
    }

    /**
     * @return string
     */
    public function getOp() {
        return $this->op;
    }

    /**
     * @return PhiExpression
     */
    public function getArg() {
        return $this->arg;
    }

    /**
     * @return PhiValue
     */
    public function evaluate() {
        if ($this->op === 'typeof') {
            if ($this->arg instanceof PhiNameRef && !Phi::getCurrentEnv()->hasVar($this->arg->getName())) {
                return new PhiString('undefined');
            } else {
                $argPhiValue = $this->arg->evaluate();
                return new PhiString($argPhiValue->typeof());
            }
        }
        else {
            Phi::wtf("f88f33d3-2868-43d5-82b7-a1b8c20fc1cb");
        }
    }
}

function phiFailWantingException() {
    throw new PhiAssertionError("I want an exception");
}

function phiAssertException($expectedExportedMessage, $f) {
    $gotException = false;
    try {
        $f();
    } catch (Exception $e) {
        $gotException = true;
        phiAssertEquals($expectedExportedMessage, $e->getMessage());
    }
    if (!$gotException)
        phiFailWantingException();
}

class PhiBrackets extends PhiExpression {
    /**@var PhiExpression*/ private $object;
    /**@var PhiExpression*/ private $index;

    /**
     * @param PhiExpression $object
     * @param PhiExpression $index
     */
    public function __construct(PhiExpression $object, PhiExpression $index) {
        $this->object = $object;
        $this->index = $index;
    }

    /**
     * @return PhiExpression
     */
    public function getObject() {
        return $this->object;
    }

    /**
     * @return PhiExpression
     */
    public function getIndex() {
        return $this->index;
    }

    /**
     * @return PhiValue
     */
    public function evaluate() {
        Phi::imf("7bef0ca5-7259-462b-9545-00f90f312918");
    }
}

class PhiInvocation extends PhiExpression {
    /**@var PhiExpression*/ private $callee;
    /**@var array[PhiExpression]*/ private $args;

    /**
     * @param PhiExpression $callee
     * @param array $args
     */
    public function __construct(PhiExpression $callee, array $args) {
        $this->callee = $callee;
        $this->args = $args;
    }

    /**
     * @return PhiExpression
     */
    public function getCallee() {
        return $this->callee;
    }

    /**
     * @return array
     */
    public function getArgs() {
        return $this->args;
    }

    /**
     * @return PhiValue
     */
    public function evaluate() {
        Phi::imf("74952c0e-859f-4a28-8b57-ca9633744542");
    }
}

class PhiExternalNameRef extends PhiExpression {
    /**@var string*/ private $name;

    /**
     * @param string $name
     */
    public function __construct($name) {
        $this->name = $name;
    }

    /**
     * @return string
     */
    public function getName() {
        return $this->name;
    }

    /**
     * @return PhiValue
     */
    public function evaluate() {
        Phi::imf("9ef6218c-e6bd-4394-acdf-ca73a469b33c");
    }
}

class PhiConditional extends PhiExpression {
    /**@var PhiExpression*/ private $if;
    /**@var PhiExpression*/ private $then;
    /**@var PhiExpression*/ private $else;

    /**
     * @param PhiExpression $if
     * @param PhiExpression $then
     * @param PhiExpression $else
     */
    public function __construct(PhiExpression $if, PhiExpression $then, PhiExpression $else) {
        $this->if = $if;
        $this->then = $then;
        $this->else = $else;
    }

    /**
     * @return PhiExpression
     */
    public function getIf() {
        return $this->if;
    }

    /**
     * @return PhiExpression
     */
    public function getThen() {
        return $this->then;
    }

    /**
     * @return PhiExpression
     */
    public function getElse() {
        return $this->else;
    }

    /**
     * @return PhiValue
     */
    public function evaluate() {
        Phi::imf("c45cf7e3-e4a3-4924-a3da-be43805b0f4b");
    }
}

class PhiObjectLiteral extends PhiExpression {
    /**@var array*/ private $keyValuePairs;

    /**
     * @param array $keyValuePairs
     */
    public function __construct(array $keyValuePairs) {
        $this->keyValuePairs = $keyValuePairs;
    }

    /**
     * @return array
     */
    public function getKeyValuePairs() {
        return $this->keyValuePairs;
    }

    /**
     * @return PhiValue
     */
    public function evaluate() {
        Phi::imf("360d40af-538e-4f4b-bbcc-ff035a699938");
    }
}

/**
 * @param array $nameValuePairs
 */
function phiVars($nameValuePairs) {
    foreach ($nameValuePairs as $pair) {
        /**@var string $varName*/
        $varName = $pair[0];
        /**@var PhiExpression $expr*/
        $expr = $pair[1];

        Phi::getCurrentEnv()->setVar($varName, $expr->evaluate());
    }
}


// ==================================== ENTRY ======================================


Phi::initEnv();
Phi::initStdlib();

function phiQuickTest_1() {
    Phi::initEnv();
    phiAssertException("'We are hosed, man...'", function() {
        $expr = new PhiNew(new PhiNameRef('Error'),
                           array(new PhiStringLiteral("We are hosed, man...")));
        phiThrow($expr);
    });
}
phiQuickTest_1();

function phiQuickTest_2() {
    Phi::initEnv();
    phiAssertException("'Fuck you'", function() {
        if (phiEvaluateToBoolean(new PhiBinaryOperation('===', new PhiPrefixOperation('typeof', new PhiNameRef('kotlin')), new PhiStringLiteral('undefined')))) {
            phiThrow(new PhiNew(new PhiNameRef('Error'), array(new PhiStringLiteral("Fuck you"))));
        }
    });
}
phiQuickTest_2();

function phiQuickTest_3() {
    Phi::initEnv();
    Phi::initStdlib();
    if (phiEvaluateToBoolean(new PhiBinaryOperation('===', new PhiPrefixOperation('typeof', new PhiNameRef('kotlin')), new PhiStringLiteral('undefined')))) {
        phiThrow(new PhiNew(new PhiNameRef('Error'), array(new PhiStringLiteral("Fuck you"))));
    }
}
// phiQuickTest_3();









if (phiEvaluateToBoolean(new PhiBinaryOperation('===', new PhiPrefixOperation('typeof', new PhiNameRef('kotlin')), new PhiStringLiteral('undefined')))) {
    phiThrow(new PhiNew(new PhiNameRef('Error'), array(new PhiStringLiteral("Error loading module 'phi-gross-test-1'. Its dependency 'kotlin' was not found. Please, check whether 'kotlin' is loaded prior to 'phi-gross-test-1'."))));
}
phiExpressionStatement(new PhiBinaryOperation('=', new PhiBrackets(new PhiThis(), new PhiStringLiteral('phi-gross-test-1')), new PhiInvocation(new PhiFunctionExpression(null, array('_', 'Kotlin'), function () {
    phiExpressionStatement(new PhiStringLiteral('use strict'));
    phiExpressionStatement(new PhiFunctionExpression('Error_0', array('message'), function () {
        phiExpressionStatement(new PhiBinaryOperation('=', new PhiDot(new PhiThis(), 'message'), new PhiNameRef('message')));
    }));
    phiExpressionStatement(new PhiBinaryOperation('=', new PhiDot(new PhiNameRef('Error_0'), '$metadata$'), new PhiObjectLiteral(array(array(new PhiNameRef('kind'), new PhiDot(new PhiDot(new PhiNameRef('Kotlin'), 'Kind'), 'CLASS')), array(new PhiNameRef('simpleName'), new PhiStringLiteral('Error')), array(new PhiNameRef('interfaces'), array())))));
    phiExpressionStatement(new PhiFunctionExpression('Shit', array('name', 'text'), function () {
        phiExpressionStatement(new PhiBinaryOperation('=', new PhiDot(new PhiThis(), 'name'), new PhiNameRef('name')));
        phiExpressionStatement(new PhiBinaryOperation('=', new PhiDot(new PhiThis(), 'text'), new PhiNameRef('text')));
    }));
    phiExpressionStatement(new PhiBinaryOperation('=', new PhiDot(new PhiNameRef('Shit'), '$metadata$'), new PhiObjectLiteral(array(array(new PhiNameRef('kind'), new PhiDot(new PhiDot(new PhiNameRef('Kotlin'), 'Kind'), 'CLASS')), array(new PhiNameRef('simpleName'), new PhiStringLiteral('Shit')), array(new PhiNameRef('interfaces'), array())))));
    phiExpressionStatement(new PhiFunctionExpression('main', array('args'), function () {
        phiExpressionStatement(new PhiInvocation(new PhiNameRef('sayShit'), array(new PhiNew(new PhiNameRef('Shit'), array(new PhiStringLiteral('Archibald'), new PhiStringLiteral('Fuck you'))))));
    }));
    phiExpressionStatement(new PhiFunctionExpression('sayShit', array('shit'), function () {
        phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef('echo'), array(new PhiBinaryOperation('+', new PhiBinaryOperation('+', new PhiBinaryOperation('+', new PhiBinaryOperation('+', new PhiStringLiteral('<b>'), new PhiDot(new PhiNameRef('shit'), 'text')), new PhiStringLiteral(', ')), new PhiDot(new PhiNameRef('shit'), 'name')), new PhiStringLiteral('<\/b>')))));
    }));
    phiVars(array(array('package$phi', new PhiBinaryOperation('||', new PhiDot(new PhiNameRef('_'), 'phi'), new PhiBinaryOperation('=', new PhiDot(new PhiNameRef('_'), 'phi'), new PhiObjectLiteral(array()))))));
    phiVars(array(array('package$gross', new PhiBinaryOperation('||', new PhiDot(new PhiNameRef('package$phi'), 'gross'), new PhiBinaryOperation('=', new PhiDot(new PhiNameRef('package$phi'), 'gross'), new PhiObjectLiteral(array()))))));
    phiVars(array(array('package$test', new PhiBinaryOperation('||', new PhiDot(new PhiNameRef('package$gross'), 'test'), new PhiBinaryOperation('=', new PhiDot(new PhiNameRef('package$gross'), 'test'), new PhiObjectLiteral(array()))))));
    phiVars(array(array('package$one', new PhiBinaryOperation('||', new PhiDot(new PhiNameRef('package$test'), 'one'), new PhiBinaryOperation('=', new PhiDot(new PhiNameRef('package$test'), 'one'), new PhiObjectLiteral(array()))))));
    phiExpressionStatement(new PhiBinaryOperation('=', new PhiDot(new PhiNameRef('package$one'), 'Error'), new PhiNameRef('Error_0')));
    phiExpressionStatement(new PhiBinaryOperation('=', new PhiDot(new PhiNameRef('package$one'), 'Shit'), new PhiNameRef('Shit')));
    phiExpressionStatement(new PhiBinaryOperation('=', new PhiDot(new PhiNameRef('package$one'), 'main_kand9s$'), new PhiNameRef('main')));
    phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNameRef('Kotlin'), 'defineModule'), array(new PhiStringLiteral('phi-gross-test-1'), new PhiNameRef('_'))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef('main'), array(array())));
    return new PhiNameRef('_');
}), array(new PhiConditional(new PhiBinaryOperation('===', new PhiPrefixOperation('typeof', new PhiBrackets(new PhiThis(), new PhiStringLiteral('phi-gross-test-1'))), new PhiStringLiteral('undefined')), new PhiObjectLiteral(array()), new PhiBrackets(new PhiThis(), new PhiStringLiteral('phi-gross-test-1'))), new PhiNameRef('kotlin')))));





























