#ifndef FILE_H_
#define FILE_H_

#include <stdlib.h>
#include <string.h>
#include "client.h"
#include "parser.h"

struct file {
  struct file_info *info;
  struct file *next;
};

struct file_info {
  char name[200];
  int length;
  int piece_size;
  char key[200];
};

//Ajoute un fichier à la liste de fichiers d'un client
void file_add(struct client *c, struct file *new_file);
//Supprime un fichier de la liste de fichiers d'un client
void file_remove(struct client *c, char *key_file);
//Recherche un fichier par sa clé dans une liste: 1 si présent, 0 sinon
int file_is_present(struct file *file, char *key_file);
//Libère la liste de files
void file_free(struct file *f);

#endif
