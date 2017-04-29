open Core.Std

module Expression = struct
    type t =
    | New of New.t
    | Number_literal of Number_literal.t
    | String_literal of String_literal.t
    | Name_ref of Name_ref.t
end

module rec Value : sig
    type t =
    | Object of Object.t
    | String of String.t

    (*module Object = struct
        type t = unit
    end

    module String = struct
        type t = unit
    end*)
end = Value

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

let phi_throw expr =
    let phi_value = Expression.evaluate expr in

    match phi_value with
    | Value.Object -> (
        let message_phi_value = Value.Object.get_property phi_value "message" in

        let message = match message_phi_value with
        | Value.String -> message_phi_value.value
        | Value.Undefined -> ""
        | _ -> failwith "cbba8949-ba96-43d5-93f1-dd84bd002d67" in

        raise (Bloody_exception {message; phi_value})
    )
    | _ -> failwith "d6b5d1bf-c9d9-4aa7-b9f9-420bd0124b1f"

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




