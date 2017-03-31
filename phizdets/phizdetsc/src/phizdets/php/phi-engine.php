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
     * @throws PhiIllegalStateException
     */
    public function evaluate() {
        // TODO:vgrechka @duplication 6fe21b9f-6bdb-446e-8186-801891740e1b

        $object = $this->qualifier->evaluate();
        if (!($object instanceof PhiObject))
            throw new PhiIllegalStateException("8db9f0c6-b900-4294-a0d9-5dfbc4ccad97");

        return $object->getField($this->name);
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


class PhiObject extends PhiValue {
    /**@var PhiValue[]*/ private $fields = array();

    function __construct() {
    }

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
     * @throws PhiIllegalStateException
     */
    function setField($name, $value) {
        if (gettype($name) !== 'string')
            throw new PhiIllegalStateException("0c2b2e01-6046-4e68-974b-d479cb1b1019");
        $this->fields[$name] = $value;
    }

    /**
     * @return string
     */
    public function typeof() {
        return 'object';
    }

    public function isTruthy() {
        return true;
    }
}

class PhiFunction extends PhiObject {
    /**@var PhiFunctionExpression*/ private $expr;

    /**
     * @param PhiFunctionExpression $expr
     */
    public function __construct(PhiFunctionExpression $expr) {
        parent::__construct();
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
        $res = call_user_func($this->expr->getBody());
        Phi::popEnv();
        return $res;
    }

    /**
     * @return string
     */
    public function typeof() {
        return 'function';
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

    /**
     * @return boolean
     */
    abstract public function isTruthy();
}

class PhiUndefined extends PhiValue {
    public function typeof() {
        return 'undefined';
    }

    public function isTruthy() {
        return false;
    }
}

class PhiNull extends PhiValue {
    /**
     * @return string
     */
    public function typeof() {
        return 'object';
    }

    /**
     * @return boolean
     */
    public function isTruthy() {
        return false;
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
     * @throws PhiIllegalStateException
     */
    public function getVar($name) {
        $value = @$this->vars[$name];
        if ($value !== null) {
            return $value;
        } else {
            if ($this->parent === null) {
                throw new PhiIllegalStateException("Variable `$name` not found");
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
     * @throws PhiIllegalStateException
     */
    public function getThisValue() {
        if ($this->thisValue === null)
            throw new PhiIllegalStateException("`this` is not bound");
        return $this->thisValue;
    }

    /**
     * @param string $varName
     * @param PhiValue $value
     * @throws PhiIllegalStateException
     */
    public function setExistingVarHereOrInParents($varName, $value) {
        if (array_key_exists($varName, $this->vars)) {
            $this->vars[$varName] = $value;
        } else {
            if ($this->parent === null) {
                throw new PhiIllegalStateException("Assigning to non-existent variable `$varName`");
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
    /**@var PhiObject*/ private static $global;

    public static function initEnv() {
        self::$currentEnv = new PhiEnv(null);
        self::$global = new PhiObject();
        self::$currentEnv->setThisValue(self::$global);

        phiExpressionStatement(
            new PhiFunctionExpression('Error', array('message'),
                function() {
                    phiExpressionStatement(
                        new PhiBinaryOperation('=',
                                               new PhiDot(new PhiThis(),
                                                          'message'),
                                               new PhiNameRef('message')));
                }
            )
        );
    }

    public static function initStdlib() {
        phiVars(array(array('kotlin', new PhiObjectLiteral(array(
            // Kotlin.Kind = {CLASS:"class", INTERFACE:"interface", OBJECT:"object"};
            array(new PhiNameRef('Kind'), new PhiObjectLiteral(array(
                array(new PhiNameRef('CLASS'), new PhiStringLiteral('class')),
                array(new PhiNameRef('INTERFACE'), new PhiStringLiteral('interface')),
                array(new PhiNameRef('OBJECT'), new PhiStringLiteral('object')),
            ))),

            // Kotlin.defineModule = function(id, declaration) {}
            array(new PhiNameRef('defineModule'),
                  new PhiFunctionExpression('defineModule',
                                            array('id', 'declaration'),
                                            function() {})),
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
            throw new PhiIllegalStateException("0a0f0cfa-1ccf-4017-b97c-c064ad0b0015");
    }

//    public static function imf($msg) {
//        throw new PhiIllegalStateException("Implement me, please, fuck you. $msg");
//    }
//
//    public static function wtf($msg) {
//        throw new PhiIllegalStateException("WTF: $msg");
//    }
//
//    public static function bitch($msg) {
//        throw new PhiIllegalStateException($msg);
//    }

    public static function check($cond, $msg) {
        if (!$cond) {
            throw new PhiIllegalStateException($msg);
        }
    }

    /**
     * @param PhiValue $phiValue
     * @return mixed
     * @throws PhiIllegalStateException
     */
    public static function phiValueToNative($phiValue) {
        if ($phiValue instanceof PhiNull || $phiValue instanceof PhiUndefined) {
            return null;
        }
        else if ($phiValue instanceof PhiString) {
            return $phiValue->getValue();
        }
        else {
            throw new PhiIllegalStateException("7d8c0fe9-e2f2-4fa1-9e5e-7aa1f5079f9a");
        }
    }

    /**
     * @param mixed $nativeValue
     * @return PhiValue
     * @throws PhiIllegalStateException
     */
    public static function nativeToPhiValue($nativeValue) {
        if (gettype($nativeValue) === 'NULL') {
            return new PhiNull();
        }
        else if (gettype($nativeValue) === 'string') {
            return new PhiString($nativeValue);
        }
        else {
            throw new PhiIllegalStateException("02db567a-dc19-4a51-9d4e-5a8c22178d59");
        }
    }
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
     * @throws PhiIllegalStateException
     */
    public function evaluate() {
        // Example:
        //     function Shit(a, b) {this.a = a; this.b = b}
        //     new Shit(a, b)

        $ctorValue = $this->ctor->evaluate();
        if (!($ctorValue instanceof PhiFunction))
            throw new PhiIllegalStateException("d3b13f4a-b3d3-4b80-b9e0-45ac964f08f8");
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

    public function isTruthy() {
        return $this->value;
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
     * @throws PhiIllegalStateException
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
                throw new PhiIllegalStateException("8614085d-5212-46f6-9516-f256e02788b0    lhsValue is $lhsValueClass; rhsValue is $rhsValueClass");
            }
        }
        else if ($this->op === '=') {
            if ($this->lhs instanceof PhiNameRef) {
                // Ex: a = b
                $varName = $this->lhs->getName();
                Phi::getCurrentEnv()->setExistingVarHereOrInParents($varName, $rhsValue);
                return $rhsValue;
            }
            else if ($this->lhs instanceof PhiDot) {
                // Ex: fucking(shit()).b = c
                $object = $this->lhs->getQualifier()->evaluate();
                if (!($object instanceof PhiObject))
                    throw new PhiIllegalStateException("36107ced-0278-40c0-a2ba-3742aeb4330c");
                $fieldName = $this->lhs->getName();
                $object->setField($fieldName, $rhsValue);
                return $rhsValue;
            }
            else if ($this->lhs instanceof PhiBrackets) {
                // Ex: fucking(shit())[ebanoe(govno())] = c
                // TODO:vgrechka @duplication fd1f1faa-e7d1-49ad-9e58-1892cd15ecff

                $objectPhiValue = $this->lhs->getObject()->evaluate();
                if (!($objectPhiValue instanceof PhiObject))
                    throw new PhiIllegalStateException("ef6e4743-1a4e-4de3-8af8-338277b62901");

                $indexPhiValue = $this->lhs->getIndex()->evaluate();
                if (!($indexPhiValue instanceof PhiString) && !($indexPhiValue instanceof PhiNumber))
                    throw new PhiIllegalStateException("f063cb97-2e17-491c-ae25-b50610504d19");

                $fieldName = strval($indexPhiValue->getValue());
                return $objectPhiValue->getField($fieldName);
            }
            else {
                throw new PhiIllegalStateException("a7b2e444-2530-4a2c-a3cf-eb821e3f440b");
            }
        }
        else if ($this->op === '||') {
            $lhsPhiValue = $this->lhs->evaluate();
            if ($lhsPhiValue->isTruthy()) {
                return $lhsPhiValue;
            } else {
                return $this->rhs->evaluate();
            }
        }
        else if ($this->op === '+') {
            $lhsPhiValue = $this->lhs->evaluate();
            $rhsPhiValue = $this->rhs->evaluate();
            if ($lhsPhiValue instanceof PhiString && $rhsPhiValue instanceof PhiString) {
                return new PhiString($lhsPhiValue->getValue() . $rhsPhiValue->getValue());
            }
            else {
                throw new PhiIllegalStateException("d79e06d2-dcc0-4d51-a73a-0585c168e404");
            }
        }
        else {
            throw new PhiIllegalStateException("6bb8ca7a-c00a-4f60-9930-211dba14c031    op = {$this->op}");
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

class PhiNumberLiteral extends PhiExpression {
    /**@var double*/ private $value;

    /**
     * @param double $value
     */
    public function __construct($value) {
        $this->value = $value;
    }

    /**
     * @return PhiValue
     */
    public function evaluate() {
        return new PhiNumber($this->value);
    }

    /**
     * @return double
     */
    public function getValue() {
        return $this->value;
    }
}

class PhiStringLiteral extends PhiExpression {
    /**@var string*/ private $value;

    /**
     * @param string $value
     */
    public function __construct($value) {
        // `\/` in JS is `/`
        $this->value = mb_ereg_replace('\\\\/', '/', $value);
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

class PhiNumber extends PhiValue {
    /**@var double*/ private $value;

    /**
     * @param double $value
     */
    public function __construct($value) {
        $this->value = $value;
    }

    /**
     * @return double
     */
    public function getValue() {
        return $this->value;
    }

    /**
     * @return double
     */
    public function typeof() {
        return 'number';
    }

    public function isTruthy() {
        return $this->value !== 0.0 && $this->value !== 0;
    }
}

class PhiString extends PhiValue {
    /**@var string*/ private $value;

    /**
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

    public function isTruthy() {
        return $this->value !== '';
    }
}

/**
 * @param PhiExpression $expr
 */
function phiExpressionStatement($expr) {
    if ($expr instanceof PhiFunctionExpression) {
        Phi::getCurrentEnv()->setVar($expr->getName(), $expr->evaluate());
    } else {
        $expr->evaluate();
    }
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
 * @return bool
 * @throws PhiIllegalStateException
 */
function phiEvaluateToBoolean($expr) {
    $phiValue = $expr->evaluate();
    if (!($phiValue instanceof PhiBoolean))
        throw new PhiIllegalStateException("0c8e6543-b05c-4852-a200-185ac3b36632");
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
     * @throws PhiIllegalStateException
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
            throw new PhiIllegalStateException("f88f33d3-2868-43d5-82b7-a1b8c20fc1cb");
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
     * @throws PhiIllegalStateException
     */
    public function evaluate() {
        // TODO:vgrechka @duplication fd1f1faa-e7d1-49ad-9e58-1892cd15ecff

        $objectPhiValue = $this->object->evaluate();
        if (!($objectPhiValue instanceof PhiObject))
            throw new PhiIllegalStateException("14c25e26-1fad-481d-9d9c-6a9ee74fd815");

        $indexPhiValue = $this->index->evaluate();
        if (!($indexPhiValue instanceof PhiString) && !($indexPhiValue instanceof PhiNumber))
            throw new PhiIllegalStateException("d27c6721-2fb0-42a5-b20c-96239a66bdda");

        $fieldName = strval($indexPhiValue->getValue());
        return $objectPhiValue->getField($fieldName);
    }
}

class PhiInvocation extends PhiExpression {
    /**@var PhiExpression*/ private $callee;
    /**@var PhiExpression[]*/ private $args;

    /**
     * @param PhiExpression $callee
     * @param PhiExpression[] $args
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
     * @return PhiExpression[]
     */
    public function getArgs() {
        return $this->args;
    }

    /**
     * @return PhiValue
     * @throws PhiIllegalStateException
     */
    public function evaluate() {
        /**@var PhiObject $receiverPhiValue*/
        /**@var PhiFunction $calleePhiValue*/

        if ($this->callee instanceof PhiDot) {
            // TODO:vgrechka @duplication 6fe21b9f-6bdb-446e-8186-801891740e1b

            $receiverPhiValue = $this->callee->getQualifier()->evaluate();
            if (!($receiverPhiValue instanceof PhiObject))
                throw new PhiIllegalStateException("4720e481-3172-4ce3-b80c-0b8166126f0e");

            $calleePhiValue = $receiverPhiValue->getField($this->callee->getName());
        }
        else if ($this->callee instanceof PhiBrackets) {
            // TODO:vgrechka @duplication fd1f1faa-e7d1-49ad-9e58-1892cd15ecff

            $receiverPhiValue = $this->callee->getObject()->evaluate();
            if (!($receiverPhiValue instanceof PhiObject))
                throw new PhiIllegalStateException("cb9ee3b6-21ef-4cfc-a385-6f7ac55c8aab");

            $indexPhiValue = $this->callee->getIndex()->evaluate();
            if (!($indexPhiValue instanceof PhiString) && !($indexPhiValue instanceof PhiNumber))
                throw new PhiIllegalStateException("21e82a53-d99e-43b1-8aa2-9de29f8042c9");

            $fieldName = strval($indexPhiValue->getValue());
            $calleePhiValue = $receiverPhiValue->getField($fieldName);
        }
        else if ($this->callee instanceof PhiExternalNameRef) {
            $nativeArgs = array();
            foreach ($this->args as $argExpression) {
                $argPhiValue = $argExpression->evaluate();
                $argNativeValue = Phi::phiValueToNative($argPhiValue);
                array_push($nativeArgs, $argNativeValue);
            }
            $nativeRes = call_user_func_array($this->callee->getName(), $nativeArgs);
            return Phi::nativeToPhiValue($nativeRes);
        }
        else {
            $receiverPhiValue = new PhiUndefined();
            $calleePhiValue = $this->callee->evaluate();
        }

        if (!($calleePhiValue instanceof PhiFunction))
            throw new PhiIllegalStateException("f30e6af1-a2b9-4345-8922-51caa7ba7bcb");

        $argPhiValues = array();
        foreach ($this->args as $arg)
            array_push($argPhiValues, $arg->evaluate());

        return $calleePhiValue->invoke($receiverPhiValue, $argPhiValues);
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
     * @throws PhiIllegalStateException
     */
    public function evaluate() {
        throw new PhiIllegalStateException("9ef6218c-e6bd-4394-acdf-ca73a469b33c");
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
        $ifPhiValue = $this->if->evaluate();
        if ($ifPhiValue->isTruthy())
            return $this->then->evaluate();
        else
            return $this->else->evaluate();
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
     * @throws PhiIllegalStateException
     */
    public function evaluate() {
        $object = new PhiObject();
        foreach ($this->keyValuePairs as $pair) {
            /**@var string key*/
            /**@var PhiExpression $keyExpression*/
            $keyExpression = $pair[0];
            if ($keyExpression instanceof PhiStringLiteral || $keyExpression instanceof PhiNumberLiteral) {
                $keyPhiValue = $keyExpression->evaluate();
                if (!($keyPhiValue instanceof PhiString || $keyPhiValue instanceof PhiNumber))
                    throw new PhiIllegalStateException("204f554b-b5e8-44ea-996b-99e178e991be");
                $key = strval($keyPhiValue->getValue());
            }
            else if ($keyExpression instanceof PhiNameRef) {
                $key = $keyExpression->getName();
            }
            else {
                throw new PhiIllegalStateException("4610af55-172a-41c3-aa13-433d981b6eb1");
            }

            /**@var PhiExpression $valueExpression*/
            $valueExpression = $pair[1];
            if (!($valueExpression instanceof PhiExpression))
                throw new PhiIllegalStateException("753209fd-1f57-4b4c-8a8a-a38685acaca3");

            $object->setField($key, $valueExpression->evaluate());
        }
        return $object;
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

class PhiArray extends PhiObject {
    /**@var PhiValue[]*/ private $items = array();

    /**
     * @param PhiValue $x
     */
    function push($x) {
        array_push($this->items, $x);
    }

    /**
     * @return string
     */
    public function typeof() {
        return 'object';
    }

    /**
     * @return boolean
     */
    public function isTruthy() {
        return true;
    }
}

class PhiArrayLiteral extends PhiExpression {
    /**@var PhiExpression[]*/ private $items;

    /**
     * @param PhiExpression[] $items
     */
    public function __construct(array $items) {
        $this->items = $items;
    }

    /**
     * @return PhiExpression[]
     */
    public function getItems() {
        return $this->items;
    }

    /**
     * @return PhiValue
     */
    public function evaluate() {
        $arr = new PhiArray();
        foreach ($this->items as $item)
            $arr->push($item->evaluate());
        return $arr;
    }
}

function phiPrint($x) {
    echo $x;
}


// ==================================== ENTRY ======================================


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







Phi::initEnv();
Phi::initStdlib();


if (phiEvaluateToBoolean(new PhiBinaryOperation('===', new PhiPrefixOperation('typeof', new PhiNameRef('kotlin')), new PhiStringLiteral('undefined')))) {
    phiThrow(new PhiNew(new PhiNameRef('Error'), array(new PhiStringLiteral("Error loading module 'phi-gross-test-1'. Its dependency 'kotlin' was not found. Please, check whether 'kotlin' is loaded prior to 'phi-gross-test-1'."))));
}
phiExpressionStatement(new PhiBinaryOperation('=', new PhiBrackets(new PhiThis(), new PhiStringLiteral('phi-gross-test-1')), new PhiInvocation(new PhiFunctionExpression(null, array('_', 'Kotlin'), function () {
    phiExpressionStatement(new PhiStringLiteral('use strict'));
    phiExpressionStatement(new PhiFunctionExpression('Shit', array('name', 'text'), function () {
        phiExpressionStatement(new PhiBinaryOperation('=', new PhiDot(new PhiThis(), 'name'), new PhiNameRef('name')));
        phiExpressionStatement(new PhiBinaryOperation('=', new PhiDot(new PhiThis(), 'text'), new PhiNameRef('text')));
    }));
    phiExpressionStatement(new PhiBinaryOperation('=', new PhiDot(new PhiNameRef('Shit'), '$metadata$'), new PhiObjectLiteral(array(array(new PhiNameRef('kind'), new PhiDot(new PhiDot(new PhiNameRef('Kotlin'), 'Kind'), 'CLASS')), array(new PhiNameRef('simpleName'), new PhiStringLiteral('Shit')), array(new PhiNameRef('interfaces'), new PhiArrayLiteral(array()))))));
    phiExpressionStatement(new PhiFunctionExpression('main', array('args'), function () {
        phiExpressionStatement(new PhiInvocation(new PhiNameRef('sayShit'), array(new PhiNew(new PhiNameRef('Shit'), array(new PhiStringLiteral('Archibald'), new PhiStringLiteral('Fuck you'))))));
    }));
    phiExpressionStatement(new PhiFunctionExpression('sayShit', array('shit'), function () {
        phiExpressionStatement(new PhiInvocation(new PhiExternalNameRef('phiPrint'), array(new PhiBinaryOperation('+', new PhiBinaryOperation('+', new PhiBinaryOperation('+', new PhiBinaryOperation('+', new PhiStringLiteral('<b>'), new PhiDot(new PhiNameRef('shit'), 'text')), new PhiStringLiteral(', ')), new PhiDot(new PhiNameRef('shit'), 'name')), new PhiStringLiteral('<\/b>')))));
    }));
    phiVars(array(array('package$phi', new PhiBinaryOperation('||', new PhiDot(new PhiNameRef('_'), 'phi'), new PhiBinaryOperation('=', new PhiDot(new PhiNameRef('_'), 'phi'), new PhiObjectLiteral(array()))))));
    phiVars(array(array('package$gross', new PhiBinaryOperation('||', new PhiDot(new PhiNameRef('package$phi'), 'gross'), new PhiBinaryOperation('=', new PhiDot(new PhiNameRef('package$phi'), 'gross'), new PhiObjectLiteral(array()))))));
    phiVars(array(array('package$test', new PhiBinaryOperation('||', new PhiDot(new PhiNameRef('package$gross'), 'test'), new PhiBinaryOperation('=', new PhiDot(new PhiNameRef('package$gross'), 'test'), new PhiObjectLiteral(array()))))));
    phiVars(array(array('package$one', new PhiBinaryOperation('||', new PhiDot(new PhiNameRef('package$test'), 'one'), new PhiBinaryOperation('=', new PhiDot(new PhiNameRef('package$test'), 'one'), new PhiObjectLiteral(array()))))));
    phiExpressionStatement(new PhiBinaryOperation('=', new PhiDot(new PhiNameRef('package$one'), 'Shit'), new PhiNameRef('Shit')));
    phiExpressionStatement(new PhiBinaryOperation('=', new PhiDot(new PhiNameRef('package$one'), 'main_kand9s$'), new PhiNameRef('main')));
    phiExpressionStatement(new PhiInvocation(new PhiDot(new PhiNameRef('Kotlin'), 'defineModule'), array(new PhiStringLiteral('phi-gross-test-1'), new PhiNameRef('_'))));
    phiExpressionStatement(new PhiInvocation(new PhiNameRef('main'), array(new PhiArrayLiteral(array()))));
    return new PhiNameRef('_');
}), array(new PhiConditional(new PhiBinaryOperation('===', new PhiPrefixOperation('typeof', new PhiBrackets(new PhiThis(), new PhiStringLiteral('phi-gross-test-1'))), new PhiStringLiteral('undefined')), new PhiObjectLiteral(array()), new PhiBrackets(new PhiThis(), new PhiStringLiteral('phi-gross-test-1'))), new PhiNameRef('kotlin')))));




























