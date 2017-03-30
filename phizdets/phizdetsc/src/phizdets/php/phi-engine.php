<?php

/*
$a = array('foo' => null);
$b =& $a['foo'];
phi__dumpNameValueSequence('b', $b);
exit();
*/

phi__pushEnv();

phi__functionDeclaration('Error', array('message'), function () {
    phi__binop('=', phi__dot(phi__this(), 'message'), phi__name('message'));
});

function phi__dot($lhs, $name) {
    return array('kind' => 'dot', 'lhs' => $lhs, 'name' => $name);
}

function phi__this() {
    return array('kind' => 'this');
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

function phi__assertEquals($expectedExported, $actual) {
    $actualExported = var_export($actual, true);
    if ($actualExported !== $expectedExported)
        throw new PhiAssertionError("Expected: $expectedExported; Actual: $actualExported");
}

function phi__printBoolean($b) {
    if ($b) echo 'true';
    else echo 'false';
}

function quickTest_1() {
    try {
        throw phi__new(phi__name('Error'), array(phi__stringLiteral("We are hosed, man...")));
    } catch (Exception $e) {
        if ($e instanceof PhiIllegalStateException)
            throw $e;
        phi__assertEquals("'We are hosed, man...'", $e->getMessage());
    }
}
quickTest_1();


function &phi__pushEnv() {
    $newEnv = array('varCells' => array());
    if (isset($GLOBALS['phi__currentEnv'])) {
        $newEnv['parent'] =& $GLOBALS['phi__currentEnv'];
    } else {
        $newEnv['parent'] = null;
    }
    $GLOBALS['phi__currentEnv'] =& $newEnv;
    return $newEnv;
}

function phi__popEnv() {
    check(isset($GLOBALS['phi__currentEnv']), "65e61a69-46a5-4df0-abe6-45d39898bca0");
    check(isset($GLOBALS['phi__currentEnv']['parent']), "47a8554a-1bf1-42ed-a627-dc98c3fbe65d");
    $GLOBALS['phi__currentEnv'] =& $GLOBALS['phi__currentEnv']['parent'];
}


/*
'kotlin' => array(
                'typeof' => 'object',
                'fields' => array()
            )
            */




function phi__functionDeclaration($name, $argNames, $f) {
    $cell = phi__makeCell();
    $GLOBALS['phi__currentEnv']['varCells'][$name] =& $cell;
    $cell['value'] = phi__functionExpression($name, $argNames, $f);
    // phi__dump('env', $GLOBALS['phi__currentEnv']); exit();
}

function phi__functionExpression($name, $argNames, $f) {
    return array(
        'debugDescription' => "Function `$name`",
        'debugBacktrace' => phi__backtraceString(),
        'typeof' => 'function',
        'name' => $name,
        'argNames' => $argNames,
        'function' => $f
    );
}


function phi__imf($msg) {
    throw new PhiIllegalStateException("Implement me, please, fuck you. $msg");
}

function phi__wtf($msg) {
    throw new PhiIllegalStateException("WTF: $msg");
}


function phi__check($cond, $msg) {
    if (!$cond) {
        throw new PhiIllegalStateException($msg);
    }
}

function phi__dumpNameValueSequence() {
    phi__println(phi__dumpNameValueSequenceToString(func_get_args()));
}

function phi__dumpNameValueSequenceToString($nameValueSequence, $opts = array()) {
    $count = count($nameValueSequence);
    phi__check($count > 0 && $count % 2 === 0, "40c23506-2a94-468f-9f07-e32e1b318068    count = $count");

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

function phi__makeCallWithReceiver($receiver, $fPE, $args) {
    // phi__dumpNameValueSequence('receiver', $receiver, 'fPE', $fPE, 'args', $args, 'env', $GLOBALS['phi__currentEnv']); exit();

    $fCell = phi__findCell($fPE);
    // phi__dumpNameValueSequence('fCell', $fCell); exit();
    $f =& $fCell['value'];
    $argNames = $f['argNames'];
    phi__check(count($argNames) === count($args), "c099b7ef-ab67-4155-8b54-388357b3781a");
    $env =& phi__pushEnv();
    for ($i = 0; $i < count($argNames); ++$i) {
        $cell =& phi__makeCell();
        $cell['value'] =& $args[$i];
        $env['varCells'][$argNames[$i]] =& $cell;
    }
    $env['thisCell'] =& phi__makeCell();
    $res = call_user_func($f['function']);
    phi__popEnv();
    return $res;
}


function phi__backtraceString() {
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

function& phi__new($ctor, $args) {
    $inst = array(
        'debugBacktrace' => phi__backtraceString(),
        'typeof' => 'object',
        'fields' => array()
    );
    phi__makeCallWithReceiver($inst, $ctor, $args);
    return $inst;
}

function phi__stringLiteral($s) {
    return array('typeof' => 'string', 'value' => $s);
}

function phi__println($x) {
    echo $x . "\n";
}

function phi__name($name) {
    return array('kind' => 'name', 'name' => $name);
}

function phi__typeof($x) {
    return phi__stringLiteral($x['typeof']);
}

function phi__binop($op, $lhs, $rhs) {
    if ($op === '===') {
        if ($lhs['typeof'] === 'string' && $rhs['typeof'] === 'string') {
            return array(
                'typeof' => 'boolean',
                'value' => $lhs['value'] === $rhs['value']
            );
        } else {
            throw new Exception("8614085d-5212-46f6-9516-f256e02788b0    lhs->typeOf = {$lhs->typeOf}; rhs->typeOf = {$rhs->typeOf}");
        }
    } else if ($op === '=') {
        // Ex: a = b             phi__name('a') = phi__name('b')
        // Ex: a.b = c()           phi__dot(phi__name('a'), 'b') = phi__call(...)
        // Ex: a['b'] = c        phi__brackets(phi__name('a'), phi__name('b'))
        $cell = phi__findCell($lhs);
        $cell['value'] = phi__evaluate($rhs);
    } else {
        throw new Exception("6bb8ca7a-c00a-4f60-9930-211dba14c031    op = $op");
    }
}

function phi__dumpFunctionEnterAndExit() {
    $args = func_get_args();
    $functionName = $args[0];
    $rest = array_slice($args, 1);

    phi__println("Entering $functionName");
    echo call_user_func('phi__dumpNameValueSequenceToString', $rest, array('indent' => 4));
    exit();
}

function phi__dump($x) {
    echo var_export($x, true);
}

function phi__dumpAndExit($x) {
    phi__dump($x);
    exit();
}

function phi__dumpNameValueSequenceAndExit($x) {
    phi__dumpNameValueSequence($x);
    exit();
}

function phi__evaluate($pe) {
    // phi__dumpFunctionEnterAndExit('phi__evaluate', 'pe', $pe, 'env', $GLOBALS['phi__currentEnv']);
}

function phi__makeCell() {
    return array('value' => null);
}

function &phi__findCell($expr) {
    if ($expr['kind'] === 'name') {
        $name = $expr['name'];
        $env =& $GLOBALS['phi__currentEnv'];
        while (!is_null($env)) {
            $cells =& $env['varCells'];
            if (isset($cells[$name]))
                return $cells[$name];
            $env =& $env['parent'];
        }
        phi__wtf("e49e2fc1-8bbb-459f-831f-3eafee4edc69");
    }
    else if ($expr['kind'] === 'this') {
        $env =& $GLOBALS['phi__currentEnv'];
        $thisCell =& $env['thisCell'];
        phi__check(isset($thisCell), "87f8bf17-63a3-4368-a471-85cbfce7b6a7");
        return $thisCell;
    }
    else if ($expr['kind'] === 'dot') {
        $objCell = phi__findCell($expr['lhs']);
        $obj =& $objCell['value'];
        $fieldName = $expr['name'];
        $fields =& $obj['fields'];
        if (isset($fields[$fieldName])) {
            return $fields[$fieldName];
        } else {
            $fields[$fieldName] =& phi__makeCell();
            return $fields[$fieldName];
        }
    }
    else if ($expr['kind'] === 'brackets') {
        phi__imf("de38b397-5ee5-46bc-ae0d-66067e099427");
    }
    else {
        phi__wtf("ed923de8-f21a-44cd-b991-d8e3c03fa762    kind = {$expr['kind']}");
    }
}




















