####  redis的搭建

springboot整合redis很简单，自己以前也用过，本篇仅仅是作为一篇记录。以后有机会写一篇关于redis本质以及redis作为缓存和数据库一致性的问题。

话不多说，进入正文：

##### redis依赖的引入

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

##### 添加redis配置文件

笔者使用的是springboot，直接在application.yml中配置即可，部分人使用的是application.properties，可以自行修改。

```xml
spring:
  redis:
		# Redis服务器地址 
    host: 127.0.0.1
		# Redis服务器连接端口 
    port: 6379
    # 密码 没有则可以不填
		password= 123456
    # 如果使用的jedis 则将lettuce改成jedis即可
    lettuce:
      pool:
        # 最大活跃链接数 默认8
        max-active: 8
        # 最大空闲连接数 默认8
        max-idle: 8
        # 最小空闲连接数 默认0
        min-idle: 0
```

##### 缓存的配置

这里笔者要说明一下，网上对这地方的配置大体上相同，但又有所不同，但无非就是配置自定义的`redisTemplate`和`redisConnectionFactory`。至于另外redis中`key`和`value`的序列化和`redisConnectionFactory`是否自定义，笔者比较懒，`redisConnectionFactory`这部分就自动化实现了，以下是笔者的实现代码：

```java
@EnableCaching
@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)//该部分是根据yml自动配置，也可自行实现
public class RedisConfig {
	/**
	 * 配置自定义redisTemplate<K, V>
	 * @param connectionFactory
	 * @return
	 */
	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory 						connectionFactory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setValueSerializer(jackson2JsonRedisSerializer());
		//使用StringRedisSerializer来序列化和反序列化redis的key值
		template.setKeySerializer(new StringRedisSerializer());
		template.setHashKeySerializer(new StringRedisSerializer());
		template.setHashValueSerializer(jackson2JsonRedisSerializer());
		template.afterPropertiesSet();
		return template;
	}

	/**
	 * json序列化
	 * @return
	 */
	@Bean
	public RedisSerializer<Object> jackson2JsonRedisSerializer() {
		//使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
		Jackson2JsonRedisSerializer serializer = new Jackson2JsonRedisSerializer(Object.class);

		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    // 此项必须配置，否则会报java.lang.ClassCastException: java.util.LinkedHashMap cannot be cast to XXX
		mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		serializer.setObjectMapper(mapper);
		return serializer;
	}


	/**
	 * 配置缓存管理器
	 * @param redisConnectionFactory
	 * @return
	 */
	@Bean
	public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
		// 生成一个默认配置，通过config对象即可对缓存进行自定义配置
		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
		// 设置缓存的默认过期时间，也是使用Duration设置
		config = config.entryTtl(Duration.ofMinutes(1))
				// 设置 key为string序列化
				.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new 					StringRedisSerializer()))
				// 设置value为json序列化
			     					  .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer()))
				// 不缓存空值
				.disableCachingNullValues();

		// 设置一个初始化的缓存空间set集合
		Set<String> cacheNames = new HashSet<>();
		cacheNames.add("timeGroup");
		cacheNames.add("user");

		// 对每个缓存空间应用不同的配置
		Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
		configMap.put("timeGroup", config);
		configMap.put("user", config.entryTtl(Duration.ofSeconds(120)));

		// 使用自定义的缓存配置初始化一个cacheManager
		RedisCacheManager cacheManager = RedisCacheManager.builder(redisConnectionFactory)
				// 一定要先调用该方法设置初始化的缓存名，再初始化相关的配置
				.initialCacheNames(cacheNames)
				.withInitialCacheConfigurations(configMap)
				.build();
		return cacheManager;
	}
}
```

##### 数据缓存

接下是比较重要的方面，我们在前面已经进行了序列化，所以在此不再提起。这里我分两个方面来讲

##### 小项目

