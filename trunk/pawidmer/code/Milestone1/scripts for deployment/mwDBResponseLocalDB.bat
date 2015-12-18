@echo off 
for %%x in (12) do ( 
for %%y in (1) do ( 
SET PGPASSWORD=wurst& "C:\Program Files\PostgreSQL\9.4\bin\psql" -h 127.0.0.1 -U postgres -d ASL -a -f "C:\Users\Admin\Desktop\Synced\ASLCURRENT\DBstatements\AllinOne.sql"
start /wait java -jar mw.jar 127.0.0.1 6013 0 %%x 60000 0 0 1 0 %%y 1))