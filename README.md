Projet CookieTorrent.

Pour compiler le tracker:
	Dans le répertoire src/Tracker, lancer la commande : make
Pour lancer le tracker:
	Dans le répertoire src/Tracker, lancer la commande : ./tracker

Le tracker doit être lancé avant les clients.

Pour compiler/executer le client :
     Dans le répertoire src/Client, lancer la commande : ./CookieTorrent.sh
     Ce script se charge de compiler les sources du projet et d'executer le client.

     Pour executer les différents tests implémentés, lancer la commende : make run 
 	   	  
Ctrl-C pour fermer le client.


Pour générer la documentation du client : La documentation est générée grâce à javadoc. Cela génère une documentation au format html.

Lancer la commande ci-dessous dans le dossier src :

javadoc -private -d Documentation Client/Tools/* Client/*.java Client/DownloadManager/* Client/Parser/* Client/Interface/*.java Client/RequestTreatment/* Client/Tools/*

Pour l'afficher avec firefox par exemple:

firefox ./Documentation/overview-summary.html 

