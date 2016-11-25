/*
Utiliser thpool_pause si probleme d'accès à lnk_client etc.
TODO : corriger pb quand deco d'un client
*/

#include <stdlib.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h> 
#include <arpa/inet.h>
#include <unistd.h>
#include <pthread.h>
#include <sys/select.h>
#include <sys/time.h>
#include <signal.h>

#include "config.h"
#include "tracker.h"
#include "link.h"
#include "file.h"
#include "client.h"
#include "functions.h"
#include "thpool.h"

#define BUFFER_SIZE 1024
#define THREAD_NB 10
#define TIME_OUT 5
#define TIME_CO 120

int to_destroy;
int sd;

void close_program(int signo) {
  printf("Program closing\n");
  to_destroy = 1;
  thpool_destroy(thpool);
  lnk__free(client_lnk);
  socket_close(sd);
}


/* initialize the socket (socket + bind + listen)
   Return the socket descriptor */
int socket_init(){
    int sd;
    if ((sd = socket(AF_INET,SOCK_STREAM,0)) == -1)
    {
	perror("socket");
	exit(EXIT_FAILURE);
    }

    struct sockaddr_in sockAddr;

    sockAddr.sin_family = AF_INET;
    sockAddr.sin_addr.s_addr = inet_addr(tracker_address);
    sockAddr.sin_port = htons(tracker_port);

    if (bind(sd, (const struct sockaddr *)&sockAddr, sizeof(sockAddr)) == -1) {
	perror("bind");
	exit(EXIT_FAILURE);
    }

    if (listen(sd, MAX_NB_CONNEXIONS) == -1) {
	perror("listen");
	exit(EXIT_FAILURE);
    }

    return sd;
}

//Close the socket
void socket_close(int sd) {
    if(close(sd) == -1) {
	perror("close");
	exit(EXIT_FAILURE);
    }
    free(tracker_address);
}

void client_remove(struct elmt * el)
{
  
  while(client_lnk->active);
  client_lnk->active = 1;
  
    if (el->client != NULL) {
	printf("\nClient %d left\n", el->client->sd);
	FD_CLR(el->client->sd, &active_set);
	file_free(el->client->files);
	el->client->files = NULL;
	free(el->client);
    }
    lnk__remove(client_lnk, el); 
    client_lnk->active = 0;
}

void *client_thread(void * e){
    struct elmt *el = e;
    char buffer[BUFFER_SIZE];
    struct client * cl = el->client;
    struct command * cmd;
    int announced = (-1 != cl->port);
    int size;
    int res;
    if(!announced) {
	printf("0\n");
	if ((size = recv(cl->sd, buffer, BUFFER_SIZE,0)) < 0) {
	    perror("recv");
	    client_remove(el);
	    exit(EXIT_FAILURE);
	}
	if (size) {
	    cmd = parse(buffer);
	    if(cmd != NULL) {
		if (cmd->type == ANNOUNCE) {
		    if (!(res = announce(cl, cmd))) {
			announced = 1;
		    }
		}
		cmd_free(cmd);
		cmd = NULL;
		print_database(); //AFFICHE BASE DE DONNEES
	    }
	    else {
		send(cl->sd, "Please, announce yourself before any operations\n", 50, 0);
	    }
	    cl->is_unactive = 1;
	}
	else {
	    client_remove(el);

	}
    }
    else {
	size = recv(cl->sd, buffer, BUFFER_SIZE,0);
	if (size < 0) {
	    perror("recv");
	    client_remove(el);
	    exit(EXIT_FAILURE);
	}
	else if (size != 0) {
	    cmd = parse(buffer);
	    if(cmd != NULL) {
		switch (cmd->type) {
		case ANNOUNCE :
		    announce(cl, cmd);
		    break;
		case UPDATE :
		    announce(cl, cmd);
		    break;
		case LOOK :
		    look(cl, cmd);
		    break;
		case GETFILE :
		    getfile(cl, cmd);
		    break;
		}//switch
		cmd_free(cmd);
		cmd = NULL;
	    }//if
	    cl->is_unactive = 1;
	}//else if
	else {
	    client_remove(el);
	}
    }
    return NULL;
}

