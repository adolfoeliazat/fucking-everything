open Core.Std

module rec Value : sig
    type t =
    | Object of Object_value.t
    | String of String_value.t
    | Undefined
    | Function of Function_value.t
end = Value

    and Object_value : sig type t = {
        fields : Value.t String.Table.t;
        props : Object_value.t
    } end = Object_value

    and String_value : sig type t = {
        value : string
    } end = String_value

    and Function_value : sig type t = {
        todo : unit
    } end = Function_value


module rec Expression : sig
    type t =
    | New of New_expression.t
    | Number_literal of Number_literal.t
    | String_literal of String_literal.t
    | Name_ref of Name_ref.t
end = Expression

    and String_literal : sig type t = {
        value : string
    } end = String_literal

    and Number_literal : sig type t = {
        value : float
    } end = Number_literal

    and Name_ref : sig type t = {
        name : string
    } end = Name_ref

    and New_expression : sig type t = {
        ctor : Expression.t; args : Expression.t list
    } end = New_expression


let println s = print_string (s ^ "\n")

let say_hello () =
    println "Pretty much fuck you";
    println "Yeah, right..."

let init_env () : unit =
    ()

let as_object (x : Value.t) : Object_value.t =
    match x with
    | Value.Object x -> x
    | _ -> failwith "b525635e-c148-4689-ac3b-495a27c48f3c"

let as_function (x : Value.t) : Function_value.t =
    match x with
    | Value.Function x -> x
    | _ -> failwith "7f53a927-7b8a-4886-a4a3-28b6ab8179b5"

let object_get_property (this : Object_value.t) ?(receiver : Object_value.t = this) (name : string) : Value.t =
    if (array__key__exists($name, $this->fields)) {
        return $this->fields[$name];
    }

    if (array__key__exists($name, $this->props)) {
        $prop = $this->props[$name];
        $getter = $prop->get_property('get');
        if ($getter->is_truthy()) {
            if (!($getter instanceof Phi_function))
                throw new Phi_illegal_state_exception("a88c164b-b4a9-4228-9b0a-faf92cc797cd");
            $receiver = @$opts['receiver'] ?: $this;
            $res = $getter->invoke($receiver, array());
            return $res;
        }
    }

    $proto = $this->get_proto();
    if ($proto != null) {
        return $proto->get_property($name, array('receiver' => @$opts['receiver'] ?: $this));
    } else {
        if (@$opts['php_null_if_not_found'])
            return null;
        else
            return new Phi_undefined();
    }

let phi_new (ctor_value : Function_value.t) (arg_values : Value.t list) : Object_value.t =
    let proto = as_object (object_get_property ctor_value "prototype") in
    let inst = Object_value.create ~proto:proto in
    invoke ctor_value inst arg_values;
    inst

let rec evaluate (expr : Expression.t) : Value.t =
    match expr with
    | New this ->
        (* Example:
               function Shit(a, b) {this.a = a; this.b = b}
               new Shit(a, b) *)

        let ctor_value = as_function (evaluate this.ctor) in
        let arg_values = List.map this.args ~f:evaluate in
        Value.Object (phi_new ctor_value arg_values)

    | Number_literal x -> failwith "691e47ac-ad9b-4769-8a71-de3b844d6420"
    | String_literal x -> failwith "cf56d27b-1602-4eb0-8edf-85521d98bf33"
    | Name_ref x -> failwith "d4301b19-2455-46ee-bff9-7441069032db"

let debug_value_to_string (value : Value.t) : string =
    match value with
    | Value.Object _ -> sprintf "(Value.Object %s)" "some"
    | Value.String x -> sprintf "(Value.String %s)" x.value
    | Value.Undefined -> "Value.Undefined"
    | Value.Function _ -> "Value.Function"

let throw (expr : Expression.t) : unit =
    let phi_value = evaluate expr in

    match phi_value with
    | Value.Object x -> ()
    | _ -> failwith (sprintf "%s    d6b5d1bf-c9d9-4aa7-b9f9-420bd0124b1f" (debug_value_to_string phi_value))

    (*
    match phi_value with
    | Value.Object x -> (
        let message_phi_value = object_get_property x "message" in

        let message = match message_phi_value with
        | Value.String x -> x.value
        | Value.Undefined -> ""
        | _ -> failwith "cbba8949-ba96-43d5-93f1-dd84bd002d67" in
            raise (Bloody_exception {message; phi_value})
    )
    | _ -> failwith "d6b5d1bf-c9d9-4aa7-b9f9-420bd0124b1f"
    *)

let assert_string_equals (expected : string) (actual : string) : unit =
    if not (actual = expected) then
        failwith ("Expected: " ^ expected ^ "; Actual: " ^ actual)

let assert_exception (expected_message : string) (f : unit -> unit) : unit =
    let got_exception = ref false in
    (try
        f ();
    with Failure msg ->
        got_exception := true;
        assert_string_equals expected_message msg);
    if not !got_exception then
        failwith "I want an exception"