自身项目其实不用纠结复杂的操作，无非就是添加缓存、删除缓存、更新缓存操作。可以实现一个redis服务类，直接在进行数据库操作时，使用对应的缓存方法，如下：

```java
@Service
public class RedisService {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    public void set(String key, Object value) {
        //更改在redis里面查看key编码问题，其实无所谓，config中已配置
        RedisSerializer redisSerializer =new StringRedisSerializer();
        redisTemplate.setKeySerializer(redisSerializer);
        ValueOperations<String,Object> vo = redisTemplate.opsForValue();
        vo.set(key, value);
    }

    public Object get(String key) {
        ValueOperations<String,Object> vo = redisTemplate.opsForValue();
        return vo.get(key);
    }
}s
```

然后就是在controller调用

```java
@Autowired
private RedisTemplate<String, Object> redisTemplate;

redisTemplate.opsForValue().set("", "");
redisTemplate.opsForValue().get("");
```

也可以采用**注解**来实现 PS：@Cacheable用于添加缓存，@CachePut用于更新缓存，@CacheEvict用于删除缓存，三者都有三个参数分别为value、key、condition。

```java
/**
	 * @CacheEvict 应用到删除数据的方法上，调用方法时会从缓存中删除对应key的数据
	 *      condition 与unless相反，只有表达式为真才会执行。
	 * @param id 主键id
	 * @return
	 */
	@CacheEvict(value = "user", key = "#id", condition = "#result eq true")
	@Override
	public Boolean removeUser(int id) {
		// 如果删除记录不为1  则是失败
		return userMapper.deleteById(id) == 1;
	}

	/**
	 *  @Cacheable 应用到读取数据的方法上，先从缓存中读取，如果没有再从DB获取数据，然后把数据添加到缓存中
	 *            key 缓存在redis中的key
	 *            unless 表示条件表达式成立的话不放入缓存
	 * @param id 主键id
	 * @return
	 */
	@Cacheable(value = "user", key = "#id", unless = "#result eq null ")
	@Override
	public User getById(int id) {
		return userMapper.selectById(id);
	}

	/**
	 *  @CachePut 应用到写数据的方法上，如新增/修改方法，调用方法时会自动把相应的数据放入缓存
	 * @param user 用户信息
	 * @return
	 */
	@CachePut(value = "user", key = "#user.id", unless = "#user eq null ")
	@Override
	public User updateUser(User user) {
		userMapper.update(user);
		return user;
	}
```

##### 公司项目

如果是像公司项目这种比较大的项目，redis服务类则需要封装的比较完整，不应该仅仅局限于基本操作，例如`ValueOperations`和`HashOperations`两个工具类，这里就不再展开。

下期预告，redis集群和springboot的整合

---

这边就先跳过redis集群环境的搭建，无非就是修改下conf文件，然后用ruby安装集群所需软件，最后创建集群并启动集群。

#### redis集群的搭建

##### 依赖的引入

集群和springboot的整合和上面redis的整合类似，也要引入dependency，这里跳过。

##### yml的配置

```xml
spring:
  redis:
    jedis:
      pool:
        max-wait:5000
        max-Idle:50
        min-Idle:5
    cluster:
    	nodes:127.0.0.1:7000,127.0.0.1:7001,127.0.0.1:7002,127.0.0.1:7003,127.0.0.1:7004
    	127.0.0.1:7005
    timeout:1500
```

##### redis-cluster的使用

使用推荐的**redisTemplate**使用Redis-Cluster，这里配置和前面redis的配置一样。

```java
@Configuration
public class RedisClusterConfiguration{

       @Bean
       public RedisTemplate<String,String> redisTemplate(RedisConnectionFactory redisConnectionfactory){
              RedisTemplate<String,String> redisTemplate=new RedisTemplate<>();
              redisTemplate.setConnectionFactory(redisConnectionFactory);
              redisTemplate.setKeySerializer(new StringRedisSerializer());
              redisTemplate.setValueSerializer(new StringRedisSerializer());
              redisTemplate.afterPropertiesSet();
              return redisTemplate;
       }
}
```

