SET=$(seq 0 24)
for i in $SET
do
    sleep 1h
    java -jar org.jacoco.cli-0.8.7-nodeps.jar dump --address localhost --port "$1" --destfile jacoco_"$1"_"$i".exec
done
