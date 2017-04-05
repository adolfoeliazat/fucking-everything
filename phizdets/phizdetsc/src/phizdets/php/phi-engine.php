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
        if (!($object instanceof PhiObject)) {
            if ($object instanceof PhiString) {
                $String = phiEvaluate(new PhiNameRef('String'));
                if (!($String instanceof PhiFunction))
                    throw new PhiIllegalStateException('825979da-9e92-45f4-882a-f667d895f9ad');

                $object = phiNew($String, array($object));
            }
            else {
                throw new PhiIllegalStateException("{$this->shitToName($this->qualifier)} is not an object    8db9f0c6-b900-4294-a0d9-5dfbc4ccad97");
            }
        }

        return $object->getProperty($this->name);
    }

    function shitToName($shit) {
        if ($shit instanceof PhiNameRef)
            return $shit->getName();
        if ($shit instanceof PhiDot) {
            return $this->shitToName($shit->getQualifier()) . '.' . $shit->getName();
        }
        return '?';
    }

    function __toString() {
        return "{$this->qualifier}.{$this->name}";
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

    function __toString() {
        return "[TODO: " . get_class($this) . ".__toString]";
    }
}


class PhiObject extends PhiValue {
    /**@var PhiValue[]*/ protected $fields = array();
    /**@var PhiObject[]*/ protected $props = array();

    /**
     * @param array $opts
     */
    function __construct($opts = array()) {
        /**@var PhiValue $proto*/
        if (!array_key_exists('proto', $opts)) {
            $proto = Phi::$Object_prototype;
        } else {
            $proto = $opts['proto'];
        }

//        /**@var PhiValue $constructor*/
//        if (!array_key_exists('constructor', $opts)) {
//            $constructor = Phi::$Object;
//        } else {
//            $constructor = $opts['constructor'];
//        }

        $this->fields['__proto__'] = $proto;
//        $this->fields['constructor'] = $constructor;
    }

//    /**
//     * @param string $name
//     * @return PhiValue
//     */
//    function getField($name) {
//        $value = @$this->fields[$name];
//        if ($value === null)
//            return new PhiUndefined();
//        else
//            return $value;
//    }

    /**
     * @param string $name
     * @param array $opts
     * @return PhiValue
     * @throws PhiIllegalStateException
     */
    function getProperty($name, $opts = array()) {
        if (array_key_exists($name, $this->fields)) {
            return $this->fields[$name];
        }

        if (array_key_exists($name, $this->props)) {
            $prop = $this->props[$name];
            $getter = $prop->getProperty('get');
            if ($getter->isTruthy()) {
                if (!($getter instanceof PhiFunction))
                    throw new PhiIllegalStateException("a88c164b-b4a9-4228-9b0a-faf92cc797cd");
                $receiver = @$opts['receiver'] ?: $this;
                $res = $getter->invoke($receiver, array());
                return $res;
            }
        }

        $proto = $this->getProto();
        if ($proto != null) {
            return $proto->getProperty($name, array('receiver' => @$opts['receiver'] ?: $this));
        } else {
            if (@$opts['phpNullIfNotFound'])
                return null;
            else
                return new PhiUndefined();
        }
    }

    /**
     * @return PhiObject
     * @throws PhiIllegalStateException
     */
    public function getProto() {
        $proto = $this->fields['__proto__'];
        if ($proto instanceof PhiObject) {
            return $proto;
        }
        else if ($proto instanceof PhiNull) {
            return null;
        }
        else {
            throw new PhiIllegalStateException("d9db15b4-22d9-4710-b0b8-98c1cc5f96a0");
        }
    }

    /**
     * @param string $name
     * @param PhiValue $value
     * @throws PhiIllegalStateException
     */
    function setProperty($name, $value) {
        if (gettype($name) !== 'string')
            throw new PhiIllegalStateException("0c2b2e01-6046-4e68-974b-d479cb1b1019");

        $obj = $this;
        while ($obj != null) {
            if (array_key_exists($name, $obj->props)) {
                $prop = $obj->props[$name];
                $setter = $prop->getProperty('set');
                if ($setter->isTruthy()) {
                    if (!($setter instanceof PhiFunction))
                        throw new PhiIllegalStateException("e4823d52-c6cb-4294-8fd6-bc5e3ce73640");
                    $setter->invoke($this, array($value));
                    return;
                }
            }

            $obj = $obj->getProto();
        }

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

    /**
     * @param string $name
     * @param PhiObject $descriptorPhiObject
     * @throws PhiIllegalStateException
     */
    public function defineProperty($name, $descriptorPhiObject) {
        $this->props[$name] = $descriptorPhiObject;

//        $set = @$descriptorPhiObject->fields['set'];
//        if ($set !== null) {
//            throw new PhiIllegalStateException("Implement me d7861e2e-e927-437d-9461-f46ca6576c7e");
//        }
//
//        $value = @$descriptorPhiObject->fields['value'];
//        if ($value !== null) {
//            throw new PhiIllegalStateException("Implement me dfee054f-3d16-45ff-8b35-21eff5792c6d");
//        }
//
//        $get = @$descriptorPhiObject->fields['get'];
//        if ($get !== null) {
//            if (!($get instanceof PhiFunction))
//                throw new PhiIllegalStateException("fb6b0629-8ce8-429b-a7f0-0ac9bafd96fd");
//        }
//
//        throw new PhiIllegalStateException("3e309b2d-d908-4544-876c-f461c10d4d13");
    }

    public function getOwnPropertyDescriptor($name) {
        if (array_key_exists($name, $this->fields)) {
            $descr = new PhiObject();
            $descr->setProperty('value', $this->fields[$name]);
            return $descr;
        }

        if (array_key_exists($name, $this->props)) {
            return $this->props[$name];
        }

        return new PhiUndefined();
    }
}

class PhiFunction extends PhiObject {
    /**@var PhiFunctionExpression*/ private $expr;
    /**@var boolean vararg*/ private $vararg;
    /**@var PhiEnv*/ private $definitionEnv;

    /**
     * @param PhiFunctionExpression $expr
     * @param array $opts
     */
    public function __construct($expr, $opts = array()) {
        parent::__construct();
        $this->definitionEnv = $definitionEnv = Phi::getCurrentEnv();
        $this->expr = $expr;
        $this->vararg = (boolean) @$opts['vararg'];

        { // me.__proto__ = Function.prototype
            $this->fields['__proto__'] = Phi::$Function_prototype;
        }

        { // me.prototype = {}
            $prototype = new PhiObject();
            $prototype->fields['constructor'] = $this;
            $this->fields['prototype'] = $prototype;
        }
    }

    /**
     * @param PhiObject|PhiNull $receiver
     * @param PhiValue[] $args
     * @return PhiValue
     */
    public function invoke($receiver, $args) {
        $debugID = Phi::nextDebugID();
//        if ($debugID === 1798) {
//            strval('break on me');
//        }

        $oldEnv = Phi::getCurrentEnv();
        $newEnv = new PhiEnv($this->definitionEnv);
        $newEnv->setFunctionArgs($args);
        $newEnv->setThisValue($receiver);
        if (!$this->vararg) {
            for ($i = 0; $i < count($args); ++$i) {
                $name = $this->expr->getArgNames()[$i];
                $newEnv->setVar($name, $args[$i]);
            }
        }
        Phi::setCurrentEnv($newEnv);

        try {
            $res = call_user_func($this->expr->getBody());
            return $res;
        } finally {
            Phi::setCurrentEnv($oldEnv);
        }
    }

