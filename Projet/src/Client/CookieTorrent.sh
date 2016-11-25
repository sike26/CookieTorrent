#!/bin/bash

if [ -e ./Shared.state ]
then
    echo "Shared.state file is ok !" &
else
    touch Shared.state &
    echo "Creating Shared.state file" &
    echo "filename,size,pieceSize,key,isComplete\n" > Shared.state &    
fi

if [ -e ./build ]
then
    echo "build folder is ok !" &
else
    echo "Creating build folder" &
    mkdir build &
fi

if [ -e ./build ] && [ -d ./build ]
then
    if [ -e ./build/CookieClient/Client.class ]
    then
	python WelcomCookie/cookie.py && make cookie
    else
	echo "Start installing CookieTorrent !" &
	make clean && make && python WelcomCookie/cookie.py && make cookie
    fi
else
    mkdir ./build && make && python WelcomCookie/cookie.py && make cookie
fi


