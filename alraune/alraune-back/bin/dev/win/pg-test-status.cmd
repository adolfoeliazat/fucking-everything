@echo off
call config.cmd

%PG_HOME%\bin\pg_ctl status -D %DATA_DIR%