    /**
     * @return string
     */
    public function typeof() {
        return 'function';
    }

    /**
     * @return PhiFunctionExpression
     */
    public function getExpr() {
        return $this->expr;
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

    /**
     * @return boolean
     */
    public final function isFalsy() {return !$this->isTruthy();}
}

class PhiUndefined extends PhiValue {
    public function typeof() {
        return 'undefined';
    }

    public function isTruthy() {
        return false;
    }
}

class PhiNullLiteral extends PhiExpression {
    /**@var string*/ private $debugTag;

    /**
     * @param $debugTag
     */
    public function __construct($debugTag) {
        $this->debugTag = $debugTag;
    }

    /**
     * @return PhiValue
     */
    public function evaluate() {
        return new PhiNull();
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
    /**@var PhiValue[]*/ private $functionArgs;

    /**
     * @param PhiEnv $parent
     */
    function __construct($parent) {
        $this->parent = $parent;
    }

    /**
     * @param PhiValue[] $functionArgs
     */
    public function setFunctionArgs($functionArgs) {
        $this->functionArgs = $functionArgs;
    }

    /**
     * @return PhiValue[]
     * @throws PhiIllegalStateException
     */
    public function getFunctionArgs() {
        if ($this->functionArgs === null)
            throw new PhiIllegalStateException("1f43259d-e1cd-4f78-a527-017aa01d80af");
        return $this->functionArgs;
    }

    /**
     * @param string $name
     * @param PhiValue $value
     * @return void
     */
    public function setVar($name, $value) {
// @debug
//        if ($name === 'Array') {
//            count('break on me');
//        }
        $this->vars[$name] = $value;
    }

    /**
     * @param string $name
     * @return PhiValue
     * @throws PhiIllegalStateException
     */
    public function getVar($name) {
        if ($name === 'this')
            return $this->getThisValue();

        $value = @$this->vars[$name];
        if ($value !== null) {
            return $value;
        } else {
            if ($this->parent === null) {
                $value = Phi::$global->getProperty($name, array('phpNullIfNotFound' => true));
                if ($value == null) {
                    throw new PhiIllegalStateException("Variable `$name` not found");
                } else {
                    return $value;
                }
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
                $value = Phi::$global->getProperty($varName, array('phpNullIfNotFound' => true));
                return $value !== null;
            } else {
                return $this->parent->hasVar($varName);
            }
        }
    }
}

function fuck() {
    strval('break on me');
}

class Phi {
    /**@var string*/ public static $debugContext;
    /**@var PhiEnv*/ private static $currentEnv;
    /**@var PhiObject*/ public static $global;
    /**@var PhiObject*/ public static $Object_prototype;
    /**@var PhiFunction*/ public static $Object;
    /**@var PhiObject*/ public static $Function_prototype;
    /**@var PhiObject*/ public static $Array_prototype;
    /**@var PhiObject*/ public static $Array;
    /**@var PhiExpression*/ public static $phiExpressionStatement_expr;
    /**@var number*/ private static $nextDebugID;

    public static function nextDebugID() {
        return self::$nextDebugID++;
    }

