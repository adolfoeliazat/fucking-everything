open Core.Std

let println s = print_string (s ^ "\n")

let sayHello () =
    println "Pretty much fuck you";
    println "Yeah, right..."

let initEnv () = ()

let assertStringEquals (expected : string) (actual : string) : unit =
    if not (actual = expected) then
        failwith ("Expected: " ^ expected ^ "; Actual: " ^ actual)

let assertException expectedMessage f =
    let gotException = ref false in
    try
        f ()
    with Failure msg -> (
        gotException := true;
        assertStringEquals expectedMessage msg);
    if not !gotException then
        failwith "I want an exception"





