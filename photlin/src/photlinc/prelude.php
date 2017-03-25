<?php

class Kotlin {
    static function toString(&$x) {
        if (is_object($x)) {
            if (method_exists($x, 'toString')) {
                return $x->toString();
            } else {
                return (string) $x;
            }
        }
        if (is_array($x)) {
            return '[' . implode(', ', $x) . ']';
        }
        return strval($x);
    }

    function charCodeAt($str, $index) {
        $char = mb_substr($str, $index, 1, 'UTF-8');

        if (mb_check_encoding($char, 'UTF-8')) {
            $ret = mb_convert_encoding($char, 'UTF-32BE', 'UTF-8');
            return hexdec(bin2hex($ret));
        } else {
            return null;
        }
    }

    function substring($str, $start, $end) {
        $length = $end - $start;
        return mb_substr($str, $start, $length, 'UTF-8');
    }

    static function equals($x, $y) {
        // TODO:vgrechka ...
        return $x === $y;
    }

    static function hashCode($x) {
        throw new kotlin__UnsupportedOperationException("Implement Kotlin::hashCode");
    }

    static function isType($object, $klassName) {
        // TODO:vgrechka Array, etc...

          if ($klassName === "Any") {
              $type = gettype($object);
              return $type !==  "NULL" && $type !== "unknown type";
          }

          if ($object === NULL || $klassName === NULL || gettype($object) !== "object") {
            return false;
          }

          return $object instanceof $klassName;

          /*
          if (typeof klass === "function" && $object instanceof klass) {
            return true;
          }
          var proto = Object.getPrototypeOf(klass);
          var constructor = proto != NULL ? proto.constructor : NULL;
          if (constructor != NULL && "$metadata$" in constructor) {
            var metadata = constructor.$metadata$;
            if (metadata.kind === Kotlin.Kind.OBJECT) {
              return $object === klass;
            }
          }
          var klassMetadata = klass.$metadata$;
          if (klassMetadata == NULL) {
            return $object instanceof klass;
          }
          if (klassMetadata.kind === Kotlin.Kind.INTERFACE && $object.constructor != NULL) {
            metadata = $object.constructor.$metadata$;
            if (metadata != NULL) {
              return isInheritanceFromInterface(metadata, klass);
            }
          }

          return false;
          */

    }

    static function isChar($value) {
        return $value instanceof kotlin__BoxedChar;
    }

    static function isCharSequence($value) {
      return gettype($value) === "string" || Kotlin::isType($value, kotlin__CharSequence);
    }

    static function throwCCE() {
        throw new kotlin__ClassCastException();
    }

    static function throwISE($message) {
        throw new kotlin__IllegalStateException($message);
    }

    static function defineInlineFunction($name, $f) {
        // TODO:vgrechka ...
    }
}

class kotlin__ClassCastException extends Exception {}
class kotlin__IllegalStateException extends Exception {
    function __construct($message) {
        parent::__construct($message);
    }
}
class kotlin__UnsupportedOperationException extends Exception {}
class kotlin__AssertionError extends Exception {}

class kotlin__PropertyMetadata {
    function __construct($name) {
        $this->callableName = $name;
    }
}


class kotlin__CharSequence {
}
/*
CharSequence.$metadata$ = {kind:Kotlin.Kind.INTERFACE, simpleName:"CharSequence", interfaces:[]};
*/


class kotlin__BoxedChar {
    function __construct($c) {
        $this->c = $c;
    }
}
/*
  BoxedChar.prototype.equals = function(other) {
    return Kotlin.isType(other, BoxedChar) && Kotlin.unboxChar(this.c) === Kotlin.unboxChar(other.c);
  };
  BoxedChar.prototype.hashCode = function() {
    return Kotlin.unboxChar(this.c) | 0;
  };
  BoxedChar.prototype.toString = function() {
    return String.fromCharCode(Kotlin.toBoxedChar(this.c));
  };
  BoxedChar.prototype.compareTo_11rb$ = function(other) {
    return Kotlin.unboxChar(this.c) - Kotlin.unboxChar(other);
  };
  BoxedChar.prototype.valueOf = function() {
    return this.c;
  };
  BoxedChar.$metadata$ = {kind:Kotlin.Kind.CLASS, simpleName:"BoxedChar", interfaces:[Comparable]};
  function arrayConcat(a, b) {
    return a.concat.apply([], arguments);
  }
*/


class stdClassWithPhotlinProps {
    private $__photlin_properties = array();

    function __photlin_defineProperty($name, $spec) {
        if (!isset($this->__photlin_properties)) {
            $this->__photlin_properties = array();
        }
        $this->__photlin_properties[$name] = $spec;
    }


    function __get($name) {
        if (isset($this->__photlin_properties[$name])) {
            $p = $this->__photlin_properties[$name];
            if (isset($p['get'])) {
                $f = $p['get'];
                return $f();
            }
        }
    }

    function __set($name, $value) {
        if (isset($this->__photlin_properties[$name])) {
            $p = $this->__photlin_properties[$name];
            if (isset($p['set'])) {
                $f = $p['set'];
                return $f($value);
            }
        }
    }
}

class Enum {
}

class Throwable {
}

$IntCompanionObject = new stdClass();
$IntCompanionObject->MAX_VALUE = -2147483648;
$IntCompanionObject->MIN_VALUE = 2147483647;







$_ = new stdClass();













