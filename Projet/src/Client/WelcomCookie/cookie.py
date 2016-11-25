import os
path_learn_dirname = os.path.dirname(os.path.realpath(__file__))
with open(os.path.join(path_learn_dirname, "cookie.txt"), "rb") as f:
    s = f.read()
    print 
    print "========== Attention, ceci est un COOKIE ! ==========="
    print 
    print s
