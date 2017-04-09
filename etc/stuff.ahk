;---------- Disable Alt+Numpad character code input ----------

!NumpadIns::SendInput !{Ins}
!NumpadEnd::SendInput !{End}
!NumpadDown::SendInput !{Down}
!NumpadPgDn::SendInput !{PgDn}
!NumpadLeft::SendInput !{Left}
!NumpadClear::SendInput !{Right}
; !NumpadClear::SendInput !{Clear}
!NumpadRight::SendInput !{Right}
!NumpadHome::SendInput !{Home}
!NumpadUp::SendInput !{Up}
!NumpadPgUp::SendInput !{PgUp}
!NumpadDel::SendInput !{Del}

+NumpadIns::SendInput +{Ins}
+NumpadEnd::SendInput +{End}
+NumpadDown::SendInput +{Down}
+NumpadPgDn::SendInput +{PgDn}
+NumpadLeft::SendInput +{Left}
+NumpadClear::SendInput +{Right}
; +NumpadClear::SendInput +{Clear}
+NumpadRight::SendInput +{Right}
+NumpadHome::SendInput +{Home}
+NumpadUp::SendInput +{Up}
+NumpadPgUp::SendInput +{PgUp}
+NumpadDel::SendInput +{Del}

+!NumpadIns::SendInput +!{Ins}
+!NumpadEnd::SendInput +!{End}
+!NumpadDown::SendInput +!{Down}
+!NumpadPgDn::SendInput +!{PgDn}
+!NumpadLeft::SendInput +!{Left}
+!NumpadClear::SendInput +!{Right}
; +!NumpadClear::SendInput +!{Clear}
+!NumpadRight::SendInput +!{Right}
+!NumpadHome::SendInput +!{Home}
+!NumpadUp::SendInput +!{Up}
+!NumpadPgUp::SendInput +!{PgUp}
+!NumpadDel::SendInput +!{Del}

^!NumpadIns::SendInput ^!{Ins}
^!NumpadEnd::SendInput ^!{End}
^!NumpadDown::SendInput ^!{Down}
^!NumpadPgDn::SendInput ^!{PgDn}
^!NumpadLeft::SendInput ^!{Left}
^!NumpadClear::SendInput ^!{Right}
; ^!NumpadClear::SendInput ^!{Clear}
^!NumpadRight::SendInput ^!{Right}
^!NumpadHome::SendInput ^!{Home}
^!NumpadUp::SendInput ^!{Up}
^!NumpadPgUp::SendInput ^!{PgUp}
^!NumpadDel::SendInput ^!{Del}

^+!NumpadIns::SendInput ^+!{Ins}
^+!NumpadEnd::SendInput ^+!{End}
^+!NumpadDown::SendInput ^+!{Down}
^+!NumpadPgDn::SendInput ^+!{PgDn}
^+!NumpadLeft::SendInput ^+!{Left}
^+!NumpadClear::SendInput ^+!{Right}
; ^+!NumpadClear::SendInput ^+!{Clear}
^+!NumpadRight::SendInput ^+!{Right}
^+!NumpadHome::SendInput ^+!{Home}
^+!NumpadUp::SendInput ^+!{Up}
^+!NumpadPgUp::SendInput ^+!{PgUp}
^+!NumpadDel::SendInput ^+!{Del}


;---------- Stuff ----------

NumpadClear:: Run, C:\WINDOWS\system32\cmd.exe /c e:\fegh\_run.cmd vgrechka.globalmenu.StartGlobalMenu ,,Hide
















