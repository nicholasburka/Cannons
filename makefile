JFLAGS = -g -classpath javalib-1.0.3.jar
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	CannonGame.java \

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class