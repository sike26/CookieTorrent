#ifndef CLIENT_H_
#define CLIENT_H_

struct client {
  int sd;
  char *ip;
  int port;
  struct file *files;
    int is_unactive;
};

#endif