void fdset_init() {
    FD_ZERO(&active_set);
    maxfd = 0;
}
/*
void *client_is_alive(void *e) {
    struct elmt *el = e;
    char* buf = "alive\n";
    if (send(el->client->sd, buf, strlen(buf), MSG_DONTWAIT) == -1)
        client_remove(el);
    else
	el->client->is_unactive = 1;
    return NULL;
}
*/
void *tracker_select (void * arg) {
    int result;
    struct timeval tv;
    tv.tv_sec = TIME_OUT;
    tv.tv_usec = 0;
    struct elmt *el;
    /*   time_t t1;
    time_t t2;
    time(&t1);*/
    while(1){
      if(to_destroy)
	break;
      /*
	time(&t2);
	if(difftime(t1, t2) > TIME_CO) {
	    t1 = t2;
	    for(el = client_lnk->head;
		!lnk__is_end_mark(client_lnk, el);
		el = lnk__next(client_lnk, el))
		if (el->client->is_unactive) {
		    el->client->is_unactive = 0;
		       thpool_add_work(thpool, client_is_alive(el), (void *)el);
		}

	}
      */
	if(maxfd) {
	    read_set = active_set;
	    result = select(maxfd + 1, &read_set, NULL, NULL, &tv);
	    if (result == -1) {
		fprintf(stderr, "Error : select");
	    }
	    else {
	      if(result) {
		
		while(client_lnk->active);
		client_lnk->active = 1;
		
		el = client_lnk->head;
		while(!lnk__is_end_mark(client_lnk, el)) {
		  int ready = FD_ISSET(el->client->sd, &read_set);
		  if(ready  && el->client->is_unactive) {
		    el->client->is_unactive = 0;
		    thpool_add_work(thpool, client_thread, (void *)el);
		  }
		  el = lnk__next(client_lnk, el);
		}
		client_lnk->active = 0;
	      }
		  /*
		  for(el = client_lnk->head;
		      !lnk__is_end_mark(client_lnk, el);
			el = lnk__next(client_lnk, el)) {
			int ready = FD_ISSET(el->client->sd, &read_set);
			if(ready  && el->client->is_unactive) {
			  el->client->is_unactive = 0;
			  thpool_add_work(thpool, client_thread, (void *)el);
		    }
		    }//for*/
	    }//else
	}//if
    }//while
    return NULL;
}

int main(int argc, char ** argv){
  to_destroy = 0;
  signal(SIGINT, close_program);
    config_init();
    sd = socket_init();
    fdset_init(); // init fdset
    client_lnk = lnk__empty();
    thpool = thpool_init(THREAD_NB);
    
    thpool_add_work(thpool, tracker_select, NULL);
    while (1) {
	int sdClient;
	struct sockaddr sockAddrClient;
	unsigned int sizeSA = sizeof(sockAddrClient);

	if ((sdClient = accept(sd, &sockAddrClient, &sizeSA)) == -1) {
	    perror("Program has been closed or accept error");
	    exit(EXIT_FAILURE);
	}

	struct elmt * el = elmt__empty();
	el->client->sd = sdClient;

	struct sockaddr_in *sa = (struct sockaddr_in*) &sockAddrClient;
	el->client->ip = inet_ntoa(sa->sin_addr);
	el->client->port = -1;
	el->client->files = NULL;
	el->client->is_unactive = 1;
	printf("\nnew connexion\n");
	FD_SET(sdClient, &active_set);
	if (maxfd < sdClient)
	    maxfd = sdClient;
	client_lnk->active = 1;
	lnk__add_tail(client_lnk, el);
	client_lnk->active = 0;
    } 
    socket_close(sd);
    return 0;
}
