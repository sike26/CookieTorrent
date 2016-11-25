#include "config.h"

#define MAX_SIZE 100

//read config.ini and set static variable
void config_init(){
    //open config.ini
    FILE* configFile;
    if ((configFile = fopen("config.ini", "r")) == NULL)
    {
	perror("fopen");
	exit(EXIT_FAILURE);
    }
    char buf[MAX_SIZE];
    char *p;
    while (fgets(buf, MAX_SIZE, configFile) != NULL) // while the end of the file is not reached
    {
	p = strtok(buf, " "); //p is the pointer to the first word of the line
	if(p != NULL)
	{
	    if('#' != p[0]) //if the line is not a comment
	    {
		// assignment of tracker_port
		if(!strcmp(p, "tracker-port"))
		{
		    p = strtok(NULL, " ");
		    p = NULL;
		    p = strtok(NULL, " ");
		    tracker_port = (unsigned short int) atoi(p);
		}
		// assignment of tracker_address
		else if(!strcmp(p, "tracker-address"))
		    {
			p = strtok(NULL, " ");
			p = NULL;
			p = strtok(NULL, "\n");
			tracker_address = strdup(p);
		    }
	    }
	}	    
    }
    fclose(configFile);
}
