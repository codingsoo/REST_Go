NO_INTERVAL=6
TIME_INTERVAL=10

SET=$(seq 1 "$NO_INTERVAL")
for i in $SET
do
    sleep "$TIME_INTERVAL"m
    java -jar org.jacoco.cli-0.8.7-nodeps.jar dump --address localhost --port "$1" --destfile jacoco_"$1"_"$i".exec
done
