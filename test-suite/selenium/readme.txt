For run selenium testing process on cloudworkspaces execute next steps:
1. After creation tenenat for owner go to test-suite/selenium/src/test/resources/conf/cw-selenium.properties
2. Set your credentials for owner follows :
#root user
cw.user.root.name=your login
cw.user.root.password=your password
3. Set your credentials for invited user follows :
#second user
cw.seconduser.root.name=login for invited yser
cw.seconduser.root.password=password for invited user
4. Set your host with domen prefix 
cw.host=your domen 
cw.tenant=your tenant
for example if we created tenant on mail@ukr.net 
After configuration properties, run tests next command:
mvn clean integration-test -Pselenium-test -Dtest=PlatformTestSuite