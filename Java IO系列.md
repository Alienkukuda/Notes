#### IO的理解

IO是计算机中Input和Output简称，Java对于它的实现可以分为字节流和字符流。它跟数据的传输跑不了关系，无论是在文件传输上还是socket连接后数据的传输。

传统的IO是同步阻塞IO，拿网络数据传输来说，他是一个连接建立一个线程，一个请求可以有多个连接，不仅如此，线程间还会互相阻塞，一个线程在传输数据时，其它线程会等待。

##### 传统IO的几个知识点

###### **缓冲流**

Java缓冲流是在**输入流**和**输出流**之上进行了一次包装（装饰器设计模式），目的是解决频繁写入/读取数据时效率差的问题。缓冲流先将数据缓存起来，然后一起写入或读取出来。

字节缓冲流类：**BufferedInputStream 和** **BufferedOutputStream**

字符缓冲流类：**BufferedReader 和** **BufferedWriter**

###### **flush()方法**

当向文件写入数据时是先将输出流写入到缓冲区，当缓冲区写满后才将缓冲区的内容输出到文件中。但是当主机完成输出流的输出后，有可能缓冲区这个时候还没有被填满，这样的话，就会一直等待主机发送内容，这时候，就可以使用flush()方法将缓冲区的内容强制输出到文件中，清空缓冲区。所以，一般在关闭输出流之前，要先调用flush()方法强制缓冲区中的内容输出，并清空缓冲区。

##### NIO的出现

这样的后果就是会创建大量的线程，这个可以用线程池来管理提高效率，但是线程间无用的切换以及阻塞对资源的浪费却很低效。

所以JDK1.4 提供了NIO（New IO）API，它是同步非阻塞IO，相比较传统IO，他是一个请求一个线程，客户端发送的连接请求都会注册到多路复用器上，多路复用器轮询到连接有I/O请求时才启动一个线程进行处理。

#### IO和NIO的不同

##### 面向流和面向缓冲

NIO中引入了缓冲区的概念，读数据的时候从缓冲区中取，写的时候将数据填入缓冲区。尽管传统JavaIO中也有相应的缓冲区过滤器流（BufferedInputStream等），但是移进/移出的操作是还是调用InputStream的方法完成，需要程序员来包装，并不是一种提高I/O效率的措施。NIO的缓冲区则不然，对缓冲区的移进/移出操作是由底层操作系统来实现的。换一局话说，IO面向流，NIO面向块。

##### 阻塞与非阻塞

传统JavaIO是基于阻塞I/O模型。这意味着，当一个线程调用read() 或 write()时，该线程被阻塞，直到有一些数据被读取，或数据完全写入。该线程在此期间不能再干任何事情了。 Java NIO的非阻塞模式，使一个线程从某channel发送请求读取数据，但是它仅能得到目前可用的数据，如果目前没有数据可用时，就什么都不会获取。而不是保持线程阻塞，所以直至数据变的可以读取之前，该线程可以继续做其他的事情。 非阻塞写也是如此。 线程通常将非阻塞IO的空闲时间用于在其它通道上执行IO操作，所以一个单独的线程现在可以管理多个输入和输出通道（channel）。

#### NIO核心

##### Channel

Channel（通道）和IO中的流是差不多一个等级的。只不过流是单向的，譬如：InputStream, OutputStream。而Channel是双向的，既可以用来进行读操作，又可以用来进行写操作。主要的实现有：FileChannel、DatagramChannel、SocketChannel、ServerSocketChannel。分别可以对应文件IO、UDP和TCP（Server和Client）。

##### Buffer

NIO中的关键Buffer实现有：ByteBuffer, CharBuffer, DoubleBuffer, FloatBuffer, IntBuffer, LongBuffer, ShortBuffer，分别对应基本数据类型: byte, char, double, float, int, long, short。

##### Selector

Java NIO的选择器允许一个单独的线程来监视多个输入通道，你可以注册多个通道使用一个选择器，然后使用一个单独的线程来“选择”通道：这些通道里已经有可以处理的输入，或者选择已准备写入的通道。这种选择机制，使得一个单独的线程很容易来管理多个通道。

#### 谈谈reactor模型

网络处理过程一般分为accept、read、decode、process、encode、send，reactor是以事件驱动的模型，将每个步骤映射为一个Task，服务端线程执行的最小逻辑单元不再是一次完整的网络请求，而是Task，且采用非阻塞方式执行。

