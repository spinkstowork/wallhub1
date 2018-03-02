@REM SDP
@REM minor deviation, jar is named parser-1.0-SNAPSHOT.jar vs. parser.jar
@REM Use maven so classpath is managed properly

@REM Command line switch: --accesslog=/path/to/file 
@REM The requirements were somewhat vague on this. 
@REM So, check src/main/resources/jdbc.properties instead for filename
@REM By doing this, DB is not loaded every time the program runs
@REM If you wish to re-init the DB, use the init.sql script

@REM Also needed is the -Dexec.cleanupDaemonThreads=false
@REM Ref: https://stackoverflow.com/questions/13471519/running-daemon-with-exec-maven-plugin-avoiding-illegalthreadstateexception

call mvn exec:java -Dexec.mainClass=com.ef.Parser -Dexec.args="--startDate=2017-01-01.13:00:00 --duration=daily --threshold=100" -Dexec.cleanupDaemonThreads=false
