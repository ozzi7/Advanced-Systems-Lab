@echo off 
SET PGPASSWORD=wurst& "C:\Program Files\PostgreSQL\9.4\bin\psql" -h 192.168.0.21 -U postgres -d ASL -a -f "C:\Users\Admin\Desktop\Synced\ASLCURRENT\DBstatements\AllinOne.sql"
start /wait java -jar mw.jar 192.168.0.21 6014 15 12 2000000 1 1 1 1 0 16