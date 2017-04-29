open Core.Std

let println s = print_string (s ^ "\n")

module Pizda = struct
    module Fuck = struct
        type t = {value : string}
    end

    module Shit = struct
        type t = {value : string(*; pizda: Pizda.t*)}
    end

    type t =
    | Fuck of Fuck.t
    | Shit of Shit.t

    let evaluate p =
        match p with
        | Fuck p -> "some fuck" ^ p.value
        | Shit p -> "some shit " ^ p.value
end


let () =
    println "I am the fuck2";
    Pizda.Shit {value = "big"} |> Pizda.evaluate |> println
















