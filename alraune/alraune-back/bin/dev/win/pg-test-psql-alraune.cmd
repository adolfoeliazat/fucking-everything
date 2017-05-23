@echo off
call config.cmd

%PG_HOME%\bin\psql -h 127.0.0.1 -p 5433 -U postgres -w alraune-test


