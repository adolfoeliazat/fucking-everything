open Core.Std

let println s = print_string (s ^ "\n")

module rec String_literal : sig
    type t = {value : string}
end = String_literal

and Number_literal : sig
    type t = {value : float}
end = Number_literal

and New_expression : sig
    type t = {ctor : Expression.t}
end = New_expression

and Expression : sig
    type t =
        | String_literal of String_literal.t
        | Number_literal of Number_literal.t
end = Expression


let f x =
    match x with
        | Expression.String_literal x -> printf "string literal: %s\n" x.value
        | Expression.Number_literal x -> printf "number literal: %f\n" x.value
        | _ -> assert false

let () =
    println "I am the fuck2";
    f (Expression.String_literal {value = "scuko"});
    f (Expression.Number_literal {value = 10.5});

    (*Expression.Shit {value = "big"} |> Expression.evaluate |> println*)
















