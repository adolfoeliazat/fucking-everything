@echo off
call config.cmd

rmdir /q /s %DATA_DIR%
%PG_HOME%\bin\initdb -D %DATA_DIR% -U postgres -E UTF8
copy %~dp0\pg-test.conf %DATA_DIR%\postgresql.conf
%PG_HOME%\bin\pg_ctl start -w -D %DATA_DIR%