    public static function initEnv() {
        self::$currentEnv = new PhiEnv(null);
        self::$global = new PhiObject(array('proto' => new PhiNull()));
        self::$currentEnv->setThisValue(self::$global);
        self::$currentEnv->setVar('undefined', new PhiUndefined());
        self::$currentEnv->setVar('global', self::$global);

        $Object_prototype = new PhiObject(array('proto' => new PhiNull()));
        self::$Object_prototype = $Object_prototype;
        $Function_prototype = new PhiObject(array('proto' => $Object_prototype));
        self::$Function_prototype = $Function_prototype;
        $Array_prototype = new PhiObject(array('proto' => $Object_prototype));
        self::$Array_prototype = $Array_prototype;

        $Object = new PhiFunction(new PhiFunctionExpression('Object', array(), function() {}));
        self::$Object = $Object;
//        $Object = new PhiObject(array('proto' => $Function_prototype));
        $Object->setProperty('prototype', $Object_prototype);
        $Object_prototype->setProperty('constructor', $Object);
        self::$currentEnv->setVar('Object', $Object);

        $Function = new PhiObject(array('proto' => $Function_prototype));
        $Function->setProperty('prototype', $Function_prototype);
        self::$currentEnv->setVar('Function', $Function);

        $Function_prototype->setProperty('call', new PhiFunction(new PhiFunctionExpression('call', array(), function() {
// @debug
//            if (Phi::$debugContext === 'phiQuickTest_functionDotCall-1') {
//                count('break on me');
//            }

            $e = new PhiThis();
            $thisFunction = $e->evaluate();
            if (!($thisFunction instanceof PhiFunction))
                throw new PhiIllegalStateException("f1bd87fd-9106-44a4-9a11-09c8c8932ba7");

            $callArgs = Phi::getCurrentEnv()->getFunctionArgs();
            $receiver = $callArgs[0];
            if (!($receiver instanceof PhiObject || $receiver instanceof PhiNull))
                throw new PhiIllegalStateException("26e3ff5f-5ddb-4ac9-a9a6-3bf0c36de8a3");
            $args = array_slice($callArgs, 1);
            return $thisFunction->invoke($receiver, $args);
        }), array('vararg' => true)));

//        { // ---------------------- Object.prototype.* ---------------------
//
//            $descr = new PhiObject();
//            $descr->setProperty('get', new PhiFunction(new PhiFunctionExpression('get-constructor', array(), function() {
//                $thiz = Phi::getCurrentEnv()->getThisValue();
//                if (!($thiz instanceof PhiObject))
//                    throw new PhiIllegalStateException('c699617f-2d08-4f33-bf56-438bd7312f85');
//
//
//            })));
//        }

        { // ---------------------- Object.* ---------------------

            $Object->setProperty('defineProperty', new PhiFunction(
                new PhiFunctionExpression(
                    'defineProperty',
                    array('obj', 'prop', 'descriptor'),
                    function() {
                        $obj = Phi::getCurrentEnv()->getVar('obj');
                        if (!($obj instanceof PhiObject))
                            throw new PhiIllegalStateException("c2c26a66-b8f0-4e24-aeab-f6f11a6eed9c");

                        $prop = Phi::getCurrentEnv()->getVar('prop');
                        if (!($prop instanceof PhiString))
                            throw new PhiIllegalStateException("f30171f1-3e51-4cf9-848a-ff19d4c7d197");

                        $descriptor = Phi::getCurrentEnv()->getVar('descriptor');
                        if (!($descriptor instanceof PhiObject))
                            throw new PhiIllegalStateException("9156fc1b-daaf-492f-822e-07da868471bc");

                        $obj->defineProperty($prop->getValue(), $descriptor);
                    })));

            $Object->setProperty('create', new PhiFunction(
                new PhiFunctionExpression(
                    'create',
                    array('proto'),
                    function() {
                        $proto = Phi::getCurrentEnv()->getVar('proto');
                        if (!($proto instanceof PhiObject || $proto instanceof PhiNull))
                            throw new PhiIllegalStateException("14d297f3-8690-44f2-89ca-36ae809c5637");

                        return new PhiObject(array('proto' => $proto));
                    })));

            $Object->setProperty('getOwnPropertyDescriptor', new PhiFunction(
                new PhiFunctionExpression(
                    'getOwnPropertyDescriptor', array('obj', 'prop'),
                    function() {
                        $obj = Phi::getCurrentEnv()->getVar('obj');
                        if (!($obj instanceof PhiObject))
                            throw new PhiIllegalStateException("2e66da9d-bc59-44a8-86f1-d25ec59ffbfc");

                        $prop = Phi::getCurrentEnv()->getVar('prop');
                        if (!($prop instanceof PhiString))
                            throw new PhiIllegalStateException("eb91d3f2-09b6-4382-b4c2-84c244990bd3");

                        return $obj->getOwnPropertyDescriptor($prop->getValue());
                    })));

            $Object->setProperty('getPrototypeOf', new PhiFunction(
                new PhiFunctionExpression(
                    'getPrototypeOf', array('x'),
                    function() {
                        $x = Phi::getCurrentEnv()->getVar('x');
                        if (!($x instanceof PhiObject))
                            throw new PhiIllegalStateException("ba06df93-f674-4312-853d-6b059edb0ee2");

                        return $x->getProperty("__proto__");
                    })));
        }


        $Array = new PhiObject(array('proto' => $Function_prototype));
        self::$Array = $Array;
        $Array->setProperty('prototype', $Array_prototype);
        $Array_prototype->setProperty('constructor', $Array);
        self::$currentEnv->setVar('Array', $Array);

        { //----------------------- Array.* ------------------------

            $Array->setProperty('isArray', new PhiFunction(new PhiFunctionExpression(
                'isArray', array('x'), function() {
                $x = Phi::getCurrentEnv()->getVar('x');
                return new PhiBoolean($x instanceof PhiArray);
            })));
        }


        { // String
            phiExpressionStatement(new PhiFunctionExpression(
                'String', array('value'),
                function() {
                    $value = Phi::getCurrentEnv()->getVar('value');
                    if (!($value instanceof PhiString))
                        throw new PhiIllegalStateException('1891e851-1265-416a-abde-49ec99fd00a4');

                    $thiz = Phi::getCurrentEnv()->getThisValue();

                    // Syntax: String('...') -- without `new`
                    if ($thiz instanceof PhiUndefined) {
                        // throw new PhiIllegalStateException('aaaaaaaaaa');
                        $thiz = new PhiObject();
                    }

                    if (!($thiz instanceof PhiObject))
                        throw new PhiIllegalStateException('0a559137-c3e7-4dfe-b5a2-c6a23125263b');

                    // $thiz->setProperty('_value', $value);

                    // To make `Kotlin.hashCode = function() {...}` happy
                    $thiz->setProperty('hashCode', new PhiUndefined());

                    $thiz->setProperty('length', new PhiNumber(mb_strlen($value->getValue(), 'UTF-8')));

                    $thiz->setProperty('toString', phiEvaluate(new PhiFunctionExpression(
                        'toString', array(),
                        function() use ($value) {
                            return $value;
                        }
                    )));

                    $thiz->setProperty('charCodeAt', phiEvaluate(new PhiFunctionExpression(
                        'charCodeAt', array('index'),
                        function() use ($value) {
                            $index = Phi::getCurrentEnv()->getVar('index');
                            if (!($index instanceof PhiNumber))
                                throw new PhiIllegalStateException('1891ec42f5ea8-f714-4d91-b277-1b076a6e39a6851-1265-416a-abde-49ec99fd00a4');

                            $string = $value->getValue();
                            $offset = $index->getValue();
                            $len = mb_strlen($string, 'UTF-8');
                            if ($offset < 0 || $offset > $len - 1)
                                return new PhiNumber(NAN);

                            $character = mb_substr($string, $offset, 1, 'UTF-8');
                            list(, $ret) = unpack('S', mb_convert_encoding($character, 'UTF-16LE', 'UTF-8'));
                            return new PhiNumber($ret);
                        }
                    )));

                    return $thiz;
                }
            ));
        }



        { // Invariants
            { // Object.prototype.__proto__ === null
                $e = new PhiNameRef('Object');
                $e = new PhiDot($e, 'prototype');
                $e = new PhiDot($e, '__proto__');
                phiAssert($e->evaluate() instanceof PhiNull);
            }
            { // Object.__proto__ === Function.prototype
                $e = new PhiNameRef('Object');
                $e = new PhiDot($e, '__proto__');
                phiAssert($e->evaluate() === $Function_prototype);
            }
            { // Object.__proto__.__proto__ === Object.prototype
                $e = new PhiNameRef('Object');
                $e = new PhiDot($e, '__proto__');
                $e = new PhiDot($e, '__proto__');
                phiAssert($e->evaluate() === $Object_prototype);
            }

            { // Function.__proto__ === Function.prototype
                $e = new PhiNameRef('Function');
                $e = new PhiDot($e, '__proto__');
                phiAssert($e->evaluate() === $Function_prototype);
            }
            { // Function.prototype.__proto__ === Object.prototype
                $e = new PhiNameRef('Function');
                $e = new PhiDot($e, 'prototype');
                $e = new PhiDot($e, '__proto__');
                phiAssert($e->evaluate() === $Object_prototype);
            }
            { // 'function'  EQ  typeof Function.prototype.call
                phiEvaluateAndAssertToStringEquals(
                    new PhiString('function'),
                    new PhiUnaryOperation('@@', 'prefix', 'typeof',
                        new PhiDot(new PhiDot(new PhiNameRef('Function'), 'call'), 'call')));
            }

            { // Array.__proto__ === Function.prototype
                $e = new PhiNameRef('Array');
                $e = new PhiDot($e, '__proto__');
                phiAssert($e->evaluate() === $Function_prototype);
            }
            { // Array.prototype.__proto__ === Object.prototype
                $e = new PhiNameRef('Array');
                $e = new PhiDot($e, 'prototype');
                $e = new PhiDot($e, '__proto__');
                phiAssert($e->evaluate() === $Object_prototype);
            }
        }

        phiExpressionStatement(
            new PhiFunctionExpression('Error', array('message'),
                function() {
//                    fuck();
                    if (Phi::getCurrentEnv()->hasVar('message')) {
                        phiExpressionStatement(
                            new PhiBinaryOperation(
                                '@@', '=',
                                new PhiDot(new PhiThis(), 'message'),
                                new PhiNameRef('message')));
                    }
                }
            )
        );
    }

    public static function initStdlib() {
        phiVars('@@initStdlib',
            array(
//                array('String', new PhiObjectLiteral('@@String', array(
//                    array(new PhiNameRef('prototype'), new PhiObjectLiteral('@@String.prototype', array()))
//                ))),

                array('Number', new PhiObjectLiteral('@@Number', array(
                    array(new PhiNameRef('prototype'), new PhiObjectLiteral('@@Number.prototype', array()))
                ))),

                array('Math', new PhiObjectLiteral('@@Math', array(
                ))),
            )
        );
    }