1. Reactor 将 I/O 事件与对应的 Handler绑定，用selector实现
2. Acceptor（Handler的一种） 处理客户端新连接，并分派请求到处理器链中
3. Handlers 执行非阻塞读/写 任务

reactor模型结合netty的实现更好理解

```java
 serverBootstrap.group(xxx)
                .channel(xxx)
                .childHandler(xxx)
```

至于Reactor 单线程模型、单 Reactor 多线程模型、主从 Reactor 多线程模型无非就是IO读写是否多线程、连接是否多线程。

#### Select、poll、epoll

(1)select==> 时间复杂度 O(n)

它仅仅知道了，有 I/O 事件发生了，却并不知道是哪那几个流（可能有一个，多个，甚至全部），我们只能无差别轮询所有流，找出能读出数据，或者写入数据的流，对他们进行操作。所以 select 具有 O(n)的无差别轮询复杂度，同时处理的流越多，无差别轮询时间就越长。

(2)poll==> 时间复杂度 O(n)

poll 本质上和 select 没有区别，它将用户传入的数组拷贝到内核空间，然后查询每个 fd 对应的设备状态， 但是它没有最大连接数的限制，原因是它是基于链表来存储的。

(3)epoll==> 时间复杂度 O(1)

epoll 可以理解为 event poll，不同于忙轮询和无差别轮询，epoll 会把哪个流发生了怎样的 I/O 事件通知我们。所以我们说 epoll 实际上是事件驱动（每个事件关联上 fd）的，此时我们对这些流的操作都是有意义的。（复杂度降低到了 O(1)）

select，poll，epoll 都是 IO 多路复用的机制。I/O 多路复用就通过一种机制，可以监视多个描述符，一旦某个描述符就绪 （一般是读就绪或者写就绪），能够通知程序进行相应的读写操作。但 select，poll，epoll 本质上都是同步 I/O，因为他们都 需要在读写事件就绪后自己负责进行读写，也就是说这个读写过程是阻塞的，而异步 I/O 则无需自己负责进行读写，异步 I/O 的实现会负责把数据从内核拷贝到用户空间。

#### 零拷贝

##### 操作系统方面

![零拷贝](./image/零拷贝前.jpg)

最开始的操作，将文件内容到操作系统的buffer，再从操作系统buffer拷贝到程序应用buffer，从程序buffer拷贝到socket buffer，

从socketbuffer拷贝到协议引擎。要经过4次用户态和内核态的切换。

![零拷贝后](./image/零拷贝后.gif)

零拷贝后，省去了将操作系统的readbuffer拷贝到程序的buffer，以及从程序buffer拷贝到socket buffer的步骤，直接将readbuffer拷贝到socket buffer。再之后，做了更进一步的改进，不再使用socket buffer，而是直接将readbuffer数据拷贝到协议引擎，而socket buffer只会记录数据位置的描述符和数据长度。

![零拷贝优化后](./image/零拷贝优化后.gif)

##### Java NIO方面

1、虚拟内存映射，java nio提供的FileChannel提供了map()方法，该方法可以在一个打开的文件和MappedByteBuffer之间建立一个虚拟内存映射，MappedByteBuffer继承于ByteBuffer，类似于一个基于内存的缓冲区，只不过该对象的数据元素存储在磁盘的一个文件中；调用get()方法会从磁盘中获取数据，此数据反映该文件当前的内容，调用put()方法会更新磁盘上的文件，并且对文件做的修改对其他阅读者也是可见的。（记不太住吧）

2、直接堆外内存，DirectByteBuffer继承于MappedByteBuffer，通过Native函数库直接分配堆外内存，然后通过Java堆中的DirectByteBuffer对象来对这块内存的引用进行操作。

3、Channel-to-Channel传输，Java NIO 中提供的 FileChannel 拥有 transferTo 和 transferFrom 两个方法，可直接把 FileChannel 中的数据拷贝到另外一个 Channel，或者直接把另外一个 Channel 中的数据拷贝到 FileChannel。

##### netty的方式

netty提供了零拷贝的buffer，在传输数据时，最终处理的数据会需要对单个传输的报文，进行组合和拆分，Nio原生的ByteBuffer无法做到，netty通过提供的Composite(组合)和Slice(拆分)两种buffer来实现零拷贝；CompositeChannelBuffer并不会开辟新的内存并直接复制所有ChannelBuffer内容，而是直接保存了所有ChannelBuffer的引用，并在子ChannelBuffer里进行读写，实现了零拷贝。

另外包括NIO的三种方式。

