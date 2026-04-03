cd toolbox-ui
npm run build
cp -r dist/* ../toolbox/src/main/resources/static
cd ../toolbox
mvn clean package
rm -rf ../toolbox/src/main/resources/static