    /**
     * @return PhiEnv
     */
    public static function getCurrentEnv() {
        return self::$currentEnv;
    }

    /**
     * @param PhiEnv $currentEnv
     */
    public static function setCurrentEnv($currentEnv) {
        self::$currentEnv = $currentEnv;
    }

//    public static function pushEnv() {
//        self::$currentEnv = new PhiEnv(self::$currentEnv);
//    }
//
//    public static function popEnv() {
//        self::$currentEnv = self::$currentEnv->getParent();
//        if (self::$currentEnv === null)
//            throw new PhiIllegalStateException("0a0f0cfa-1ccf-4017-b97c-c064ad0b0015");
//    }

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

function phiAssert($b) {
    if (!$b)
        throw new PhiAssertionError("Congratulations, Vova. We are hosed...");
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

        $argValues = array();
        foreach ($this->args as $arg)
            array_push($argValues, $arg->evaluate());

        return phiNew($ctorValue, $argValues);
    }

}

/**
 * @param PhiFunction $ctorValue
 * @param PhiValue[] $argValues
 * @return PhiObject
 * @throws PhiIllegalStateException
 */
function phiNew($ctorValue, $argValues) {
    $proto = $ctorValue->getProperty('prototype');
    if (!($proto instanceof PhiObject))
        throw new PhiIllegalStateException("aa9fbdd9-c5b0-4215-afc0-c2e9fbfb29a3");
    $inst = new PhiObject(array('proto' => $proto/*, 'constructor' => $ctorValue*/));
    $ctorValue->invoke($inst, $argValues);
    return $inst;
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

    function __toString() {
        if ($this->value)
            $s = 'true';
        else
            $s = 'false';
        return "PhiBoolean($s)";
    }
}

class PhiBinaryOperation extends PhiExpression {
    /**@var string*/ private $debugTag;
    /**@var string*/ private $op;
    /**@var PhiExpression*/ private $lhs;
    /**@var PhiExpression*/ private $rhs;

