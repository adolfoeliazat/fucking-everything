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

let quick_test_1 () =
    Phi.init_env ();
    Phi.assert_exception "We are hosed, man..." (fun () ->
        let expr =
            Phi.Expression.New {
                ctor = Phi.Expression.Name_ref {name = "pizda"};
                args = [Phi.Expression.String_literal {value = "We are hosed, man..."}]} in
        Phi.throw expr;
    );

    Phi.println "phiQuickTest_1: PASSED"

let run_quick_tests () =
    quick_test_1 ()

let () =
    Phi.say_hello ();
    run_quick_tests ()




