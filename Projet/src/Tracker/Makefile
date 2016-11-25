CFLAGS = -Wall -std=gnu99 -g -I headers
CC=gcc
LDFLAGS= -lpthread

all:tracker

tracker:tracker.o config.o link.o file.o functions.o parser.o thpool.o
	$(CC) $(CFLAGS) -o $@ $^ $(LDFLAGS)

tracker-old:tracker-old.o config.o link.o file.o functions.o parser.o thpool.o
	$(CC) $(CFLAGS) -o $@ $^ $(LDFLAGS)

tracker-old.o:tracker-old.c
	$(CC) $(CFLAGS) -o $@ -c $< $(LDFLAGS)

tracker.o:tracker.c
	$(CC) $(CFLAGS) -o $@ -c $< $(LDFLAGS)

config.o:config.c
	$(CC) $(CFLAGS) $(LDFLAGS) -o $@ -c $<

link.o:link.c
	$(CC) $(CFLAGS) -o $@ -c $<

file.o:file.c
	$(CC) $(CFLAGS) -o $@ -c $<

functions.o:functions.c
	$(CC) $(CFLAGS) -o $@ -c $<

parser.o:parser.c
	$(CC) $(CFLAGS) -o $@ -c $<

thpool.o:thpool.c
	$(CC) $(CFLAGS) -o $@ -c $<

test:test.o parser.o functions.o file.o
	$(CC) $(CFLAGS) $(LDFLAGS) -o $@ $^

test.o:test.c

clean:
	rm -f *.o *~
