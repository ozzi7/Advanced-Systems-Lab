@echo off 
for %%x in (12) do ( 
for %%y in (3) do ( 
SET PGPASSWORD=wurst& "C:\Program Files\PostgreSQL\9.4\bin\psql" -h 192.168.0.21 -U postgres -d ASL -a -f "C:\Users\Admin\Desktop\Synced\ASLCURRENT\DBstatements\AllinOne.sql"
start /wait java -jar mw.jar 192.168.0.21 6013 0 %%x 122000 0 0 0 1 %%y 1))