CCNxMall
========

CCNxMall

Messages are saved as files.It just periodically synchronizes the content of two directories over CCNx. 

we need to run two instances for two directories which represents two nodes.


e.g.

mall_messages1 directory has 1.txt and 2.txt

mall_messages2 directory has 3.txt and 4.txt

then execute following in two separate terminals

java -jar CCNxMall.jar ccnx:/mall ./../../../mall_messages1/
java -jar CCNxMall.jar ccnx:/mall ./../../../mall_messages2/