    /**
     * @param $debugTag
     * @param string $op
     * @param PhiExpression $lhs
     * @param PhiExpression $rhs
     */
    public function __construct($debugTag, $op, PhiExpression $lhs, PhiExpression $rhs) {
        $this->debugTag = $debugTag;
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

    private function testReferenceEquality() {
        $lhsValue = $this->lhs->evaluate();
        $rhsValue = $this->rhs->evaluate();
        if ($lhsValue instanceof PhiString && $rhsValue instanceof PhiString) {
            return $lhsValue->getValue() === $rhsValue->getValue();
        }
        else if ($lhsValue instanceof PhiNumber && $rhsValue instanceof PhiNumber) {
            return $lhsValue->getValue() === $rhsValue->getValue();
        }
        else if ($lhsValue instanceof PhiNull && $rhsValue instanceof PhiNull) {
            return true;
        }
        else if ($lhsValue instanceof PhiUndefined && $rhsValue instanceof PhiUndefined) {
            return true;
        }
        else if ($lhsValue instanceof PhiObject && $rhsValue instanceof PhiNull
                 || $lhsValue instanceof PhiNull && $rhsValue instanceof PhiObject) {
            return false;
        }
        else if ($lhsValue instanceof PhiUndefined || $rhsValue instanceof PhiUndefined) {
            return $lhsValue instanceof PhiUndefined && $rhsValue instanceof PhiUndefined;
        }
        else if ($lhsValue instanceof PhiObject && $rhsValue instanceof Phiobject) {
            return $lhsValue === $rhsValue;
        }
        else {
            $lhsValueClass = get_class($lhsValue);
            $rhsValueClass = get_class($rhsValue);
            throw new PhiIllegalStateException("8614085d-5212-46f6-9516-f256e02788b0    lhsValue is $lhsValueClass; rhsValue is $rhsValueClass");
        }
    }

    /**
     * @return boolean
     * @throws PhiIllegalStateException
     */
    function testEquality() {
        $lhsValue = $this->lhs->evaluate();
        $rhsValue = $this->rhs->evaluate();
        if ($lhsValue instanceof PhiNull || $lhsValue instanceof PhiUndefined
            || $rhsValue instanceof PhiNull || $rhsValue instanceof PhiUndefined)
        {
            return ($lhsValue instanceof PhiNull || $lhsValue instanceof PhiUndefined)
                && ($rhsValue instanceof PhiNull || $rhsValue instanceof PhiUndefined);
        }
        else if ($lhsValue instanceof PhiString && $rhsValue instanceof PhiString) {
            return $lhsValue->getValue() === $rhsValue->getValue();
        }
        else if ($lhsValue instanceof PhiNumber && $rhsValue instanceof PhiNumber) {
            return $lhsValue->getValue() === $rhsValue->getValue();
        }
        else {
            $lhsValueClass = get_class($lhsValue);
            $rhsValueClass = get_class($rhsValue);
            throw new PhiIllegalStateException("3aa9b7b3-2949-4c10-93e0-f9f6752f1cfa    lhsValue is $lhsValueClass; rhsValue is $rhsValueClass");
        }
    }

    /**
     * @return PhiValue
     * @throws PhiIllegalStateException
     */
    public function evaluate() {
        if ($this->op === '===') {
            return new PhiBoolean($this->testReferenceEquality());
        }
        else if ($this->op === '!==') {
            return new PhiBoolean(!$this->testReferenceEquality());
        }
        else if ($this->op === '==') {
            return new PhiBoolean($this->testEquality());
        }
        else if ($this->op === '!=') {
            return new PhiBoolean(!$this->testEquality());
        }
        else if ($this->op === '=') {
            $rhsValue = $this->rhs->evaluate();
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
                $object->setProperty($fieldName, $rhsValue);
                return $rhsValue;
            }
            else if ($this->lhs instanceof PhiBrackets) {
                // Ex: fucking(shit())[ebanoe(govno())] = c
                $objectPhiValue = $this->lhs->getObject()->evaluate();
                if (!($objectPhiValue instanceof PhiObject))
                    throw new PhiIllegalStateException("ef6e4743-1a4e-4de3-8af8-338277b62901");

                $indexPhiValue = $this->lhs->getIndex()->evaluate();
                if (!($indexPhiValue instanceof PhiString) && !($indexPhiValue instanceof PhiNumber))
                    throw new PhiIllegalStateException("f063cb97-2e17-491c-ae25-b50610504d19");

                $fieldName = strval($indexPhiValue->getValue());
                $objectPhiValue->setProperty($fieldName, $rhsValue);
                return $rhsValue;
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
        else if ($this->op === '&&') {
            $lhsPhiValue = $this->lhs->evaluate();
            if ($lhsPhiValue->isFalsy()) {
                return $lhsPhiValue;
            } else {
                return $this->rhs->evaluate();
            }
        }
        else if ($this->op === '+') {
            $lhsPhiValue = $this->lhs->evaluate();
            $rhsPhiValue = $this->rhs->evaluate();
            if (($lhsPhiValue instanceof PhiString) && ($rhsPhiValue instanceof PhiString)) {
                return new PhiString($lhsPhiValue->getValue() . $rhsPhiValue->getValue());
            }
            else if (($lhsPhiValue instanceof PhiNumber) && ($rhsPhiValue instanceof PhiNumber)) {
                return new PhiNumber($lhsPhiValue->getValue() + $rhsPhiValue->getValue());
            }
            else {
                throw new PhiIllegalStateException("d79e06d2-dcc0-4d51-a73a-0585c168e404");
            }
        }
        else if (in_array($this->op, array('<<', '*', '/', '<=', '<', '|', '-', '&'))) {
            $lhsPhiValue = $this->lhs->evaluate();
            $rhsPhiValue = $this->rhs->evaluate();
            if (!($lhsPhiValue instanceof PhiNumber) || !($rhsPhiValue instanceof PhiNumber))
                throw new PhiIllegalStateException("96ff2e32-1aff-4b41-8e91-9e220c5b2bce");
            $lhsValue = $lhsPhiValue->getValue();
            $rhsValue = $rhsPhiValue->getValue();

            $res = null;
            if ($this->op === '<<') {
                $res = $lhsValue << $rhsValue;
            } else if ($this->op === '*') {
                $res = $lhsValue * $rhsValue;
            } else if ($this->op === '/') {
                $res = $lhsValue / $rhsValue;
            } else if ($this->op === '<=') {
                $res = $lhsValue <= $rhsValue;
            } else if ($this->op === '<') {
                $res = $lhsValue < $rhsValue;
            } else if ($this->op === '|') {
                $res = $lhsValue | $rhsValue;
            } else if ($this->op === '-') {
                $res = $lhsValue - $rhsValue;
            } else if ($this->op === '&') {
                $res = $lhsValue & $rhsValue;
            } else {
                throw new PhiIllegalStateException("f41f9846-7e55-46bb-b9d2-829eb4ab3ed3");
            }

            if (is_numeric($res))
                return new PhiNumber($res);
            else if (is_bool($res))
                return new PhiBoolean($res);
            else
                throw new PhiIllegalStateException("3a1af0b3-0d2a-4263-8353-016c3b2e4d9c");
        }
        else if ($this->op === 'instanceof') {
            $inst = $this->lhs->evaluate();
            if (!($inst instanceof PhiObject))
                return new PhiBoolean(false);

            $ctor = $this->rhs->evaluate();
            if (!($ctor instanceof PhiFunction))
                throw new PhiIllegalStateException('709e6ffd-7a89-43f3-ad75-dd6e26b27ce0');

            $prototype = $ctor->getProperty('prototype');
            $proto = $inst->getProperty('__proto__');
            while (!($proto instanceof PhiNull)) {
                if ($proto === $prototype) {
                    if (!($proto instanceof Phiobject))
                        throw new PhiIllegalStateException('fa98c05e-874e-4e38-b05f-eb5a15e14c6a');
                    return new PhiBoolean(true);
                } else {
                    $proto = $proto->getProperty('__proto__');
                }
            }
            return new PhiBoolean(false);
        }
        else {
            throw new PhiIllegalStateException("op = {$this->op}    6bb8ca7a-c00a-4f60-9930-211dba14c031");
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

    function __toString() {
        return "{$this->name}";
    }
}

class PhiBooleanLiteral extends PhiExpression {
    /**@var string*/ private $debugTag;
    /**@var boolean*/ private $value;

    /**
     * @param string $debugTag
     * @param boolean $value
     */
    public function __construct($debugTag, $value) {
        $this->debugTag = $debugTag;
        $this->value = $value;
    }

    /**
     * @return PhiValue
     */
    public function evaluate() {
        return new PhiBoolean($this->value);
    }

    /**
     * @return boolean
     */
    public function getValue() {
        return $this->value;
    }
}

class PhiNumberLiteral extends PhiExpression {
    /**@var string*/ private $debugTag;
    /**@var double*/ private $value;

    /**
     * @param $debugTag
     * @param double $value
     */
    public function __construct($debugTag, $value) {
        $this->debugTag = $debugTag;
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

    function __toString() {
        return "PhiNumber({$this->value})";
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

    function __toString() {
        return "PhiString({$this->value})";
    }
}

/**
 * @param PhiExpression $expr
 */
function phiExpressionStatement($expr) {
    Phi::$phiExpressionStatement_expr = $expr;
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
    $phiValue = $expr->evaluate();
    if (!($phiValue instanceof PhiObject))
        throw new PhiIllegalStateException("d6b5d1bf-c9d9-4aa7-b9f9-420bd0124b1f");

    $messagePhiValue = $phiValue->getProperty('message');
//    if ($messagePhiValue instanceof PhiUndefined) { // TODO:vgrechka @kill
//        $messagePhiValue = $phiValue->getProperty('message_ujvw20$_0');
//    }

    if ($messagePhiValue instanceof PhiString) {
        $exception = new Exception($messagePhiValue->getValue());
    }
    else if ($messagePhiValue instanceof PhiUndefined) {
        $exception = new Exception();
    }
    else {
        throw new PhiIllegalStateException("cbba8949-ba96-43d5-93f1-dd84bd002d67");
    }
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
    if ($phiValue instanceof PhiBoolean) {
        return $phiValue->getValue();
    } else if ($phiValue instanceof PhiUndefined) {
            return false;
    } else {
        throw new PhiIllegalStateException("0c8e6543-b05c-4852-a200-185ac3b36632");
    }
}

/**
 * @param PhiExpression $expr
 * @return PhiValue
 */
function phiEvaluate($expr) {
    return $expr->evaluate();
}

class PhiUnaryOperation extends PhiExpression {
    /**@var string*/ private $debugTag;
    /**@var string*/ private $position;
    /**@var string*/ private $op;
    /**@var PhiExpression*/ private $arg;

    /**
     * @param $debugTag
     * @param string $position
     * @param string $op
     * @param PhiExpression $arg
     */
    public function __construct($debugTag, $position, $op, $arg) {
        $this->debugTag = $debugTag;
        $this->position = $position;
        $this->op = $op;
        $this->arg = $arg;
    }

    /**
     * @return string
     */
    public function getPosition() {
        return $this->position;
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
        if ($this->position == 'prefix') {
            if ($this->op === 'typeof') {
                if ($this->arg instanceof PhiNameRef && !Phi::getCurrentEnv()->hasVar($this->arg->getName())) {
                    return new PhiString('undefined');
                } else {
                    $argPhiValue = $this->arg->evaluate();
                    return new PhiString($argPhiValue->typeof());
                }
            }
            else if ($this->op === '-') {
                $argPhiValue = $this->arg->evaluate();
                if (!($argPhiValue instanceof PhiNumber))
                    throw new PhiIllegalStateException("bc7791cf-9beb-4fef-bff1-f66eb4fcdbed");
                return new PhiNumber(-$argPhiValue->getValue());
            }
            else if ($this->op === 'void') {
                $this->arg->evaluate();
                return new PhiUndefined();
            }
            else if ($this->op === '!') {
                $argPhiValue = $this->arg->evaluate();
                if (!($argPhiValue instanceof PhiBoolean))
                    throw new PhiIllegalStateException("9d2c372a-68a9-43fd-a94d-085e803c72b7");
                return new PhiBoolean(!$argPhiValue->getValue());
            }
            else {
                throw new PhiIllegalStateException("op = {$this->op}    f88f33d3-2868-43d5-82b7-a1b8c20fc1cb");
            }
        }
        else if ($this->position == 'postfix') {
            if ($this->op === '++') {
                if (!($this->arg instanceof PhiNameRef))
                    throw new PhiIllegalStateException('ec4aae29-4f1e-43be-9f4c-6c6b2fc5a590');
                $varName = $this->arg->getName();

                $currentValue = Phi::getCurrentEnv()->getVar($varName);
                if (!($currentValue instanceof PhiNumber))
                    throw new PhiIllegalStateException('071a8fe1-3c18-460b-ab00-69330f2ebdb3');

                $res = new PhiNumber($currentValue->getValue());
                Phi::getCurrentEnv()->setVar($varName, new PhiNumber($currentValue->getValue() + 1));
                return $res;
            }
            else {
                throw new PhiIllegalStateException("ffb361d9-7059-4418-a1ba-4d1127b806a5");
            }
        }
        else {
            throw new PhiIllegalStateException("3d709e94-9f09-49da-a4ce-985074ef38b0");
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
        return $objectPhiValue->getProperty($fieldName);
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

//        if ($this->callee instanceof PhiNameRef && $this->callee->getName() === 'getStringHashCode') {
//            strval('break on me');
//        }

        if ($this->callee instanceof PhiDot) {
            // TODO:vgrechka @duplication 6fe21b9f-6bdb-446e-8186-801891740e1b

            $receiverPhiValue = $this->callee->getQualifier()->evaluate();

            if ($receiverPhiValue instanceof PhiString) {
                $String = phiEvaluate(new PhiNameRef('String'));
                if (!($String instanceof PhiFunction))
                    throw new PhiIllegalStateException('8737db737-d199-4b6c-b146-50aff19f729225979da-9e92-45f4-882a-f667d895f9ad');
                $receiverPhiValue = phiNew($String, array($receiverPhiValue));
            }
            else if ($receiverPhiValue instanceof PhiNumber) {
                $method = $this->callee->getName();
                if ($method === 'toString') {
                    return new PhiString(strval($receiverPhiValue->getValue()));
                }
                else {
                    throw new PhiIllegalStateException("540cfc5c-de23-42c5-99e7-189e9ffe66d8");
                }
            }
            else if ($receiverPhiValue instanceof PhiBoolean) {
                $method = $this->callee->getName();
                if ($method === 'toString') {
                    if ($receiverPhiValue->getValue())
                        return new PhiString('true');
                    else
                        return new PhiString('false');
                }
                else {
                    throw new PhiIllegalStateException("c3fa9fe1-2b3b-4bf5-8080-56e55bfc6290");
                }
            }

            if (!($receiverPhiValue instanceof PhiObject))
                throw new PhiIllegalStateException("4720e481-3172-4ce3-b80c-0b8166126f0e");

            $calleePhiValue = $receiverPhiValue->getProperty($this->callee->getName());
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
            $calleePhiValue = $receiverPhiValue->getProperty($fieldName);
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

        if (!($calleePhiValue instanceof PhiFunction)) {
            // XXX KJS bug with adding `String` to `String?`
            if ($calleePhiValue instanceof PhiUndefined
                && $this->callee instanceof PhiNameRef
                && $this->callee->getName() === 'plus'
                && count($this->args) === 2)
            {
                $lhsValue = phiEvaluate($this->args[0]);
                if ($lhsValue instanceof PhiNull || $lhsValue instanceof PhiUndefined)
                    $lhsValue = new PhiString("null");
                $rhsValue = phiEvaluate($this->args[1]);
                if ($rhsValue instanceof PhiNull || $rhsValue instanceof PhiUndefined)
                    $rhsValue = new PhiString("null");
                if (!($lhsValue instanceof PhiString && $rhsValue instanceof PhiString))
                    throw new PhiIllegalStateException("b92331f5-46cf-4dd8-857c-a504b8096119");

                return new PhiString($lhsValue->getValue() . $rhsValue->getValue());
            }
            throw new PhiIllegalStateException("f30e6af1-a2b9-4345-8922-51caa7ba7bcb");
        }

        $argPhiValues = array();
        foreach ($this->args as $arg)
            array_push($argPhiValues, $arg->evaluate());

        $res = $calleePhiValue->invoke($receiverPhiValue, $argPhiValues);

// @debug
//        if (Phi::$debugContext === 'phiQuickTest_functionDotCall-1') {
//            count('break on me');
//        }

        return $res;
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
    /**@var string*/ private $debugTag;
    /**@var array*/ private $keyValuePairs;

    /**
     * @param $debugTag
     * @param array $keyValuePairs
     */
    public function __construct($debugTag, $keyValuePairs) {
        $this->debugTag = $debugTag;
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

            $object->setProperty($key, $valueExpression->evaluate());
        }
        return $object;
    }
}

/**
 * @param string $debugTag
 * @param array $nameValuePairs
 * @throws PhiIllegalStateException
 */
function phiVars($debugTag, $nameValuePairs) {
    foreach ($nameValuePairs as $pair) {
        /**@var string $varName*/
        $varName = $pair[0];

        /**@var PhiExpression $expr*/
        $expr = $pair[1];
        if (!($expr instanceof PhiExpression)) {
            throw new PhiIllegalStateException("8a8687aa-a829-402a-b6f8-9ab6c7676f6b");
        }

        Phi::getCurrentEnv()->setVar($varName, $expr->evaluate());
    }
}

class PhiArray extends PhiObject {
    /**@var PhiValue[]*/ private $items = array();

    function __construct() {
        parent::__construct();

        { // me.__proto__ = Array.prototype
            $e = new PhiNameRef('Array');
            $e = new PhiDot($e, 'prototype');
            $this->fields['__proto__'] = $e->evaluate();
        }

//        { // me.constructor = Array
//            $e = new PhiNameRef('Array');
//            $this->fields['constructor'] = $e->evaluate();
//        }
    }

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

function phiEvaluateAndAssert(PhiExpression $expr) {
    $phiValue = $expr->evaluate();
    phiAssert($phiValue instanceof PhiBoolean && $phiValue->getValue());
}

function phiEvaluateAndAssertToStringEquals($expectedPhiValue, PhiExpression $expr) {
    $exps = strval($expectedPhiValue);
    $actualPhiValue = $expr->evaluate();
    if ($actualPhiValue === null)
        $acts = '<<null>>';
    else
        $acts = strval($actualPhiValue);
    if ($exps !== $acts)
        throw new PhiAssertionError("\nExpected: $exps\nActual: $acts\n");
}

function phiBreakDebugger($shit) {
    strval('break on me');
}


// ==================================== ENTRY ======================================


if (defined('PHI_RUN_QUICK_TESTS')) {
    function phiQuickTest_1() {
        Phi::initEnv();
        phiAssertException("'We are hosed, man...'", function() {
            $expr = new PhiNew(new PhiNameRef('Error'),
                array(new PhiStringLiteral("We are hosed, man...")));
            phiThrow($expr);
        });
        phiPrintln(__FUNCTION__ . ': PASSED');
    }
    phiQuickTest_1();

    function phiQuickTest_2() {
        Phi::initEnv();
        phiAssertException("'Fuck you'", function() {
            if (phiEvaluateToBoolean(new PhiBinaryOperation('test-4ba5689c-cca4-411e-89be-ab264c263673', '===', new PhiUnaryOperation('@@', 'prefix', 'typeof', new PhiNameRef('kotlin')), new PhiStringLiteral('undefined')))) {
                phiThrow(new PhiNew(new PhiNameRef('Error'), array(new PhiStringLiteral("Fuck you"))));
            }
        });
        phiPrintln(__FUNCTION__ . ': PASSED');
    }
    phiQuickTest_2();

    function phiQuickTest_3() {
        Phi::initEnv();
        Phi::initStdlib();
        if (phiEvaluateToBoolean(new PhiBinaryOperation('test-da903864-b84a-40aa-9722-644d9b23d967', '===', new PhiUnaryOperation('@@', 'prefix', 'typeof', new PhiNameRef('kotlin')), new PhiStringLiteral('undefined')))) {
            phiThrow(new PhiNew(new PhiNameRef('Error'), array(new PhiStringLiteral("Fuck you"))));
        }
        phiPrintln(__FUNCTION__ . ': PASSED');
    }
// phiQuickTest_3();

    function phiQuickTest_getOwnPropertyDescriptor() {
        Phi::initEnv();
        Phi::initStdlib();

        // var a = {}, d = undefined
        phiVars('@@', array(
            array('a', new PhiObjectLiteral('@@', array())),
            array('d', new PhiNameRef('undefined'))));

        // a.x === undefined
        phiEvaluateAndAssert(new PhiBinaryOperation('@@',
            '===',
            new PhiDot(new PhiNameRef('a'), 'x'),
            new PhiNameRef('undefined')));

        /**
         * Helper: d = Object.getOwnPropertyDescriptor(a, '$prop')
         * @param string $prop
         */
        $getOwnPropertyDescriptor = function($prop) {
            phiExpressionStatement(new PhiBinaryOperation('@@',
                '=',
                new PhiNameRef('d'),
                new PhiInvocation(
                    new PhiDot(new PhiNameRef('Object'), 'getOwnPropertyDescriptor'),
                    array(new PhiNameRef('a'), new PhiStringLiteral($prop)))));
        };

        // d = Object.getOwnPropertyDescriptor(a, 'x')
        $getOwnPropertyDescriptor('x');
        // d === undefined
        phiEvaluateAndAssert(new PhiBinaryOperation('@@',
            '===',
            new PhiNameRef('d'),
            new PhiNameRef('undefined')));

        // a.x = 123
        phiExpressionStatement(new PhiBinaryOperation('@@',
            '=',
            new PhiDot(new PhiNameRef('a'), 'x'),
            new PhiNumberLiteral('@@', 123)));

        // a.x === 123
        phiEvaluateAndAssert(new PhiBinaryOperation('@@',
            '===',
            new PhiDot(new PhiNameRef('a'), 'x'),
            new PhiNumberLiteral('@@', 123)));

        // d = Object.getOwnPropertyDescriptor(a, 'x')
        $getOwnPropertyDescriptor('x');

        // d.value === 123
        phiEvaluateAndAssert(new PhiBinaryOperation('@@',
            '===',
            new PhiDot(new PhiNameRef('d'), 'value'),
            new PhiNumberLiteral('@@', 123)));

        // Object.defineProperty(a, 'y', {get: function() {return 333}})
        phiExpressionStatement(new PhiInvocation(
            new PhiDot(new PhiNameRef('Object'), 'defineProperty'),
            array(
                new PhiNameRef('a'), new PhiStringLiteral('y'),
                new PhiObjectLiteral('@@', array(
                    array(
                        new PhiNameRef('get'),
                        new PhiFunctionExpression(
                            'getter', array(), function() {
                            return phiEvaluate(new PhiNumberLiteral('@@', 333));
                        }))
                )))
        ));

        // a.y === 333
        phiEvaluateAndAssert(new PhiBinaryOperation('@@',
            '===',
            new PhiDot(new PhiNameRef('a'), 'y'),
            new PhiNumberLiteral('@@', 333)));

        // typeof Object.getOwnPropertyDescriptor(a, 'y').get === 'function'
        phiEvaluateAndAssert(new PhiBinaryOperation('@@',
            '===',
            new PhiUnaryOperation('@@', 'prefix', 'typeof',
                new PhiDot(new PhiInvocation(
                    new PhiDot(new PhiNameRef('Object'), 'getOwnPropertyDescriptor'),
                    array(new PhiNameRef('a'), new PhiStringLiteral('y'))), 'get')),
            new PhiStringLiteral('function')));

        phiPrintln(__FUNCTION__ . ': PASSED');
    }
    phiQuickTest_getOwnPropertyDescriptor();

    function phiQuickTest_functionDotCall() {
        Phi::initEnv();
        Phi::initStdlib();

        // function fuck(a, b) {return a + b}
        phiExpressionStatement(new PhiFunctionExpression('fuck', array('a', 'b'), function() {
            return phiEvaluate(new PhiBinaryOperation('@@', '+', new PhiNameRef('a'), new PhiNameRef('b')));
        }));

        // 5  EQ  fuck(2, 3)
        phiEvaluateAndAssertToStringEquals(
            new PhiNumber(5),
            new PhiInvocation(new PhiNameRef('fuck'), array(
                new PhiNumberLiteral('@@', 2),
                new PhiNumberLiteral('@@', 3))));

        // 5  EQ  fuck.call(null, 2, 3)
        Phi::$debugContext = 'phiQuickTest_functionDotCall-1';
        phiEvaluateAndAssertToStringEquals(
            new PhiNumber(5),
            new PhiInvocation(new PhiDot(new PhiNameRef('fuck'), 'call'), array(
                new PhiNullLiteral('@@'),
                new PhiNumberLiteral('@@', 2),
                new PhiNumberLiteral('@@', 3))));

        phiPrintln(__FUNCTION__ . ': PASSED');
    }
    phiQuickTest_functionDotCall();

    function phiQuickTest_varsDontOverrideParentEnv() {
        Phi::initEnv();
        Phi::initStdlib();

        // function f() {
        //     var x = 10;
        //     function g() {
        //         var x = 20;
        //     }
        //     g();
        //     return x;
        // }
        phiExpressionStatement(new PhiFunctionExpression('f', array(), function() {
            phiVars('@@', array(array('x', new PhiNumberLiteral('@@', 10))));
            phiExpressionStatement(new PhiFunctionExpression('g', array(), function() {
                phiVars('@@', array(array('x', new PhiNumberLiteral('@@', 20))));
            }));
            phiExpressionStatement(new PhiInvocation(new PhiNameRef('g'), array()));
            return phiEvaluate(new PhiNameRef('x'));
        }));

        // 10  EQ  f()
        phiEvaluateAndAssertToStringEquals(
            new PhiNumber(10),
            new PhiInvocation(new PhiNameRef('f'), array())
        );
        phiPrintln(__FUNCTION__ . ': PASSED');
    }
    phiQuickTest_varsDontOverrideParentEnv();

    function phiQuickTest_instanceof() {
        Phi::initEnv();
        Phi::initStdlib();

        // function F() {}
        phiExpressionStatement(new PhiFunctionExpression('F', array(), function() {
        }));

        // var f = new F()
        phiVars('@@', array(array('f', new PhiNew(new PhiNameRef('F'), array()))));

        // true  EQ  f instanceof F
        phiEvaluateAndAssertToStringEquals(
            new PhiBoolean(true),
            new PhiBinaryOperation('@@', 'instanceof',
                new PhiNameRef('f'),
                new PhiNameRef('F')));

        // true  EQ  f instanceof Object
        phiEvaluateAndAssertToStringEquals(
            new PhiBoolean(true),
            new PhiBinaryOperation('@@', 'instanceof',
                new PhiNameRef('f'),
                new PhiNameRef('Object')));


        phiPrintln(__FUNCTION__ . ': PASSED');
    }
    phiQuickTest_instanceof();

    function phiQuickTest_string() {
        $testShit = function($setUp) {
            Phi::initEnv(); Phi::initStdlib();
            $setUp();

            // 7  EQ  s.length
            phiEvaluateAndAssertToStringEquals(new Phinumber(7), new PhiDot(new PhiNameRef('s'), 'length'));

            $checkCode = function($expected, $pos) {
                phiEvaluateAndAssertToStringEquals(
                    new PhiNumber($expected),
                    new PhiInvocation(new PhiDot(new PhiNameRef('s'), 'charCodeAt'), array(new PhiNumberLiteral('@@',
                        $pos))));
            };

            // 1087   EQ  s.charCodeAt(0)
            $checkCode(1087, 0);
            // 1080   EQ  s.charCodeAt(1)
            $checkCode(1080, 1);
            // 1079   EQ  s.charCodeAt(2)
            $checkCode(1079, 2);
            // 1076   EQ  s.charCodeAt(3)
            $checkCode(1076, 3);
            // 1072   EQ  s.charCodeAt(4)
            $checkCode(1072, 4);
            // 32     EQ  s.charCodeAt(5)
            $checkCode(32, 5);
            // 20189  EQ  s.charCodeAt(6)
            $checkCode(20189, 6);
            // NaN    EQ  s.charCodeAt(7)
            $checkCode(NAN, 7);
        };

        // var s = 'пизда 仝'
        $testShit(function() {
            phiVars('@@', array(array('s', new PhiStringLiteral("пизда 仝"))));
        });

        // var s = String('пизда 仝')
        $testShit(function() {
            phiVars('@@', array(array('s', new PhiInvocation(new PhiNameRef('String'), array(new PhiStringLiteral("пизда 仝"))))));
        });

        // var s = new String('пизда 仝')
        $testShit(function() {
            phiVars('@@', array(array('s', new PhiNew(new PhiNameRef('String'), array(new PhiStringLiteral("пизда 仝"))))));
        });

        phiPrintln(__FUNCTION__ . ': PASSED');
    }
    phiQuickTest_string();

    function phiQuickTest_constructor() {
        Phi::initEnv(); Phi::initStdlib();

        // var x = {}
        phiVars('@@', array(array('x', new PhiObjectLiteral('@@', array()))));

        // true  EQ  x.constructor === Object
        phiEvaluateAndAssertToStringEquals(
            new PhiBoolean(true),
            new PhiBinaryOperation('@@', '===',
                new PhiDot(new PhiNameRef('x'), 'constructor'),
                new PhiNameRef('Object')));

        // true  EQ  Object.prototype.constructor === Object
        phiEvaluateAndAssertToStringEquals(
            new PhiBoolean(true),
            new PhiBinaryOperation('@@', '===',
                new PhiDot(new PhiDot(new PhiNameRef('Object'), 'prototype'), 'constructor'),
                new PhiNameRef('Object')));

        // var arr = []
        phiVars('@@', array(array('arr', new PhiArrayLiteral(array()))));

        // true  EQ  arr.constructor === Array
        phiEvaluateAndAssertToStringEquals(
            new PhiBoolean(true),
            new PhiBinaryOperation('@@', '===',
                new PhiDot(new PhiNameRef('arr'), 'constructor'),
                new PhiNameRef('Array')));

        // true  EQ  Array.prototype.constructor === Array
        phiEvaluateAndAssertToStringEquals(
            new PhiBoolean(true),
            new PhiBinaryOperation('@@', '===',
                new PhiDot(new PhiDot(new PhiNameRef('Array'), 'prototype'), 'constructor'),
                new PhiNameRef('Array')));

        // function C() {}
        phiExpressionStatement(new PhiFunctionExpression('C', array(), function() {
        }));

        // var c = new C()
        phiVars('@@', array(array('c', new PhiNew(new PhiNameRef('C'), array()))));

        // true  EQ  c.constructor === C
        phiEvaluateAndAssertToStringEquals(
            new PhiBoolean(true),
            new PhiBinaryOperation('@@', '===',
                new PhiDot(new PhiNameRef('c'), 'constructor'),
                new PhiNameRef('C')));

        // true  EQ  Object.getPrototypeOf(c) === C.prototype
        phiEvaluateAndAssertToStringEquals(
            new PhiBoolean(true),
            new PhiBinaryOperation('@@', '===',
                new PhiInvocation(new PhiDot(new PhiNameRef('Object'), 'getPrototypeOf'), array(new PhiNameRef('c'))),
                new PhiDot(new PhiNameRef('C'), 'prototype')));

        // true  EQ  Object.getPrototypeOf(c).constructor === C
        phiEvaluateAndAssertToStringEquals(
            new PhiBoolean(true),
            new PhiBinaryOperation('@@', '===',
                new PhiDot(
                    new PhiInvocation(
                        new PhiDot(new PhiNameRef('Object'), 'getPrototypeOf'),
                        array(new PhiNameRef('c'))),
                    'constructor'),
                new PhiNameRef('C')));

        phiPrintln(__FUNCTION__ . ': PASSED');
    }
    phiQuickTest_constructor();
}

Phi::initEnv();
Phi::initStdlib();
require_once 'phizdets-stdlib.php';

if (defined('PHI_RUN_QUICK_STDLIB_TESTS')) {
    function phiQuickTest_getStringHashCode() {
        $getHashCodeExpr = function($expr) {
            return new PhiInvocation(new PhiDot(new PhiNameRef('kotlin'), 'hashCode'), array($expr));
        };

        phiEvaluateAndAssertToStringEquals(
            new Phinumber(3065272),
            $getHashCodeExpr(new PhiStringLiteral('cunt'))
        );

        // TODO:vgrechka In KJS it's 233157303, because `| 0` gives different results sometimes
        phiEvaluateAndAssertToStringEquals(
            new Phinumber(996665569975),
            $getHashCodeExpr(new PhiStringLiteral('пизда 仝'))
        );
    }
    phiQuickTest_getStringHashCode();
}























