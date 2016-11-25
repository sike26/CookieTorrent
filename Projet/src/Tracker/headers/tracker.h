#ifndef _TRACKER_
#define _TRACKER_

#include "link.h"
#include "thpool.h"

#define MAX_NB_CONNEXIONS 10

struct link * client_lnk;
threadpool thpool;

fd_set read_set;
fd_set active_set;
int maxfd;

int socket_init();
void socket_close(int sd);
void close_program(int signo);
void *client_thread(void * el);
void client_remove(struct elmt * el);
void *tracker_select (void *);
void fdset_init();

#endif
