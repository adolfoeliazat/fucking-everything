open Core.Std

let first_string_arg x =
    begin match x with
    | Phi.Expression.New x ->
        begin match x.args with
        | x :: _ ->
            begin match x with
            | Phi.Expression.String_literal x -> x.value
            | _ -> assert false
            end
        | _ -> assert false
        end
    | _ -> assert false
    end

let quickTest_1 () =
    Phi.init_env ();
    Phi.assert_exception "We are hosed, man..." (fun () ->
        let expr =
            Phi.Expression.New {Phi.New.
                ctor = Phi.Expression.Name_ref {Phi.Name_ref.name = "pizda"};
                args = [
                    Phi.Expression.String_literal {Phi.String_literal.
                        value = "We are hosed, man..."}
                ]} in
        Phi.println (first_string_arg expr)
        (*Phi.throw expr*)
    );

    Phi.println "phiQuickTest_1: PASSED"

let runQuickTests () =
    quickTest_1 ()

let () =
    Phi.sayHello ();
    runQuickTests ()




