open Core.Std

let quickTest_1 () =
    Phi.initEnv ();
    Phi.assertException "We are hosed, man..." (fun () ->
        failwith "We are hosed, man...");

    Phi.println "phiQuickTest_1: PASSED"

let runQuickTests () =
    quickTest_1 ()

let () =
    Phi.sayHello ();
    runQuickTests ()