进行单元测试

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisClusterTest{
  
        @Autowire
       private RedisTemplate<String,String> redisTemplate;

 
       @Test
       public void getValue(){
            ValueOperations<String,String> operations=redisTemplate.opsForValue();
            System.out.println(operations.get("key1"));
      }
}
```

##### 使用**JedisCluster**使用Redis-Cluster

加入依赖

```xml
<dependency>
    <groupId>redis.clients</groupId>
    <articleId>jedis</articleId>
</dependency>
```

application.yml配置

```
 #redis集群配置  
  redis:  
    # Redis服务器连接密码（默认为空）   
    password: *****  
    jedis:  
      pool:  
       # 连接池最大连接数（使用负值表示没有限制）  
        max-active: 5000  
       # 连接池最大阻塞等待时间（使用负值表示没有限制）     
        max-wait: -1  
       # 连接池中的最大空闲连接  
        max-idle: 30  
       # 连接池中的最小空闲连接   
        min-idle: 5  
    # 连接超时时间（毫秒）  
    timeout: 50000  
    commandTimeout: 50000  
		#集群  
    cluster:  
    	nodes:127.0.0.1:7000,127.0.0.1:7001,127.0.0.1:7002,127.0.0.1:7003,127.0.0.1:7004
    	127.0.0.1:7005
```

RedisConfig.class-java Config配置

```java
@Configuration
@ConditionalOnClass({JedisCluster.class})
public class RedisConfig{
   
    @Value("${spring.redis.cluster.nodes}")  
    private String clusterNodes;  
    @Value("${spring.redis.timeout}")  
    private int timeout;  
    @Value("${spring.redis.jedis.pool.max-idle}")  
    private int maxIdle;  
    @Value("${spring.redis.jedis.pool.max-wait}")  
    private long maxWaitMillis;  
    @Value("${spring.redis.commandTimeout}")  
    private int commandTimeout;
    @Value("${spring.redis.password}")
    private String password;

    @Bean
    public JedisCluster getJedisCluster(){
          String[] cNodes=ClusterNodes.split(",");
          Set<HostAndPort> node=new HashSet<HostAndPort>();
          //分割出集群点
          for(String node:cNodes){
             String[] hp=node.split(":");
             nodes.add(new HostAndPort(hp[0],Integer.parseInt(hp[1])));
          }
          JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
          jedisPoolConfig.setMaxIdle(maxIdle);
	  jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
          JedisCluster jedisCluster=new JedisCluster(nodes,commandTimeout,timeout,maxIdle,password,jedisPoolConfig);
          return jedisCluster;
   }
}
```

编写使用工具类JedisUtils.java

```java
public class JedisClientCluster implements JedisClient {	
    @Autowired	private JedisCluster jedisCluster;

    @Override	
		public String set(String key, String value) {		
        return jedisCluster.set(key, value);	
    } 	
    @Override	
    public String get(String key) {		
        return jedisCluster.get(key);	
    } 	
    @Override	
    public Boolean exists(String key) {		
        return jedisCluster.exists(key);	
    } 	
    @Override	
    public Long expire(String key, int seconds) {		
      return jedisCluster.expire(key, seconds);	
    } 	
    @Override	
    public Long ttl(String key) {		
        return jedisCluster.ttl(key);	
    } 	
    @Override	
    public Long incr(String key) {		
        return jedisCluster.incr(key);	
    } 	
    @Override	
    public Long hset(String key, String field, String value) {		
        return jedisCluster.hset(key, field, value);	
    } 	
    @Override	
    public String hget(String key, String field) {		
        return jedisCluster.hget(key, field);	
    } 	
    @Override	
     public Long hdel(String key, String... field) {		
       return jedisCluster.hdel(key, field);	
     } 
}
```

看起来繁琐不少，个人感觉不如2.x推出的新功能也就是redisTemplate。