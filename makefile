JFLAGS = -g -s src -cp "./javalib-1.0.3.jar:lib" -d bin
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	src/CannonGame.java \

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
	$(RM) bin/*.class

run:
	java -cp bin:javalib-1.0.3.jar CannonGame