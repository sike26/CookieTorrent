#ifndef FUNCTIONS_H_
#define FUNCTIONS_H_

#define _GNU_SOURCE
#include <stdlib.h>
#include <stdio.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#include "parser.h"
#include "file.h"
#include "tracker.h"

int announce(struct client *c, struct command *cmd);
int look(struct client *c, struct command *cmd);
int getfile(struct client *c, struct command *cmd);
void print_database();
int criterion_respected(struct criterion *crit, struct file_info *f);

#endif
