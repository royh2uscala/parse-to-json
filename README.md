
pwd
parse-to-json

rm OutcomeFile.json
curl -X POST "http://localhost:8081/api/somebizdomain/upload"   -F "file=@EntryFile.txt"   -o OutcomeFile.json
cat OutcomeFile.json

====================================

docker-compose down -v

docker exec -it postgres-db psql -U devco01 -d requestinfodb