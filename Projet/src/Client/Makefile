BUILD=./build
PARSER=./Parser
TESTS=./Tests
TOOLS=./Tools
INTERFACE=./Interface
TREATMENT=./RequestTreatment
DOWNLOAD=./DownloadManager
COMPILE= ToolsC ParserC RequestTreatmentC InterfaceC DownloadC SourceC Test	

default: $(COMPILE)

ParserC: $(PARSER)/*.java
	javac -Xlint -d $(BUILD)  -cp $(BUILD):$(BUILD) $(PARSER)/*.java

ToolsC: $(TOOLS)/*.java
	javac -Xlint -d $(BUILD) -cp $(BUILD):$(BUILD) $(TOOLS)/*java

RequestTreatmentC: $(TREATMENT)/*.java	
	javac -Xlint -d $(BUILD) -cp $(BUILD):$(BUILD) $(TREATMENT)/*java

SourceC: ./*java
	javac -Xlint -d $(BUILD) -cp $(BUILD):$(BUILD) ./*java

InterfaceC: $(INTERFACE)/*.java
	javac -Xlint -d $(BUILD) -cp $(BUILD):$(BUILD) $(INTERFACE)/*java

DownloadC : $(DOWNLOAD)/*.java
	javac -Xlint -d $(BUILD) -cp $(BUILD):$(BUILD) $(DOWNLOAD)/*java

Test: $(TESTS)/*.java
	javac -Xlint -d $(BUILD) -cp $(BUILD):$(BUILD) $(TESTS)/*.java


run: ToolsC ParserC SourceC Test
	java -ea -cp build:build/ parseTest 
	java -ea -cp build:build/ lancerTest

client:ToolsC ParserC RequestTreatmentC SourceC 
	java -cp build:build/ CookieClient.Client 

clean:
	rm -rf $(BUILD)/*

cookie: 
	java -cp build:build/ CookieClient.Client
