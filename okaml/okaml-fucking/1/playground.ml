open Core.Std

let println s = print_string (s ^ "\n")

module Pizda = struct
    type t =
    | Fuck of Fuck.t
    | Shit of Shit.t
end


let () =
    println "pizda big"

