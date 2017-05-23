@echo off
call config.cmd

%PG_HOME%\bin\pg_ctl stop -D %DATA_DIR%

