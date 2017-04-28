open Core.Std

module rec Expression : sig
    type t =
    | New of New.t
    | Number_literal of Number_literal.t
    | String_literal of String_literal.t
    | Name_ref of Name_ref.t
end = Expression

and String_literal : sig
    type t = {
        value : string
    }
end = String_literal

and Name_ref : sig
    type t = {
        name : string
    }
end = Name_ref

and New : sig
    type t = {
        ctor : Expression.t;
        args : Expression.t list;
    }
end = New

and Number_literal : sig
    type t = {
        value : int;
    }
end = Number_literal


let println s = print_string (s ^ "\n")

let sayHello () =
    println "Pretty much fuck you";
    println "Yeah, right..."

let init_env () = ()

let assert_string_equals (expected : string) (actual : string) : unit =
    if not (actual = expected) then
        failwith ("Expected: " ^ expected ^ "; Actual: " ^ actual)

let assert_exception expected_message f =
    let got_exception = ref false in
    try
        f ()
    with Failure msg -> (
        got_exception := true;
        assert_string_equals expected_message msg);
    if not !got_exception then
        failwith "I want an exception"




