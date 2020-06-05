#### MQ理解

MQ是消费-生产者模型的一个典型的代表，一端往消息队列中不断写入消息，而另一端则可以读取队列中的消息。自己用ReentrantLock和Condition写个生产者-消费者队列就会很明白。

##### RabbitMQ

发消息者、交换机、队列、收消息者

Direct, topic, Headers（不管） and Fanout

无非就是交换机和队列绑定，绑定的时候根据情况是否添加topic路由键，接收方监听队列。

##### RocketMQ

RocketMQ分的比较细，消息生产者、生产者组、消息消费者、消费者组、topic逻辑分类、tag标签、Broker、Name Server为生产者和消费者提供路由信息。（rabbitmq的好理解，发时带topic，交换机根据topic给不同的队列）

<img src="./image/RocketMQ架构.jpg" alt="RocketMQ架构" style="zoom: 33%;" />

从 Broker 开始，Broker Master1 和 Broker Slave1 是主从结构，它们之间会进行数据同步，即 Date Sync。同时每个 Broker 与
 NameServer 集群中的所有节点建立长连接，定时注册 Topic 信息到所有 NameServer 中。

Producer 与 NameServer 集群中的其中一个节点（随机选择）建立长连接，定期从 NameServer 获取 Topic 路由信息，并向提供 Topic 服务的 Broker Master 建立长连接，且定时向 Broker 发送心跳。Producer 只能将消息发送到 Broker master，但是 Consumer 则不一样，它同时和提供 Topic 服务的 Master 和 Slave建立长连接，既可以从 Broker Master 订阅消息，也可以从 Broker Slave 订阅消息。

解耦，生产者不用关心是谁来消费，消费者不用关心谁在生产消息；异步、削峰，异步下单这个例子。

#### RocketMQ是怎么保证系统高可用的

多Master部署，防止单点故障；主从结构，防止消息丢失。

#### RocketMQ是怎么保证系统可靠性

为了降低消息丢失的概率，MQ需要进行超时和重传。

sender传给mq server，mq收到发送ACK给sender，sender接受到表示成功。如果mq发送ack失败，sender 内的 timer 会重发消息，直到收到 ACK消息，如果重试N次后还未收到，则回调发送失败。需要注意的是，这个过程中 mq server 可能会收到同一条消息的多次重发。所以对每条消息，MQ系统内部必须生成一个inner-msg-id，作为去重和幂等的依据，这个内部消息ID的特性是：

- 全局唯一
- MQ生成，具备业务无关性，对消息发送方和消息接收方屏蔽

mq server和receiver之间也类似，需要强调的是，receiver 回ACK给 mq server，是消息消费业务方的主动调用行为。为了保证业务幂等性，业务消息体中，必须有一个biz-id，作为去重和幂等的依据，这个业务ID的特性是：

- 对于同一个业务场景，全局唯一
- 业务相关，对MQ透明