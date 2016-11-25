#include "functions.h"
       

//Traite la commande announce, met à jour la struct client et répond sur la socket
int announce(struct client *c, struct command *cmd) {

  if (c->port == -1)
    c->port = cmd->announce_port;

  if (cmd->type != ANNOUNCE && cmd->type != UPDATE) {
    return -1;
  }
  
  //Ajout des fichiers seed à la liste de fichiers du client
  for (int i = 0; i < cmd->nb_seed_files; i++) {
    if (!file_is_present(c->files, cmd->announce_seed[i].key)) {
      struct file *f = malloc(sizeof(*f));
      f->info = malloc(sizeof(struct file_info));

      fprintf(stderr,"size:%d\n", cmd->announce_seed[0].length);

      int j;
      if (cmd->type == ANNOUNCE) {
	j = 0;
	while (cmd->announce_seed[i].name[j] != '\0') {
	  f->info->name[j] = cmd->announce_seed[i].name[j];
	  j++;
	}
	f->info->name[j] = '\0';

	f->info->length = cmd->announce_seed[i].length;
	f->info->piece_size = cmd->announce_seed[i].piece_size;
      }
      else {
	f->info->name[0] = '*';
	f->info->name[0] = '\0';
      }

      j = 0;
      while (cmd->announce_seed[i].key[j] != '\0') {
	f->info->key[j] = cmd->announce_seed[i].key[j];
	j++;
      }
      f->info->key[j] = '\0';
  
      file_add(c, f);
    }
  }

  //Ajout des fichiers leech à la liste de fichiers du client ("*",-1,-1,Key)
  for (int i = 0; i < cmd->nb_leech_files; i++) {
    if (!file_is_present(c->files, cmd->announce_leech[i].key)) {
    struct file *f = malloc(sizeof(*f));
    f->info = malloc(sizeof(struct file_info));
	
    f->info->name[0] = '*';
    f->info->name[1] = '\0';

    f->info->length = -1;
    f->info->piece_size = -1;

    int j = 0;
    while (cmd->announce_leech[i].key[j] != '\0') {
      f->info->key[j] = cmd->announce_leech[i].key[j];
      j++;
    }
    f->info->key[j] = '\0';
  
    file_add(c, f);
    }
  }

  // Réponse "OK" sur la socket
  if (send(c->sd, "ok\n", strlen("ok\n"), 0) == -1) {
    perror("send");
    exit(EXIT_FAILURE);
  }
  printf("Sent: ok\n");

  return 0;
}

//Traite la commande look, cherche les fichiers correspondants et les envoie au client
int look(struct client *c, struct command *cmd)
{
  if (cmd->type != LOOK) {
    return -1;
  }

  struct client *valid_files = malloc(sizeof(struct client));
  valid_files->files = NULL;
  
  //Vérifie le premier critère, et crée une liste chainée des fichiers correspondants
  struct elmt *e = client_lnk->head;
  while (!lnk__is_end_mark(client_lnk, e)) {
    if (e->client->sd != c->sd) {
      struct file *f = e->client->files;

      while (f != NULL) {
	if (criterion_respected(&(cmd->look_criterion[0]), f->info) && !file_is_present(valid_files->files, f->info->key)) {
	  struct file *new_file = malloc(sizeof(struct file));
	  new_file->info = f->info;
	  file_add(valid_files,new_file);
	  printf("File %s: OK\n", f->info->name);
	}
	f = f->next;
      }
    }
    e = lnk__next(client_lnk, e);
  }

  //Vérifie les critères suivants en retirant les fichiers de la liste chainée s'ils ne les respectent pas
  for (int i = 1; i < cmd->nb_criterions; i++) {
    struct elmt *e = client_lnk->head;

    while (!lnk__is_end_mark(client_lnk, e)) {
      struct file *f = e->client->files;

      while (f != NULL) {
	if (!criterion_respected(&(cmd->look_criterion[i]), f->info))
	  file_remove(valid_files, f->info->key);
	f = f->next;
      }
      
      e = lnk__next(client_lnk, e);
    }
  }

  // Réponse sur la socket
  char *list_files = "";
  int allocated = 0;
  char *answer;
  struct file *f = valid_files->files;

  int count = 0;
  while (f != NULL) {
    allocated = 1;
    if (count == 0)
      asprintf(&list_files, "%s %d %d %s", f->info->name, f->info->length, f->info->piece_size, f->info->key);
    else
      asprintf(&list_files, "%s %s %d %d %s", list_files, f->info->name, f->info->length, f->info->piece_size, f->info->key);  
    f = f->next;
    count++;
  }
  asprintf(&answer,"list [%s]\n", list_files);

  if (send(c->sd, answer, strlen(answer), 0) == -1) {
    perror("send");
    file_free(valid_files->files);
    free(valid_files);
    exit(EXIT_FAILURE);
  }
  printf("Sent: %s\n", answer);

  if (allocated)
    free(list_files);
  free(answer);

  struct file *current = valid_files->files;
  struct file *next_f = valid_files->files;
  while (current != NULL) {
    next_f = current->next;
    free(current);
    current = next_f;
  }
  free(valid_files);
  return 0;
}

