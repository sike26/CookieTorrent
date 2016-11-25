#include "file.h"
#include "stdio.h"

//Ajoute un fichier à la liste de fichiers d'un client
void file_add(struct client *c, struct file *new_file) {
  new_file->next = c->files;
  c->files = new_file;
}

//Supprime un fichier de la liste de fichiers d'un client s'il le trouve
void file_remove(struct client *c, char *key_file) {
  struct file *f = c->files;
  struct file *prev = c->files;
  int count = 0;
    while (f != NULL) {
      if (strcmp(f->info->key, key_file) == 0) {
	if (count == 0) {
	  c->files = f->next;
	}
	else {
	  prev->next = f->next;
	}
	f->next = NULL;
	file_free(f);
	return;
      }

      prev = f;
      f = f->next;
      count++;
    }
}

int file_is_present(struct file *file, char *key_file) {
  struct file *f = file;
  while (f != NULL) {
    if (!strcmp(f->info->key, key_file))
      return 1;
    f = f->next;
  }
  return 0;
}

//Libère la liste de files
void file_free(struct file *f) {
  struct file *current = f;
  struct file *next_f = f;

  while (current != NULL) {
    next_f = current->next;
    free(current->info);
    free(current);
    current = next_f;
  }
}
