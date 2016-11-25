#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "parser.h"

// Receive one command in the buffer ended by '\0' and fill a struct command
// Return the allocated struct command or NULL if the command is invalid
struct command *parse(char* buffer) {
  fprintf(stderr, "\nReceived: ");
  int d = 0;
  while (buffer[d] != ']' && buffer[d] != '\0' && buffer[d] != '\n') {
    fprintf(stderr, "%c", buffer[d]);
    d++;
  }
  if (buffer[d] == ']')
    fprintf(stderr,"]");
  fprintf(stderr,"\n");

  struct command *cmd = malloc(sizeof(struct command));

  cmd->announce_seed = NULL;
  cmd->announce_leech = NULL;
  cmd->look_criterion = NULL;

  char word[100];
  int i = 0;
  int j;
  while (buffer[i] != ' ' && buffer[i] != '\0' && buffer[i] != '\n') {
    cmd->name[i] = buffer[i];
    i++;
  }
  cmd->name[i] = '\0';
  i++;

  //printf("name: %s\n", cmd->name);

  if (strcmp(cmd->name, "announce") == 0) { /* Process announce command */
    cmd->type = ANNOUNCE;

    //printf("traitement announce\n");
    cmd->nb_seed_files = 0;
    cmd->nb_leech_files = 0;

    /* Look for the listened port */

    i+=7;
    j = 0;
    while (buffer[i] >= '0' && buffer[i] <= '9') {
      word[j] = buffer[i];
      i++;
      j++;
    }
    word[j] = '\0';
    cmd->announce_port = atoi(word);
    
    //printf("Port: %d\n", cmd->announce_port);

    /* Look for the next keyword 'seed' or 'leech' */
    
    j = 0;
    if (buffer[i] == ' ') {
      i++;
      
      while (buffer[i] >= 'a' && buffer[i] <= 'z') {
	word[j] = buffer[i];
	i++;
	j++;
      }
      
      word[j] = '\0';
      
      if (strcmp(word, "seed") == 0) {
	
	i+=2;
	for (int k = i; buffer[k] != ']'; k++) {
	  if (buffer[k] == ' ')
	    cmd->nb_seed_files++;
	}
	
	cmd->nb_seed_files++;
        cmd->nb_seed_files /= 4;
	
	cmd->announce_seed = malloc(cmd->nb_seed_files*sizeof(struct file_info));
	
	int index = 0;
	while (buffer[i] != ']' && buffer[i] != '\0') {
	  j = 0;
	  /* Get the name of the file */
	  
	  while (buffer[i] != ' ' && buffer[i] != '\0') {
	    cmd->announce_seed[index].name[j] = buffer[i];
	    i++;
	    j++;
	  }

	  cmd->announce_seed[index].name[j] = '\0';
	  i++;
	  
    
	  /* Get the length of the file */
	  
	  j = 0;
	  while (buffer[i] != ' ' && buffer[i] != '\0') {
	    word[j] = buffer[i];
	    i++;
	    j++;
	  }

	  word[j] = '\0';
	  i++;

	  cmd->announce_seed[index].length = atoi(word);
	  
	  /* Get the piece size of the file */
	  j = 0;
	  while (buffer[i] != ' ' && buffer[i] != '\0') {
	    word[j] = buffer[i];
	    i++;
	    j++;
	  }
	  word[j] = '\0';
	  i++;

	  cmd->announce_seed[index].piece_size = atoi(word);
	  
	  /* Get the key of the file */
	  j = 0;
	  while (buffer[i] != ' ' && buffer[i] != ']' && buffer[i] != '\0') {
	    cmd->announce_seed[index].key[j] = buffer[i];
	    
	    i++;
	    j++;
	  }
	  cmd->announce_seed[index].key[j] = '\0';
	  
	  if (buffer[i] != ']')
	    i++;
	  
	  index++;
	  
	}

	i++;
	
	if (buffer[i] != '\0') { /* Look for the seed files */
	  i++;
	  j = 0;
	  while (buffer[i] != ' ' && buffer[i] != '\0') {
	    word[j] = buffer[i];
	    i++;
	    j++;
	  }

	  word[j] = '\0';

	  i+=2;
								  
	}
								  						  
      }
      
      if (strcmp(word, "leech") == 0) {
	while (buffer[i] != ']') {
	  for (int k = i; buffer[k] != ']' && buffer[k] != '\0'; k++) {
	    if (buffer[k] == ' ')
	      cmd->nb_leech_files++;
	  }
	  cmd->nb_leech_files++;
	    
	  cmd->announce_leech = malloc(cmd->nb_leech_files*sizeof(struct file_info));

	  int index = 0;
	  while (buffer[i] != ']' && buffer[i] != '\0') {
	    /* Get the key of the file */
	    j = 0;
	    while (buffer[i] != ' ' && buffer[i] != ']' && buffer[i] != '\0') {
	      cmd->announce_leech[index].key[j] = buffer[i];
	      i++;
	      j++;
	    }
	    cmd->announce_leech[index].key[j] = '\0';

	    if (buffer[i] != ']') {
	      i++;
	      index++;
	    }
	  }
	}
      }

				 
    }
    /*
      printf("\nSEED: %d\n", cmd->nb_seed_files);
      for (int k = 0; k < cmd->nb_seed_files; k++) {
      printf("File %d\n", k);
      printf("name: %s\n", cmd->announce_seed[k].name);
      printf("length: %d\n", cmd->announce_seed[k].length);
      printf("piece_size: %d\n", cmd->announce_seed[k].piece_size);
      printf("key: %s\n", cmd->announce_seed[k].key);
      }

      printf("\nLEECH: %d\n", cmd->nb_leech_files);
      for (int k = 0; k < cmd->nb_leech_files; k++) {
      printf("File %d\n", k);
      printf("key: %s\n", cmd->announce_leech[k].key);
      }
    */
  }
  else if (strcmp(cmd->name, "look") == 0) { /* Process look command */
    cmd->type = LOOK;
    cmd->nb_criterions = 0;
    i++;

    for (int k = i; buffer[k] != ']'; k++) {
      if (buffer[k] == ' ')
	cmd->nb_criterions++;
    }
    cmd->nb_criterions++;

    cmd->look_criterion = malloc(cmd->nb_criterions*sizeof(struct criterion));

    int index = 0;
    while (buffer[i] != ']' && buffer[i] != '\0') {
      j = 0;

      while(buffer[i] >= 'a' && buffer[i] <= 'z') {
	cmd->look_criterion[index].name[j] = buffer[i];
	i++;
	j++;
      }
      cmd->look_criterion[index].name[j] = '\0';

      cmd->look_criterion[index].operator = buffer[i];
      i+=2;

      j = 0;
      while (buffer[i] != '\"') {
	cmd->look_criterion[index].value[j] = buffer[i];
	i++;
	j++;
      }
      cmd->look_criterion[index].value[j] = '\0';
      i++;
      if (buffer[i] != ']')
	i++;
      index++;
    }

    /*
      for (int k = 0; k < cmd->nb_criterions; k++) {
      printf("Critère %d: name:%s / operator:%c / value:%s\n", k, cmd->look_criterion[k].name, cmd->look_criterion[k].operator, cmd->look_criterion[k].value);
      }
    */
  }
  else if (strcmp(cmd->name, "getfile") == 0) { /* Process getfile command */
    cmd->type = GETFILE;

    int j = 0;
    while (buffer[i] != '\0' && buffer[i] != '\n') {
      cmd->getfile_key[j] = buffer[i];
      i++;
      j++;
    }
    cmd->getfile_key[j] = '\0';
  }
  else if (strcmp(cmd->name, "update") == 0) { /* Process update seed command */
    cmd->type = UPDATE;

    //fprintf(stderr, "traitement update\n");
    cmd->nb_seed_files = 0;
    cmd->nb_leech_files = 0;

    /* If cmd = 'update' */
    if (buffer[i-1] == '\0' || buffer[i-1] == '\n')
      return cmd;

    /* Look for the next keyword 'seed' or 'leech' */
    
    j = 0;
    while (buffer[i] >= 'a' && buffer[i] <= 'z') {
      word[j] = buffer[i];
      i++;
      j++;
    }

    word[j] = '\0';
      
    if (strcmp(word, "seed") == 0) {
      i+=2;

      for (int k = i; buffer[k] != ']'; k++) {
	if (buffer[k] == ' ')
	  cmd->nb_seed_files++;
      }
      cmd->nb_seed_files++;
	
      cmd->announce_seed = malloc(cmd->nb_seed_files*sizeof(struct file_info));
	
      int index = 0;
      while (buffer[i] != ']' && buffer[i] != '\0') {	  
	/* Get the key of the file */
	j = 0;
	while (buffer[i] != ' ' && buffer[i] != ']' && buffer[i] != '\0') {
	  cmd->announce_seed[index].key[j] = buffer[i];
	    
	  i++;
	  j++;
	}
	cmd->announce_seed[index].key[j] = '\0';
	  
	if (buffer[i] != ']')
	  i++;
	  
	index++;
	  
      }

      i++;
	
      if (buffer[i] != '\0') { /* Look for the seed files */
	i++;
	j = 0;
	while (buffer[i] != ' ' && buffer[i] != '\0') {
	  word[j] = buffer[i];
	  i++;
	  j++;
	}

	word[j] = '\0';

	i+=2;
								  
      }
								  						  
    }
      
    if (strcmp(word, "leech") == 0) {
      while (buffer[i] != ']') {
	for (int k = i; buffer[k] != ']' && buffer[k] != '\0'; k++) {
	  if (buffer[k] == ' ')
	    cmd->nb_leech_files++;
	}
	cmd->nb_leech_files++;
	cmd->announce_leech = malloc(cmd->nb_leech_files*sizeof(struct file_info));

	int index = 0;
	while (buffer[i] != ']' && buffer[i] != '\0') {
	  /* Get the key of the file */
	  j = 0;
	  while (buffer[i] != ' ' && buffer[i] != ']' && buffer[i] != '\0') {
	    cmd->announce_leech[index].key[j] = buffer[i];
	    i++;
	    j++;
	  }
	  cmd->announce_leech[index].key[j] = '\0';

	  if (buffer[i] != ']') {
	    i++;
	    index++;
	  }
	}
      }
    }

    /*
    printf("\nUPDATE SEED: %d\n", cmd->nb_seed_files);
    for (int k = 0; k < cmd->nb_seed_files; k++) {
      printf("key: %s\n", cmd->announce_seed[k].key);
    }

    printf("\nUPDATE LEECH: %d\n", cmd->nb_leech_files);
    for (int k = 0; k < cmd->nb_leech_files; k++) {
      printf("key: %s\n", cmd->announce_leech[k].key);
    }
    */

  }
  else {
    return NULL;
  }
  return cmd;
}

void cmd_free(struct command *cmd) {
  if (cmd != NULL) {
    if (cmd->announce_seed != NULL) {
      free(cmd->announce_seed);
      cmd->announce_seed = NULL;
    }
    if (cmd->announce_leech != NULL) {
      free(cmd->announce_leech);
      cmd->announce_leech = NULL;
    }

    if (cmd->look_criterion != NULL) {
      free(cmd->announce_leech);
      cmd->look_criterion = NULL;
    }
    free(cmd);
    cmd = NULL;
  }
}


/*
  int main() {
  char *buffer = "announce listen 1106 seed [coukie.txt 2 2048 5588DLLFIJ555DKKCJ5669DLEKDPAEE9 test2.lld 1364 2048 LLD755EIIJFNFKJHDG5EC5BE745B7110]";

  struct command *cmd = parse(buffer);
  if (cmd != NULL) 
  cmd_free(cmd);
  return 0;
  }
*/

