;---------- Disable Alt+Numpad character code input ----------

!NumpadIns::SendInput !{Ins}
!NumpadEnd::SendInput !{End}
!NumpadDown::SendInput !{Down}
!NumpadPgDn::SendInput !{PgDn}
!NumpadLeft::SendInput !{Left}
; !NumpadClear::SendInput !{Right}
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

!NumpadClear::
    clipboard = ; Empty the clipboard
    Send, ^c
    ClipWait, 1
    if ErrorLevel
    {
        MsgBox, Can't copy your shit to clipboard
        return
    }
    ; MsgBox Control-C copied the following contents to the clipboard:`n`n%clipboard%
    Run, C:\WINDOWS\system32\cmd.exe /c e:\fegh\_run.cmd vgrechka.globalmenu.StartGlobalMenu ,,Hide
    return

NumpadClear:: Run, C:\WINDOWS\system32\cmd.exe /c e:\fegh\_run.cmd vgrechka.globalmenu.StartGlobalMenu ,,Hide
