//Parcourt les fichiers de chaque client et retourne l'ip et le port des clients possédant le fichier demandé
int getfile(struct client *c, struct command *cmd)
{
  if (cmd->type != GETFILE) {
    return -1;
  }

  struct client clients[MAX_NB_CONNEXIONS];
  for (int i = 0; i < MAX_NB_CONNEXIONS; i++)
    clients[i].port = -1;
  
  int count = 0;
  struct elmt *e = client_lnk->head;
  while (!lnk__is_end_mark(client_lnk, e)) {
    if (file_is_present(e->client->files, cmd->getfile_key)) {
      clients[count] = *(e->client);
      count++;
    }
    e = lnk__next(client_lnk, e);
  }

  char *list_clients;
  char *answer;

  for (int i = 0; i < count; i++) {
    if (i == 0)
      asprintf(&list_clients, "%s:%d", clients[i].ip, clients[i].port);
    else
      asprintf(&list_clients, "%s %s:%d", list_clients, clients[i].ip, clients[i].port); 
  }

  asprintf(&answer,"peers %s [%s]\n", cmd->getfile_key, list_clients);

  if (send(c->sd, answer, strlen(answer), 0) == -1) {
    perror("send");
    exit(EXIT_FAILURE);
  }
  printf("Sent: %s\n", answer);

  free(list_clients);
  free(answer);
  return 0;
}

//Affiche l'ensemble des clients connectés, et leurs fichiers
void print_database() {
  struct elmt *e = client_lnk->head;
  printf("\nDATABASE:\n\n");
  while (!lnk__is_end_mark(client_lnk, e)) {
    printf("Client %d [%s:%d]:\n", e->client->sd, e->client->ip, e->client->port);

    struct file *f = e->client->files;
    int i = 1;
    while (f != NULL) {
      printf("File %d: %s (%d octets)\n", i, f->info->name, f->info->length);
      f = f->next;
      i++;
    }

    e = lnk__next(client_lnk, e);
    printf("\n");
  }
}

//Retourne 1 si le fichier file respecte le critère crit, 0 s'il ne respecte pas, -1 si le critère est inconnu
int criterion_respected(struct criterion *crit, struct file_info *f) {
  if (!strcmp(crit->name, "filename")) { //Recherche par nom
    if (!strcmp(crit->value, f->name))
      return 1;
    else
      return 0;
  }
  else if (!strcmp(crit->name, "filesize")) {
    switch (crit->operator) {
    case '=':
      if (f->length == atoi(crit->value))
	return 1;
      else
	return 0;

    case '<':
      if (f->length < atoi(crit->value))
	return 1;
      else
	return 0;

    case '>':
      if (f->length > atoi(crit->value))
	return 1;
      else
	return 0;

    default:
      return 1;
    }
  }
  else
    return -1;
}
