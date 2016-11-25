#ifndef _PARSER_H_
#define _PARSER_H_

#include "file.h"

enum type {ANNOUNCE, LOOK, GETFILE, UPDATE};

struct criterion {
  char name[50];
  char operator;
  char value[100];
};

struct command {
  char name[30];
  enum type type;

  //Announce
  int announce_port;
  int nb_seed_files;
  int nb_leech_files;
  struct file_info *announce_seed;
  struct file_info *announce_leech;

  //Look
  struct criterion *look_criterion;
  int nb_criterions;

  //Getfile
  char getfile_key[50];
};

struct command *parse(char* buffer);
void cmd_free(struct command *cmd);

#endif
