cat ./Template.html | sed -e "s/USERSNICK/$1/g" > $1.html
appletviewer -J"-Djava.security.policy=all.policy" $1.html &
sleep 1
rm -rf $1.html
