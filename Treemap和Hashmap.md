##### TreeMap和HashMap概述

<img src="./image/TreeMap与其他TreeMap相关类图.png" alt="map相关类图" style="zoom:67%;" />

##### TreeMap

首先是TreeMap，不同于 HashMap，TreeMap并非要覆写 hashCode和 equals方法来达到 Key去重的目的，它根据comparable或comparator进行排序，保证key的有序性。不管1.7和1.8，它是基于红黑树实现的，通过put()和deleteEntry()来实现红黑树的增加和删除。

在此仅阐述put内部分源码，首先是比较部分，如果未调用外部比较器，就用自然排序的Comparable比较，比较完之后 **fixAfterInsertion **方法则是重中之重，用来调整红黑树。

新节点总是黑色的，如果父结点为黑色则不变化，反之进行后续。

1. 父节点为红色，叔叔是红色，则爷爷变为红色，父亲和右叔变为黑色
2. 父节点为红色，叔叔是黑色，新节点为父亲的右子树，先左旋，父结点变为黑色，爷爷变成红色，最后爷爷右旋。
3. 父节点为红色，叔叔是黑色，新节点为父亲的左子树，父结点变为黑色，爷爷变成红色，最后爷爷右旋。

##### HashMap

HashMap已经看了好几遍了，这里确实有必要记一下。

首先是源码部分，**1.7版本**，put -> 获取hash(key) -> 通过返回值&处理获得index -> 遍历判断slot上是否有相同key，有则覆盖 -> addEntry -> resize -> translate -> createEntry（hashmap和concurrenthashmap在jdk1.8将头插入改为尾插入）；**1.8版本**则在putVal方法中modCount++前尾部插入。

**原因**：在jdk1.7中采用头插入法，在扩容时会改变链表中元素原本的顺序，以至于在并发场景下导致链表成环的问题。而在jdk1.8中采用尾插入法，在扩容时会保持链表元素原本的顺序，就不会出现**链表成环**的问题了，个人觉得也没了并发条件下数据被覆盖的情况。

