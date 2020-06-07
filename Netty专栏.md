#### Netty是什么

Netty封装了JDK的NIO，以事件驱动的网络异步框架。用一句简单的话来说就是：Netty封装了JDK的NIO，让你用得更爽，不用再写一大堆复杂的代码了。

#### netty的重要组件

- Channel：Netty 网络操作抽象类，它除了包括基本的 I/O 操作，如 bind、connect、read、write 等。
- EventLoop：主要是配合 Channel 处理 I/O 操作，用来处理连接的生命周期中所发生的事情。
- ChannelFuture：Netty 框架中所有的 I/O 操作都为异步的，因此我们需要 ChannelFuture 的 addListener()注册一个 ChannelFutureListener 监听事件，当操作执行成功或者失败时，监听就会自动触发返回结果。
- ChannelHandler：充当了所有处理入站和出站数据的逻辑容器。ChannelHandler 主要用来处理各种事件，这里的事件很广泛，比如可以是连接、数据接收、异常、数据转换等。
- ChannelPipeline：为 ChannelHandler 链提供了容器，当 channel 创建时，就会被自动分配到它专属的 ChannelPipeline，这个关联是永久性的。

#### 特点

- 高并发：Netty 是一款基于 NIO（Nonblocking IO，非阻塞IO）开发的网络通信框架，对比于 BIO（Blocking I/O，阻塞IO），他的并发性能得到了很大提高。
- 传输快：Netty 的传输依赖于零拷贝特性，尽量减少不必要的内存拷贝，实现了更高效率的传输。
- 封装好：Netty 封装了 NIO 操作的很多细节，提供了易于使用调用接口。

#### 零拷贝

见NIO专栏，除了虚拟映射、堆外直接内存、文件的tranferTo方法外，多了Buffer组合。

#### 使用Netty不使用JDK原生NIO的原因

JDK自带的NIO变成复杂，netty绑定一个loopgroup、channel、handler就可以。

netty自带拆包解包，异常检测机制。

netty使用了reactor模型，对selector做了很多优化。

自带各种协议栈让你处理任何一种通用协议都几乎不用亲自动手。

#### netty的高性能提现在什么方面

- IO 线程模型：使用reactor模式，同步非阻塞，用最少的资源做更多的事。
- 内存零拷贝：尽量减少不必要的内存拷贝，实现了更高效率的传输。
- 内存池设计：申请的内存可以重用，主要指直接内存。内部实现是用一颗二叉查找树管理内存分配情况。
- 串形化处理读写：避免使用锁带来的性能开销。
- 高性能序列化协议：支持 protobuf 等高性能序列化协议。

#### Netty 发送消息的两种方式

直接写入 Channel 中，消息从 ChannelPipeline 当中尾部开始移动；

写入和 ChannelHandler 绑定的 ChannelHandlerContext 中，消息从 ChannelPipeline 中的下一个 ChannelHandler 中移动。

#### 默认情况 Netty 起多少线程？何时启动？

Netty 默认是 CPU 处理器数的两倍，bind 完之后启动。

#### 心跳机制

在Netty中提供了IdleStateHandler类来进行心跳的处理

- readerIdleTimeSeconds：设置读超时时间；
- writerIdleTimeSeconds：设置写超时时间；
- allIdleTimeSeconds：所有类型。

#### 粘包、拆包的解决方法

1. 客户端在发送数据包的时候，每个包都固定长度，比如 1024 个字节大小，如果客户端发送的数据长度不足 1024 个字节，则通过补充空格的方式补全到指定长度；
2. 客户端在每个包的末尾使用固定的分隔符，例如\r\n，如果一个包被拆分了，则等待下一个包发送过来之后找到其中的\r\n，然后对其拆分后的头部部分与前一个包的剩余部分进行合并，这样就得到了一个完整的包
3. 将消息分为头部和消息体，在头部中保存有当前整个消息的长度，只有在读取到足够长度的消息之后才算是读到了一个完整的消息
4. 通过自定义协议进行粘包和拆包的处理。

具体的实现：

- FixedLengthFrameDecoder
   对于使用**固定长度**的粘包和拆包场景，可以使用 FixedLengthFrameDecoder，该解码一器会每次读取固定长度的消息，如果当前读取到的消息不足指定长度，那么就会等待下一个消息到达后进行补足。
- LineBasedFrameDecoder 与 DelimiterBasedFrameDecoder
   对于通过分隔符进行粘包和拆包问题的处理，Netty 提供了两个编解码的类，LineBasedFrameDecoder 和
   DelimiterBasedFrameDecoder。这里 LineBasedFrameDecoder 的作用主要是通过**换行符**，即\n 或者\r\n 对数据进行处理； 而 DelimiterBasedFrameDecoder 的作用则是通过用户**指定的分隔符**对数据进行粘包和拆包处理。
- LengthFieldBasedFrameDecoder 与 LengthFieldPrepender
   这里 LengthFieldBasedFrameDecoder 与 LengthFieldPrepender 需要配合起来使用，其实本质上来讲，这两者一个是解码，一个是编码的关系。它们处理粘拆包的主要思想是在生成的数据包中添加一个长度字段，用于记录当前数据包的长度。
- 自定义粘包与拆包器
   对于粘包与拆包问题，其实前面三种基本上已经能够满足大多数情形了，但是对于一些更加复杂的协议，可能有一些定制化的需求。
   对于这些场景，其实本质上，我们也不需要手动从头开始写一份粘包与拆包处理器，而是通过继承 LengthFieldBasedFrameDecoder
   和 LengthFieldPrepender 来实现粘包和拆包的处理。