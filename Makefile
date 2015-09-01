all: jar

jar: main
	jar -cvfm main.jar manifest.txt Parser.class
	chmod +x main.jar

main: src/Parser.java
	javac src/Parser.java -d .
